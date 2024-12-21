package com.edunexusgraphql.service;

import com.edunexusgraphql.model.CourseSessionFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://edu-nexus-file-manage-service/sessions/{sessionId}/files";

    @Autowired
    public FileService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "files", key = "#sessionId")
    public List<CourseSessionFile> findFilesBySessionId(Long sessionId) {
        String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .buildAndExpand(sessionId).toUriString();
        CourseSessionFile file = restTemplate.getForObject(url, CourseSessionFile.class);
        return Optional.ofNullable(file).stream().toList();
    }

    @Cacheable(value = "file", key = "#fileId")
    public Optional<CourseSessionFile> getFileById(Long sessionId, Long fileId) {
        String url = UriComponentsBuilder.fromUriString(BASE_URL + "/{fileId}")
                .buildAndExpand(sessionId, fileId).toUriString();
        CourseSessionFile file = restTemplate.getForObject(url, CourseSessionFile.class);
        return Optional.ofNullable(file);
    }
}