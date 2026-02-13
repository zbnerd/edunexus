# File Management Service

## Overview

The File Management Service handles video file storage, streaming, and session file metadata for the EduNexus platform. It provides REST endpoints for file upload, retrieval, and video streaming capabilities.

## Features

- **File Upload**: Store video files for course sessions
- **File Metadata**: Track session files with database records
- **Video Streaming**: Stream video content to clients
- **File Management**: Retrieve and delete session files

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- MySQL 8.0 for metadata storage
- Local filesystem for video storage
- Multipart file upload support

### Architecture

```
edu-nexus-file-manage-service/
├── domain/
│   ├── controller/        # REST controllers
│   ├── service/          # Business logic
│   ├── entity/           # JPA entities
│   ├── repository/        # Repository interfaces
│   └── exception/        # Custom exceptions
└── infrastructure/         # Configuration (future)
```

## API Endpoints

### Session Files

#### Upload File
```http
POST /sessions/{sessionId}/files
Content-Type: multipart/form-data

file: <binary-video-data>
```

**Response:**
```json
{
  "fileId": 123,
  "sessionId": 1,
  "fileName": "lesson1.mp4",
  "filePath": "/uploads/session-1-lesson1.mp4",
  "fileSize": 52428800,
  "contentType": "video/mp4",
  "uploadedAt": "2024-01-15T10:30:00"
}
```

File is stored to filesystem and metadata saved to database.

#### Get Recent File by Session
```http
GET /sessions/{sessionId}/files
```

Returns the most recent file for a session (ordered by `fileId` descending).

**Response:** SessionFile entity or 404 if not found.

#### Get File by ID
```http
GET /sessions/{sessionId}/files/{fileId}
```

**Response:** SessionFile entity or 404 if not found.

#### Delete File
```http
DELETE /sessions/{sessionId}/files/{fileId}
```

**Response:** 204 No Content

Note: This only deletes the database record. File must be manually removed from filesystem.

### Video Streaming

#### Stream Video
```http
GET /stream/{sessionId}
Accept: video/mp4
Range: bytes=0-1024
```

Streams video file with support for:
- Partial content (Range requests for progressive loading)
- Content-Type headers
- Content-Length headers
- Accept-Ranges: bytes

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/next_file_manage
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  servlet:
    multipart:
      max-file-size: ${MAX_FILE_SIZE:500MB}
      max-request-size: ${MAX_FILE_SIZE:500MB}

file:
  upload-dir: ${UPLOAD_DIR:./uploads}
  allowed-extensions: mp4,webm,mkv,avi

server:
  port: 8003

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/
```

### Environment Variables

| Variable | Description | Default |
|-----------|-------------|----------|
| `DB_USERNAME` | Database username | Required |
| `DB_PASSWORD` | Database password | Required |
| `MAX_FILE_SIZE` | Maximum upload size | 500MB |
| `UPLOAD_DIR` | File storage location | ./uploads |

## Database Schema

### Session_File Table

| Column | Type | Description |
|---------|-------|-------------|
| file_id | BIGINT | Primary key |
| session_id | BIGINT | Associated course session |
| file_name | VARCHAR(255) | Original filename |
| file_path | VARCHAR(512) | Storage location |
| file_size | BIGINT | Size in bytes |
| content_type | VARCHAR(100) | MIME type |
| uploaded_at | TIMESTAMP | Upload timestamp |

## File Storage

### Directory Structure

```
uploads/
├── session-1-lesson1.mp4
├── session-1-lesson2.mp4
├── session-2-lecture1.mp4
└── ...
```

### Naming Convention

```
{sessionId}-{originalName}.{extension}
```

Example: `1-introduction-to-msa.mp4`

### Storage Considerations

- **No deduplication**: Same file can be uploaded multiple times
- **No cleanup**: Deleted database records don't remove files
- **No versioning**: New uploads replace old files for a session
- **Local storage**: Not suitable for distributed deployments

## Video Streaming

### Supported Features

- **Range Requests**: Partial content for HTML5 video players
- **Byte Serving**: Efficient streaming for large files
- **Content-Type Detection**: Based on file extension

### Client-Side Usage

```html
<video controls>
  <source src="http://localhost:8003/stream/1" type="video/mp4">
  Your browser does not support video.
