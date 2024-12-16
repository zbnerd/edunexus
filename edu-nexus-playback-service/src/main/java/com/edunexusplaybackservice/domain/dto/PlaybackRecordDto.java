package com.edunexusplaybackservice.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlaybackRecordDto {
    private Long userId;
    private Long fileId;
    private LocalDateTime startTime;
}
