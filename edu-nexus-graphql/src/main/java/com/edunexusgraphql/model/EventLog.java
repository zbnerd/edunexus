package com.edunexusgraphql.model;

import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceOuterClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {
    private Long recordId;
    private String eventType;

    public static EventLog fromProto(PlaybackServiceOuterClass.EventLog proto) {
        EventLog log = new EventLog();
        log.setRecordId(proto.getRecordId());
        log.setEventType(proto.getEventType());
        return log;
    }

    public static PlaybackServiceOuterClass.EventLog toProto(EventLog domain) {
        return PlaybackServiceOuterClass.EventLog.newBuilder()
                .setRecordId(domain.getRecordId())
                .setEventType(domain.getEventType())
                .build();
    }
}
