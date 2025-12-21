package com.cac.iam.service.plan;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.MasterPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PlanWriter {

    private final FileLocationProperties fileLocationProperties;
    private final ObjectMapper objectMapper;

    public PlanWriter(FileLocationProperties fileLocationProperties, ObjectMapper objectMapper) {
        this.fileLocationProperties = fileLocationProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Writes the given master plan to the configured output path.
     *
     * @param masterPlan plan to persist
     * @return path to the written file
     * @throws IllegalStateException if writing fails
     */
    public Path write(MasterPlan masterPlan) {
        Path outputPath = fileLocationProperties.masterPlanPath();
        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(outputPath.toFile(), masterPlan);
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write plan file", e);
        }
    }
}
