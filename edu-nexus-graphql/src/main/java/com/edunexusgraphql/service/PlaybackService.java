package com.edunexusgraphql.service;


import com.edunexusgraphql.model.EventLog;
import com.edunexusgraphql.model.PlaybackRecord;
import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceGrpc;
import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceOuterClass;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class PlaybackService {

    @GrpcClient("edu-nexus-playback-service")
    private PlaybackServiceGrpc.PlaybackServiceBlockingStub playbackServiceBlockingStub;

    public PlaybackRecord startRecord(Long userId, Long fileId) {
        PlaybackRecord record = new PlaybackRecord();
        record.setUserId(userId);
        record.setFileId(fileId);

        PlaybackServiceOuterClass.StartRecordRequest request =
                PlaybackServiceOuterClass.StartRecordRequest.newBuilder()
                        .setUserId(userId)
                        .setFileId(fileId)
                        .build();

        PlaybackServiceOuterClass.StartRecordResponse response =
                playbackServiceBlockingStub.startRecord(request);

        return PlaybackRecord.fromProto(response.getRecord());
    }

    public PlaybackRecord endRecord(Long recordId) {
        PlaybackServiceOuterClass.EndRecordRequest request =
                PlaybackServiceOuterClass.EndRecordRequest.newBuilder()
                        .setRecordId(recordId)
                        .build();

        PlaybackServiceOuterClass.EndRecordResponse response =
                playbackServiceBlockingStub.endRecord(request);

        return PlaybackRecord.fromProto(response.getRecord());
    }

    public EventLog logEvent(EventLog eventLog) {
        PlaybackServiceOuterClass.LogEventRequest request = PlaybackServiceOuterClass.LogEventRequest.newBuilder()
                .setEvent(EventLog.toProto(eventLog))
                .build();
        PlaybackServiceOuterClass.LogEventResponse response = playbackServiceBlockingStub.logEvent(request);

        return EventLog.fromProto(response.getEvent());
    }

}