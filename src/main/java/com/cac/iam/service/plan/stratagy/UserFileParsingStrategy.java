package com.cac.iam.service.plan.stratagy;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.cac.iam.util.PathUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class UserFileParsingStrategy implements FileParsingStrategy {

    private final FileLocationProperties fileLocationProperties;
    private final ObjectMapper objectMapper;

    public UserFileParsingStrategy(FileLocationProperties fileLocationProperties, ObjectMapper objectMapper) {
        this.fileLocationProperties = fileLocationProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public FileCategory getCategory() {
        return FileCategory.USERS;
    }

    @Override
    public boolean supports(Path path) {
        return path != null
                && path.toString().toLowerCase().endsWith(".json")
                && hasSegment(path, fileLocationProperties.getChangedFilesDir())
                && hasSegment(path, fileLocationProperties.getUsersDirName());
    }

    @Override
    public LoadedFile parse(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("User file does not exist: " + path);
        }
        JsonNode json = objectMapper.readTree(path.toFile());
        ObjectNode payload = json.isObject() ? (ObjectNode) json : objectMapper.createObjectNode();
        String key = extractKey(payload);
        if (key == null || key.isBlank()) {
            key = PathUtils.baseName(path);
        }
        return new LoadedFile(getCategory(), key, path, payload);
    }

    private String extractKey(ObjectNode payload) {
        if (payload == null) {
            return null;
        }
        JsonNode login = payload.get("login");
        if (login != null && login.isTextual()) {
            return login.asText();
        }
        JsonNode email = payload.get("emailAddress");
        if (email != null && email.isTextual()) {
            return email.asText();
        }
        return null;
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
