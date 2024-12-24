package com.edunexusplaybackservice.domain.entity;

import com.edunexusplaybackservice.domain.dto.EventLogDto;
import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceOuterClass;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "event_logs")
public class EventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private PlaybackRecord playbackRecord;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime timestamp;

    public void setEventLogInfo(EventLogDto eventLogDto) {
        this.playbackRecord = eventLogDto.getPlaybackRecord();
        this.userId = eventLogDto.getUserId();
        this.eventType = eventLogDto.getEventType();
        this.timestamp = eventLogDto.getTimestamp();
    }

    public PlaybackServiceOuterClass.EventLog toProto() {
        PlaybackServiceOuterClass.EventLog.Builder builder = PlaybackServiceOuterClass.EventLog.newBuilder();

        if (eventId != null) {
            builder.setEventId(eventId);
        }

        if (this.playbackRecord != null && this.playbackRecord.getRecordId() != null) {
            builder.setRecordId(this.playbackRecord.getRecordId());
        }
        if (this.userId != null) {
            builder.setUserId(this.userId);
        }
        if (this.eventType != null) {
            builder.setEventType(this.eventType.name());
        }
        if (this.timestamp != null) {
            builder.setTimestamp(this.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        return builder.build();
    }
}
