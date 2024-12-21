package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.model.CourseSessionFile;
import com.edunexusgraphql.service.dummy.DummyFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourseSessionDataResolver {
    private final DummyFileService fileService;

    @SchemaMapping(typeName = "CourseSession", field = "files")
    public List<CourseSessionFile> getFiles(CourseSession courseSession) {
        return fileService.findFilesBySessionId(courseSession.getId());
    }
}
