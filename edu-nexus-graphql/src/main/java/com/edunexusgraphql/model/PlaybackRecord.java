package com.edunexusgraphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackRecord {
    private Long recordId;
    private Long userId;
    private Long fileId;
    private String startTime;
    private String endTime;
}
