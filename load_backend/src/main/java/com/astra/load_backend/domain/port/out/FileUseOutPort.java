package com.astra.load_backend.domain.port.out;

import com.astra.load_backend.domain.model.FileUse;

import java.util.List;

public interface FileUseOutPort {

    void saveSegment(String originalFileName, FileUse segment);
    List<FileUse> getSegments(String originalFileName);
    void saveOriginalFile(String originalFileName, byte[] content);
    byte[] getOriginalFile(String originalFileName);
    byte[] getSegmentBytes(String segmentFileName);
}
