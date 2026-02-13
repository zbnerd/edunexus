package com.edunexusgraphql.service.video;

import com.edunexusgraphql.util.http.HttpRangeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service for handling video streaming operations.
 * Extracts video streaming logic from controllers for better separation of concerns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoStreamingService {

    private final ResourceLoader resourceLoader;
    private static final String VIDEOS_LOCATION = "classpath:videos/";

    /**
     * Prepare a video for streaming with range support.
     *
     * @param filename Video filename
     * @param rangeHeader HTTP Range header (optional)
     * @return VideoStreamingResult with resource and appropriate headers
     * @throws IOException if video file cannot be loaded
     */
    public VideoStreamingResult prepareVideoStream(String filename, String rangeHeader) throws IOException {
        String videoPath = VIDEOS_LOCATION + filename;
        Resource videoResource = resourceLoader.getResource(videoPath);

        if (!videoResource.exists()) {
            log.warn("Video file not found: {}", videoPath);
            throw new IllegalArgumentException("Video not found: " + filename);
        }

        long fileSize = videoResource.contentLength();
        log.debug("Loading video: {}, size: {} bytes", filename, fileSize);

        HttpHeaders headers = new HttpHeaders();

        // Parse range header if present
        HttpRangeParser.VideoRangeInfo rangeInfo;
        if (rangeHeader != null && !rangeHeader.isEmpty()) {
            headers.set(HttpHeaders.RANGE, rangeHeader);
            rangeInfo = HttpRangeParser.parseRange(headers, fileSize);

            headers.set(HttpHeaders.CONTENT_RANGE, rangeInfo.toContentRangeHeader());
            headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeInfo.getContentLength()));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            // Create byte range input stream
            InputStream rangeStream = createByteRangeStream(videoResource, rangeInfo.getStart(), rangeInfo.getEnd());
            Resource rangeResource = new InputStreamResource(rangeStream, filename);

            return VideoStreamingResult.builder()
                    .videoResource(rangeResource)
                    .headers(headers)
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .contentLength(rangeInfo.getContentLength())
                    .build();
        } else {
            // Full content request
            headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            return VideoStreamingResult.builder()
                    .videoResource(videoResource)
                    .headers(headers)
                    .status(HttpStatus.OK)
                    .contentLength(fileSize)
                    .build();
        }
    }

    /**
     * Create an input stream that reads a specific byte range from a resource.
     *
     * @param resource The resource to read from
     * @param start Start byte position
     * @param end End byte position
     * @return Bounded input stream
     * @throws IOException if resource cannot be read
     */
    private InputStream createByteRangeStream(Resource resource, long start, long end) throws IOException {
        InputStream is = resource.getInputStream();
        is.skip(start);
        return new BoundedInputStream(is, end - start + 1);
    }

    /**
     * Input stream that limits reading to a specific number of bytes.
     */
    private static class BoundedInputStream extends java.io.FilterInputStream {
        private long remaining;

        public BoundedInputStream(InputStream in, long length) {
            super(in);
            this.remaining = length;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int result = super.read();
            if (result != -1) {
                remaining--;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int maxLen = (int) Math.min(len, remaining);
            int result = super.read(b, off, maxLen);
            if (result != -1) {
                remaining -= result;
            }
            return result;
        }

        @Override
        public long skip(long n) throws IOException {
            long maxSkip = Math.min(n, remaining);
            long skipped = super.skip(maxSkip);
            remaining -= skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(super.available(), remaining);
        }
    }
}
