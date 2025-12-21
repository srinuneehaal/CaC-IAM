package com.cac.iam.service.plan;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.MasterPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Produces a standalone HTML report from the generated {@link MasterPlan}.
 */
@Component
public class MasterPlanHtmlReportGenerator {

    private static final String REPORT_FILE_NAME = "masterplan.html";
    private static final String TEMPLATE_PATH = "masterplan-report-template.html";

    private final FileLocationProperties fileLocationProperties;
    private final ObjectMapper objectMapper;
    private final String template;

    public MasterPlanHtmlReportGenerator(FileLocationProperties fileLocationProperties, ObjectMapper objectMapper) {
        this.fileLocationProperties = fileLocationProperties;
        this.objectMapper = objectMapper;
        this.template = loadTemplate();
    }

    /**
     * Writes the HTML report next to the JSON master plan.
     *
     * @param masterPlan master plan data
     * @return written report path
     */
    public Path generateReport(MasterPlan masterPlan) {
        Path reportPath = fileLocationProperties.planDirPath().resolve(REPORT_FILE_NAME);
        try {
            Files.createDirectories(reportPath.getParent());
            String json = objectMapper.writeValueAsString(masterPlan);
            Files.writeString(reportPath, template.replace("{{PLAN_DATA}}", json));
            return reportPath;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write master plan HTML report", e);
        }
    }

    private String loadTemplate() {
        try (InputStream stream = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load master plan report template", e);
        }
    }
}
