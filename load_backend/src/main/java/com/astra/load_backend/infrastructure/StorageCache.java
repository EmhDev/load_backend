package com.astra.load_backend.infrastructure;

import com.astra.load_backend.domain.model.FileUse;
import com.astra.load_backend.domain.port.out.FileUseOutPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StorageCache implements FileUseOutPort {

    private final Map<String, List<FileUse>> segmentStorage = new HashMap<>();
    private final Map<String, byte[]> originalFileStorage = new HashMap<>();
    private final Map<String, byte[]> singleSegmentStorage = new HashMap<>(); // para descarga por nombre

    @Override
    public void saveSegment(String originalFileName, FileUse segment) {
        segmentStorage.computeIfAbsent(originalFileName, k -> new ArrayList<>()).add(segment);
        singleSegmentStorage.put(segment.getName(), segment.getContent()); // acceso rápido por nombre
    }

    @Override
    public List<FileUse> getSegments(String originalFileName) {
        return segmentStorage.getOrDefault(originalFileName, Collections.emptyList());
    }

    @Override
    public void saveOriginalFile(String originalFileName, byte[] content) {
        originalFileStorage.put(originalFileName, content);
    }

    @Override
    public byte[] getOriginalFile(String filename) {
        return originalFileStorage.getOrDefault(filename, new byte[0]);
    }

    @Override
    public byte[] getSegmentBytes(String segmentFileName) {
        return singleSegmentStorage.getOrDefault(segmentFileName, new byte[0]);
    }

    
}
