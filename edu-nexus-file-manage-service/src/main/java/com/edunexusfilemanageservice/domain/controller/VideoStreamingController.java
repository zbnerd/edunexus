package com.edunexusfilemanageservice.domain.controller;

import com.edunexusfilemanageservice.domain.service.SessionFileService;
import com.edunexusfilemanageservice.domain.service.VideoStreamingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller for video streaming endpoints.
 * Delegates streaming logic to VideoStreamingService.
 */
@RestController
@RequestMapping("/sessions/{sessionId}")
@RequiredArgsConstructor
public class VideoStreamingController {

    private final SessionFileService sessionFileService;
    private final VideoStreamingService videoStreamingService;

    /**
     * Streams video for a session with HTTP Range support.
     * Supports partial content delivery for seeking in video players.
     *
     * @param sessionId the session ID
     * @param request the HTTP request (contains Range header)
     * @return ResponseEntity with video stream
     */
    @GetMapping("/streams")
    public ResponseEntity<?> streamVideo(@PathVariable Long sessionId, HttpServletRequest request) {
        Optional<com.edunexusfilemanageservice.domain.entity.SessionFile> fileOptional =
                sessionFileService.findTopBySessionIdOrderByFileIdDesc(sessionId);

        String rangeHeader = request.getHeader("Range");
        return videoStreamingService.prepareVideoStream(fileOptional, rangeHeader);
    }
}
