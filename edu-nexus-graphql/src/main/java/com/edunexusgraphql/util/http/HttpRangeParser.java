package com.edunexusgraphql.util.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;

/**
 * Utility class for parsing HTTP Range headers.
 * Extracts range information for video streaming requests.
 */
public class HttpRangeParser {

    private static final String RANGE_PREFIX = "bytes=";
    private static final long DEFAULT_CHUNK_SIZE = 1024 * 1024; // 1MB

    /**
     * Parse HTTP Range header to extract range information.
     *
     * @param headers HTTP headers containing Range header
     * @param fileSize Total file size in bytes
     * @return Parsed range information
     */
    public static VideoRangeInfo parseRange(HttpHeaders headers, long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive: " + fileSize);
        }

        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);
        if (rangeHeader == null || !rangeHeader.startsWith(RANGE_PREFIX)) {
            return new VideoRangeInfo(0, fileSize - 1, fileSize, false);
        }

        String rangeValue = rangeHeader.substring(RANGE_PREFIX.length());
        String[] ranges = rangeValue.split("-");

        long start;
        long end;

        if (ranges.length == 0 || ranges[0].isEmpty()) {
            // Request is from suffix: "bytes=-500"
            start = Math.max(0, fileSize - Long.parseLong(ranges[1]));
            end = fileSize - 1;
        } else {
            start = Long.parseLong(ranges[0]);
            if (ranges.length == 1 || ranges[1].isEmpty()) {
                // Request is from start: "bytes=1000-"
                end = fileSize - 1;
            } else {
                // Request is range: "bytes=1000-2000"
                end = Long.parseLong(ranges[1]);
            }
        }

        // Validate range
        if (start < 0) {
            start = 0;
        }
        if (end >= fileSize) {
            end = fileSize - 1;
        }
        if (start > end) {
            start = 0;
            end = fileSize - 1;
        }

        return new VideoRangeInfo(start, end, fileSize, true);
    }

    /**
     * Value object representing parsed HTTP range information.
     */
    public static class VideoRangeInfo {
        private final long start;
        private final long end;
        private final long fileSize;
        private final boolean rangeRequested;

        public VideoRangeInfo(long start, long end, long fileSize, boolean rangeRequested) {
            this.start = start;
            this.end = end;
            this.fileSize = fileSize;
            this.rangeRequested = rangeRequested;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public long getFileSize() {
            return fileSize;
        }

        public long getContentLength() {
            return end - start + 1;
        }

        public boolean isRangeRequested() {
            return rangeRequested;
        }

        public String toContentRangeHeader() {
            return String.format("bytes %d-%d/%d", start, end, fileSize);
        }

        @Override
        public String toString() {
            return "VideoRangeInfo{" +
                    "start=" + start +
                    ", end=" + end +
                    ", fileSize=" + fileSize +
                    ", contentLength=" + getContentLength() +
                    ", rangeRequested=" + rangeRequested +
                    '}';
        }
    }
}
