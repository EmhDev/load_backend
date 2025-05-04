package com.astra.load_backend.service;

import com.astra.load_backend.domain.model.FileUse;
import com.astra.load_backend.domain.port.in.FileUseIn;
import com.astra.load_backend.domain.port.out.FileUseOutPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.io.ByteArrayOutputStream;


@Service
public class LoadFileUseService implements FileUseIn {

    @Autowired
    private FileUseOutPort fileUseOutPort;   //FileStoragePort

    @Override
    public List<FileUse> splitFile(MultipartFile file, List<Integer> partSizes) throws IOException {
        byte[] content = file.getBytes();
        int totalSize = content.length;

        List<FileUse> segments = new ArrayList<>();
        int offset = 0;

        for (int i = 0; i < partSizes.size(); i++) {
            int partSizeKB = partSizes.get(i);
            int partSizeBytes = Math.min(partSizeKB * 1024, totalSize - offset);

            byte[] partContent = Arrays.copyOfRange(content, offset, offset + partSizeBytes);
            FileUse segment = new FileUse(file.getOriginalFilename() + "." + i, partContent, i);
            segments.add(segment);
            offset += partSizeBytes;
        }

        fileUseOutPort.saveOriginalFile(file.getOriginalFilename(), content);
        segments.forEach(segment -> fileUseOutPort.saveSegment(file.getOriginalFilename(), segment));

        return segments;
    }

    /**
     * @param segments
     * @return
     */
    @Override
    public byte[] reconstructFile(List<FileUse> segments) {
        return segments.stream()
                .sorted(Comparator.comparingInt(FileUse::getIndex))
                .flatMapToInt(s -> IntStream.range(0, s.getContent().length)
                        .map(i -> s.getContent()[i] & 0xFF))
                .collect(ByteArrayOutputStream::new,
                        (baos, b) -> baos.write((byte) b),
                        (baos1, baos2) -> {
                            try {
                                baos2.writeTo(baos1);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                .toByteArray();

    }
}

