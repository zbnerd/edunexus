package com.edunexusgraphql.port.client;

import com.edunexusgraphql.model.CourseSessionFile;

import java.util.List;
import java.util.Optional;

/**
 * Client interface for file management service operations.
 * Abstracts HTTP/RestTemplate concerns from the application layer.
 */
public interface FileClient {

    /**
     * Find all files for a session.
     *
     * @param sessionId Session ID
     * @return List of files
     */
    List<CourseSessionFile> findFilesBySessionId(Long sessionId);

    /**
     * Find a specific file by ID.
     *
     * @param sessionId Session ID
     * @param fileId File ID
     * @return Optional file
     */
    Optional<CourseSessionFile> getFileById(Long sessionId, Long fileId);
}
