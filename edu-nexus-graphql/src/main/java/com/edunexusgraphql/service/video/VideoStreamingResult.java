package com.edunexusgraphql.service.video;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Value object representing the result of a video streaming request.
 * Encapsulates the video resource, HTTP headers, and status code.
 */
public class VideoStreamingResult {

    private final Resource videoResource;
    private final HttpHeaders headers;
    private final HttpStatus status;
    private final long contentLength;

    private VideoStreamingResult(Builder builder) {
        this.videoResource = builder.videoResource;
        this.headers = builder.headers;
        this.status = builder.status;
        this.contentLength = builder.contentLength;
    }

    public Resource getVideoResource() {
        return videoResource;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public long getContentLength() {
        return contentLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Resource videoResource;
        private HttpHeaders headers;
        private HttpStatus status;
        private long contentLength;

        public Builder videoResource(Resource videoResource) {
            this.videoResource = videoResource;
            return this;
        }

        public Builder headers(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public VideoStreamingResult build() {
            if (videoResource == null) {
                throw new IllegalStateException("videoResource is required");
            }
            if (headers == null) {
                this.headers = new HttpHeaders();
            }
            if (status == null) {
                this.status = HttpStatus.OK;
            }
            return new VideoStreamingResult(this);
        }
    }

    @Override
    public String toString() {
        return "VideoStreamingResult{" +
                "status=" + status +
                ", contentLength=" + contentLength +
                ", headers=" + headers +
                '}';
    }
}
