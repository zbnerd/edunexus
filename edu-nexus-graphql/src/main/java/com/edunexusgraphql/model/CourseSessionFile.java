package com.edunexusgraphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSessionFile {
    private Long fileId;
    private Long courseSessionId;
    private CourseSession courseSession;
    private String fileName;
    private String fileType;
    private String filePath;
}