</video>
```

## Exceptions

| Exception | HTTP Status | Description |
|-----------|--------------|-------------|
| `FileStorageException` | 500 Internal Error | Upload/storage failure |
| `NotFoundException` | 404 Not Found | File/session not found |

### FileStorageException Scenarios

- Disk full
- Permission denied
- Invalid file type
- File exceeds size limit

## Running Locally

```bash
# Create uploads directory
mkdir -p uploads

# Build service
./gradlew :edu-nexus-file-manage-service:build

# Run service
./gradlew :edu-nexus-file-manage-service:bootRun

# Run tests
./gradlew :edu-nexus-file-manage-service:test

# With custom upload directory
UPLOAD_DIR=/var/videos ./gradlew :edu-nexus-file-manage-service:bootRun
```

## Testing

### Upload File

```bash
# Upload video file
curl -X POST http://localhost:8003/sessions/1/files \
  -F "file=@/path/to/video.mp4"

# Check response
{
  "fileId": 123,
  "sessionId": 1,
  "fileName": "video.mp4",
  "filePath": "./uploads/1-video.mp4",
  "fileSize": 52428800
}
```

### Stream Video

```bash
# Stream first 1KB
curl -X GET http://localhost:8003/stream/1 \
  -H "Range: bytes=0-1023" \
  --output video-part.mp4

# Stream entire file
curl -X GET http://localhost:8003/stream/1 \
  -H "Accept: video/mp4" \
  --output video.mp4
```

### Get Metadata

```bash
# Get latest file for session
curl http://localhost:8003/sessions/1/files

# Get specific file
curl http://localhost:8003/sessions/1/files/123
```

### Delete File

```bash
# Delete file record
curl -X DELETE http://localhost:8003/sessions/1/files/123

# Note: File on disk must be manually removed
rm ./uploads/1-*.mp4
```

## Troubleshooting

### Upload Fails

1. Check disk space: `df -h`
2. Verify upload directory exists: `ls -la uploads/`
3. Check file size limits in application.yml
4. Review logs for `FileStorageException`

### Video Won't Stream

1. Verify file exists at expected path
2. Check Content-Type header matches video format
3. Ensure Range requests are supported (check HTML5 player)
4. Test with curl first before using video player

### File Not Deleted

File deletion only removes database record. Manual cleanup required:

```bash
# Find orphaned files
find uploads/ -name "*.mp4" -not -name "*-*"
# Or
find uploads/ -type f -name "*.mp4" | while read f; do
  id=$(basename "$f" | cut -d- -f1)
  if ! mysql -e "SELECT 1 FROM session_file WHERE session_id=$id"; then
    echo "Orphan: $f"
  fi
done
```

## Production Considerations

### Storage Recommendations

For production deployment, consider:

1. **Object Storage**: Use S3, GCS, or Azure Blob instead of local filesystem
2. **CDN Integration**: Serve videos through CloudFront or Cloud CDN
3. **Transcoding**: Support multiple resolutions (1080p, 720p, 480p)
4. **Thumbnail Generation**: Extract poster images for video previews
5. **Cleanup Job**: Scheduled task to remove orphaned files
6. **Distributed Storage**: Shared filesystem or object storage for multi-instance deployments

### Security Enhancements

- Virus scanning on upload
- File type validation (not just extension)
- Signed URLs for private content
- Rate limiting on upload endpoints
- Authentication/authorization checks

## Future Enhancements

- [ ] Video transcoding service integration
- [ ] Adaptive bitrate streaming (HLS/DASH)
- [ ] Thumbnail generation
- [ ] Video metadata extraction (duration, resolution)
- [ ] Multi-language subtitle support
- [ ] Chunked upload for large files
- [ ] Upload progress tracking via WebSocket

## Dependencies

- **edu-nexus-common**: Shared exceptions
- Spring Boot DevTools for hot reload (development only)
