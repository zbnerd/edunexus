package com.edunexusplaybackservice.domain.repository;

import com.edunexusplaybackservice.domain.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, String> {
}
