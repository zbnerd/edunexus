package com.edunexusplaybackservice.domain.repository;

import com.edunexusplaybackservice.domain.entity.PlaybackRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaybackRecordRepository extends JpaRepository<PlaybackRecord, Long> {
}