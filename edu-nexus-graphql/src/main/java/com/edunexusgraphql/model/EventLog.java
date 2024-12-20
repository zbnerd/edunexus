package com.edunexusgraphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {
    private Long eventId;
    private Long recordId;
    private Long userId;
    private String eventType;
    private String timestamp;
}
