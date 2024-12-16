package com.edunexusplaybackservice.domain.dto;

import com.edunexusplaybackservice.domain.entity.PlaybackRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventLogDto {
    private PlaybackRecord playbackRecord;
    private Long userId;
    private String eventType;
    private LocalDateTime timestamp;
}
