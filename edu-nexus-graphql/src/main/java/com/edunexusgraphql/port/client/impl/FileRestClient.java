package com.edunexusgraphql.port.client.impl;

import com.edunexusgraphql.model.CourseSessionFile;
import com.edunexusgraphql.port.client.FileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * RestTemplate-based implementation of FileClient.
 * Handles HTTP communication with the file management service.
 */
@Slf4j
@Component
public class FileRestClient implements FileClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://edu-nexus-file-manage-service/sessions/{sessionId}/files";

    public FileRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "files", key = "#sessionId")
    public List<CourseSessionFile> findFilesBySessionId(Long sessionId) {
        log.debug("Finding files for sessionId={}", sessionId);
        String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .buildAndExpand(sessionId).toUriString();
        CourseSessionFile file = restTemplate.getForObject(url, CourseSessionFile.class);
        return Optional.ofNullable(file).stream().toList();
    }

    @Override
    @Cacheable(value = "file", key = "#sessionId + '_' + #fileId")
    public Optional<CourseSessionFile> getFileById(Long sessionId, Long fileId) {
        log.debug("Finding file: sessionId={}, fileId={}", sessionId, fileId);
        String url = UriComponentsBuilder.fromUriString(BASE_URL + "/{fileId}")
                .buildAndExpand(sessionId, fileId).toUriString();
        CourseSessionFile file = restTemplate.getForObject(url, CourseSessionFile.class);
        return Optional.ofNullable(file);
    }
}
