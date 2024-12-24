package com.edunexusplaybackservice.domain.service;

import com.edunexusplaybackservice.domain.dto.EventLogDto;
import com.edunexusplaybackservice.domain.dto.PlaybackRecordDto;
import com.edunexusplaybackservice.domain.entity.EventLog;
import com.edunexusplaybackservice.domain.entity.EventType;
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

        playbackRecordRepository.save(record);

        PlaybackServiceOuterClass.StartRecordResponse response = PlaybackServiceOuterClass.StartRecordResponse.newBuilder()
                .setRecord(record.toProto())
                .build();


        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void endRecord(PlaybackServiceOuterClass.EndRecordRequest request, StreamObserver<PlaybackServiceOuterClass.EndRecordResponse> responseObserver) {
        Optional<PlaybackRecord> playbackRecordOptional = playbackRecordRepository.findById(request.getRecordId());
        GrpcResponseHandler.handleOptional(
                playbackRecordOptional,
                this::getEndRecordResponse,
                responseObserver
        );
    }

    @Override
    public void logEvent(PlaybackServiceOuterClass.LogEventRequest request, StreamObserver<PlaybackServiceOuterClass.LogEventResponse> responseObserver) {

        Optional<PlaybackRecord> playbackRecordOptional = playbackRecordRepository.findById(request.getRecordId());

        GrpcResponseHandler.handleOptional(
                playbackRecordOptional,
                record -> getLogEventResponse(request, record),
                responseObserver
        );
    }

    private PlaybackServiceOuterClass.LogEventResponse getLogEventResponse(PlaybackServiceOuterClass.LogEventRequest request, PlaybackRecord record) {
        EventLog event = new EventLog();
        event.setEventLogInfo(
                EventLogDto.builder()
                        .playbackRecord(record)
                        .eventType(EventType.valueOf(request.getEventType()))
                        .userId(record.getUserId())
                        .timestamp(LocalDateTime.now())
                        .build()
        );


        eventLogRepository.save(event);

        return PlaybackServiceOuterClass.LogEventResponse.newBuilder()
                .setEvent(event.toProto())
                .build();
    }

    private PlaybackServiceOuterClass.EndRecordResponse getEndRecordResponse(PlaybackRecord record) {
        record.setEndTime(LocalDateTime.now());
        playbackRecordRepository.save(record);

        return PlaybackServiceOuterClass.EndRecordResponse.newBuilder()
                .setRecord(record.toProto())
                .build();
    }
}
