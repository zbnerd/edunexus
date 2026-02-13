package com.edunexusgraphql.service;

import com.edunexusgraphql.model.CourseSessionFile;
import com.edunexusgraphql.port.client.FileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for file-related operations.
 * Delegates HTTP client operations to FileClient interface.
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileClient fileClient;

    @Cacheable(value = "files", key = "#sessionId")
    public List<CourseSessionFile> findFilesBySessionId(Long sessionId) {
        return fileClient.findFilesBySessionId(sessionId);
    }

    @Cacheable(value = "file", key = "#sessionId + '_' + #fileId")
    public Optional<CourseSessionFile> getFileById(Long sessionId, Long fileId) {
        return fileClient.getFileById(sessionId, fileId);
    }
}