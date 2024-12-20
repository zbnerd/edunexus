package com.edunexusplaybackservice.domain.service;

import com.edunexusplaybackservice.domain.entity.PlaybackRecord;
import com.edunexusplaybackservice.domain.repository.EventLogRepository;
import com.edunexusplaybackservice.domain.repository.PlaybackRecordRepository;
import com.fastcampus.nextplaybackservice.domain.service.PlaybackServiceOuterClass;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackServiceTest {

    @Mock
    private PlaybackRecordRepository playbackRecordRepository;

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private StreamObserver<PlaybackServiceOuterClass.StartRecordResponse> startResponseObserver;

    @Mock
    private StreamObserver<PlaybackServiceOuterClass.EndRecordResponse> endResponseObserver;

    @Mock
    private StreamObserver<PlaybackServiceOuterClass.LogEventResponse> logEventResponseObserver;

    @InjectMocks
    private PlaybackService playbackService;

    @Test
    void testStartRecord() {
        when(playbackRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PlaybackServiceOuterClass.StartRecordRequest request = PlaybackServiceOuterClass.StartRecordRequest.newBuilder()
                .setUserId(1L)
                .setFileId(1L)
                .build();

        playbackService.startRecord(request, startResponseObserver);

        verify(playbackRecordRepository).save(any(PlaybackRecord.class));
        verify(startResponseObserver).onNext(any());
        verify(startResponseObserver).onCompleted();
    }

    @Test
    void testEndRecord() {
    }

    @Test
    void testLogEvent() {
    }
}