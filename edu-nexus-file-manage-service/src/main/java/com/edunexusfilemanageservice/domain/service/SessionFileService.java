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

    /**
     * Retrieves the most recent file for a specific session.
     *
     * @param sessionId the session's unique identifier
     * @return Optional containing the most recent session file if found
     */
    public Optional<SessionFile> findTopBySessionIdOrderByFileIdDesc(Long sessionId) {
        return sessionFileRepository.findTopBySessionIdOrderByFileIdDesc(sessionId);
    }

    /**
     * Retrieves a session file by its ID.
     *
     * @param fileId the file's unique identifier
     * @return Optional containing the session file if found
     */
    public Optional<SessionFile> findFileById(Long fileId) {
        return sessionFileRepository.findById(fileId);
    }

    /**
     * Saves or updates a session file.
     *
     * @param sessionFile the session file entity to save
     * @return the saved session file with generated ID
     * @throws IllegalArgumentException if sessionFile is null
     */
    public SessionFile saveFile(SessionFile sessionFile) {
        return sessionFileRepository.save(sessionFile);
    }

    /**
     * Deletes a session file by its ID.
     *
     * @param fileId the file's unique identifier
     */
    public void deleteFile(Long fileId) {
        sessionFileRepository.deleteById(fileId);
    }

}
