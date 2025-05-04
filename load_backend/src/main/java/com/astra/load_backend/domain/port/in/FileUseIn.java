package com.astra.load_backend.domain.port.in;

import com.astra.load_backend.domain.model.FileUse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileUseIn {
//FileProcessorUseCase
    List<FileUse> splitFile(MultipartFile file, List<Integer> partList) throws IOException;
    byte[] reconstructFile(List<FileUse> segments);

}
