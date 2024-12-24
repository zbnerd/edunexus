package com.edunexusplaybackservice.domain.service;

import com.edunexusplaybackservice.domain.dto.EventLogDto;
import com.edunexusplaybackservice.domain.dto.PlaybackRecordDto;
import com.edunexusplaybackservice.domain.entity.EventLog;
import com.edunexusplaybackservice.domain.entity.PlaybackRecord;
import com.edunexusplaybackservice.domain.repository.EventLogRepository;
import com.edunexusplaybackservice.domain.repository.PlaybackRecordRepository;
import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceGrpc;
import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceOuterClass;
import handler.GrpcResponseHandler;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class PlaybackService extends PlaybackServiceGrpc.PlaybackServiceImplBase {

    private final PlaybackRecordRepository playbackRecordRepository;
    private final EventLogRepository eventLogRepository;

    @Override
    public void startRecord(PlaybackServiceOuterClass.StartRecordRequest request, StreamObserver<PlaybackServiceOuterClass.StartRecordResponse> responseObserver) {

        PlaybackRecord record = new PlaybackRecord();
        record.setPlaybackRecordInfo(
                PlaybackRecordDto.builder()
                        .userId(request.getUserId())
                        .fileId(request.getFileId())
                        .startTime(LocalDateTime.now())
                        .build()
        );

        PlaybackServiceOuterClass.StartRecordResponse response = PlaybackServiceOuterClass.StartRecordResponse.newBuilder()
                .setRecord(record.toProto())
                .build();

        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void endRecord(PlaybackServiceOuterClass.EndRecordRequest request, StreamObserver<PlaybackServiceOuterClass.EndRecordResponse> responseObserver) {
        Optional<PlaybackRecord> playbackRecordOptional = playbackRecordRepository.findById(request.getRecordId());

        if (playbackRecordOptional.isPresent()) {
            PlaybackRecord record = playbackRecordOptional.get();

            record.setEndTime(LocalDateTime.now());
            playbackRecordRepository.save(record);

            PlaybackServiceOuterClass.EndRecordResponse response = PlaybackServiceOuterClass.EndRecordResponse.newBuilder()
                    .setRecord(record.toProto())
                    .build();

            GrpcResponseHandler.sendResponse(response, responseObserver);
        } else {
            responseObserver.onError(new Throwable("Record not found"));
        }

    }

    @Override
    public void logEvent(PlaybackServiceOuterClass.LogEventRequest request, StreamObserver<PlaybackServiceOuterClass.LogEventResponse> responseObserver) {

        Optional<PlaybackRecord> playbackRecordOptional = playbackRecordRepository.findById(request.getEvent().getRecordId());

        if (playbackRecordOptional.isPresent()) {
            PlaybackRecord record = playbackRecordOptional.get();

            EventLog event = new EventLog();
            event.setEventLogInfo(
                    EventLogDto.builder()
                            .playbackRecord(record)
                            .eventType(request.getEvent().getEventType())
                            .userId(request.getEvent().getUserId())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
            event = eventLogRepository.save(event);

            PlaybackServiceOuterClass.LogEventResponse response = PlaybackServiceOuterClass.LogEventResponse.newBuilder()
                    .setEvent(event.toProto())
                    .build();
            GrpcResponseHandler.sendResponse(response, responseObserver);
        } else {
            responseObserver.onError(new Throwable("Record not found"));
        }


    }
}
