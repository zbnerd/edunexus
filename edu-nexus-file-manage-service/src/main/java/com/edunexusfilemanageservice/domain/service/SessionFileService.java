package com.edunexusfilemanageservice.domain.service;

import com.edunexusfilemanageservice.domain.entity.SessionFile;
import com.edunexusfilemanageservice.domain.repository.SessionFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionFileService {

    private final SessionFileRepository sessionFileRepository;

    public Optional<SessionFile> findTopBySessionIdOrderByFileIdDesc(Long sessionId) {
        return sessionFileRepository.findTopBySessionIdOrderByFileIdDesc(sessionId);
    }

    public Optional<SessionFile> findFileById(Long fileId) {
        return sessionFileRepository.findById(fileId);
    }

    public SessionFile saveFile(SessionFile sessionFile) {
        return sessionFileRepository.save(sessionFile);
    }

    public void deleteFile(Long fileId) {
        sessionFileRepository.deleteById(fileId);
    }

}
