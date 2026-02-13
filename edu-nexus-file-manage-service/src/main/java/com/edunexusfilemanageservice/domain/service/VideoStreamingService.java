package com.edunexusfilemanageservice.domain.service;

import com.edunexusfilemanageservice.domain.entity.SessionFile;
import com.edunexusfilemanageservice.domain.exception.VideoStreamingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Service for handling video streaming operations.
 * Extracted from VideoStreamingController to follow Single Responsibility Principle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoStreamingService {

    private static final String CONTENT_TYPE_VIDEO_MP4 = "video/mp4";
    private static final String RANGE_HEADER_PREFIX = "bytes=";
    private static final String RANGE_HEADER_SEPARATOR = "-";

    /**
     * Prepares a video streaming response for the given session file.
     * Handles HTTP Range requests for partial content delivery.
     *
     * @param fileOptional the session file to stream
     * @param rangeHeader the HTTP Range header value (may be null)
     * @return ResponseEntity with appropriate headers and video content
     * @throws VideoStreamingException if file cannot be read
     */
    public ResponseEntity<?> prepareVideoStream(Optional<SessionFile> fileOptional, String rangeHeader) {
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SessionFile file = fileOptional.get();
        try {
            return buildVideoResponse(file, rangeHeader);
        } catch (IOException e) {
            log.error("Failed to stream video file: {}", file.getFileName(), e);
            throw new VideoStreamingException("Failed to stream video: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<?> buildVideoResponse(SessionFile file, String rangeHeader) throws IOException {
        Path filePath = Paths.get(file.getFilePath());
        org.springframework.core.io.Resource video = new org.springframework.core.io.UrlResource(filePath.toUri());

        if (!video.exists() || !video.isReadable()) {
            throw new VideoStreamingException("File is not readable: " + file.getFileName());
        }

        FileChannel fileChannel = FileChannel.open(filePath, java.nio.file.StandardOpenOption.READ);
        long fileLength = fileChannel.size();
        VideoRange range = parseRangeHeader(rangeHeader, fileLength);

        if (range.start > fileLength - 1 || range.end > fileLength - 1) {
            fileChannel.close();
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }

        HttpHeaders headers = buildHeaders(range, fileLength);
        InputStreamResource resource = new InputStreamResource(Channels.newInputStream(fileChannel.position(range.start)));

        HttpStatus status = rangeHeader != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status)
                .headers(headers)
                .body(resource);
    }

    /**
     * Parses HTTP Range header to extract start and end byte positions.
     *
     * @param rangeHeader the Range header value (e.g., "bytes=0-1023")
     * @param fileLength the total file length in bytes
     * @return VideoRange object with start and end positions
     */
    private VideoRange parseRangeHeader(String rangeHeader, long fileLength) {
        if (rangeHeader == null) {
            return new VideoRange(0, fileLength - 1);
        }

        try {
            String range = rangeHeader.replace(RANGE_HEADER_PREFIX, "");
            String[] ranges = range.split(RANGE_HEADER_SEPARATOR);
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileLength - 1;
            return new VideoRange(start, end);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.warn("Invalid range header: {}, returning full content", rangeHeader);
            return new VideoRange(0, fileLength - 1);
        }
    }

    private HttpHeaders buildHeaders(VideoRange range, long fileLength) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_VIDEO_MP4);

        if (range.start != 0 || range.end != fileLength - 1) {
            headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + range.start + "-" + range.end + "/" + fileLength);
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(range.end - range.start + 1));
        }

        return headers;
    }

    /**
     * Value object representing a byte range for video streaming.
     */
    private record VideoRange(long start, long end) {
    }
}
