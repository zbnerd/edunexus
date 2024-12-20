package com.edunexusfilemanageservice.domain.repository;

import com.edunexusfilemanageservice.domain.entity.SessionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionFileRepository extends JpaRepository<SessionFile, Long> {
    List<SessionFile> findBySessionId(Long sessionId);
    Optional<SessionFile> findTopBySessionIdOrderByFileIdDesc(Long sessionId);
}