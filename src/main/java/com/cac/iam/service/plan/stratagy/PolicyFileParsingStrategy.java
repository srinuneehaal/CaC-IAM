package com.cac.iam.service.plan.stratagy;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.cac.iam.util.PathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.PolicyCreationRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PolicyFileParsingStrategy implements FileParsingStrategy {

    private final FileLocationProperties fileLocationProperties;
    private final ObjectMapper objectMapper;

    public PolicyFileParsingStrategy(FileLocationProperties fileLocationProperties, ObjectMapper objectMapper) {
        this.fileLocationProperties = fileLocationProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public FileCategory getCategory() {
        return FileCategory.POLICIES;
    }

    @Override
    public boolean supports(Path path) {
        return path != null
                && path.toString().toLowerCase().endsWith(".json")
                && hasSegment(path, fileLocationProperties.getChangedFilesDir())
                && hasSegment(path, fileLocationProperties.getPoliciesDirName());
    }

    @Override
    public LoadedFile parse(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("Policy file does not exist: " + path);
        }
        PolicyCreationRequest payload = objectMapper.readValue(path.toFile(), PolicyCreationRequest.class);
        String key = payload.getCode();
        if (key == null || key.isBlank()) {
            key = PathUtils.baseName(path);
        }
        return new LoadedFile(getCategory(), key, path, payload);
    }

    private boolean hasSegment(Path path, String segment) {
        if (segment == null || segment.isBlank()) {
            return false;
        }
        for (Path part : path.normalize()) {
            if (segment.equalsIgnoreCase(part.toString())) {
                return true;
            }
        }
        return false;
    }
}
