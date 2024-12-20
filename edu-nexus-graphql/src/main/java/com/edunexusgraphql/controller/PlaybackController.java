package com.edunexusgraphql.controller;

import com.edunexusgraphql.model.EventLog;
import com.edunexusgraphql.model.PlaybackRecord;
import com.edunexusgraphql.service.dummy.DummyPlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PlaybackController {

    private final DummyPlaybackService playbackService;

    @MutationMapping
    public PlaybackRecord startRecord(
            @Argument Long userId,
            @Argument Long fileId
    ) {
        return playbackService.startRecord(userId, fileId);
    }


    @MutationMapping
    public PlaybackRecord endRecord(
            @Argument Long recordId
    ) {
        return playbackService.endRecord(recordId);
    }

    @MutationMapping
    public EventLog logEvent(
            @Argument Long recordId,
            @Argument Long userId,
            @Argument String eventType,
            @Argument String timestamp
    ) {
        EventLog eventLog = new EventLog(null, recordId, userId, eventType, timestamp);
        return playbackService.logEvent(eventLog);
    }
}
