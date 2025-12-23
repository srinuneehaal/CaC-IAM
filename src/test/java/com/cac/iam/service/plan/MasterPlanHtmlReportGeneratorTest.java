package com.cac.iam.service.plan;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.MasterPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MasterPlanHtmlReportGeneratorTest {

    @Test
    void generatesReportFromTemplate(@TempDir Path tempDir) throws Exception {
        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(tempDir.toString());
        props.setMasterPlanFile("masterplan.json");

        MasterPlanHtmlReportGenerator generator =
                new MasterPlanHtmlReportGenerator(props, new ObjectMapper());

        MasterPlan plan = new MasterPlan();
        Path report = generator.generateReport(plan);

        assertThat(report).exists();
        String html = Files.readString(report);
        assertThat(html).contains("Master Plan Report");
    }

    @Test
    void wrapsWriteFailures(@TempDir Path tempDir) throws Exception {
        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(tempDir.toString());
        props.setMasterPlanFile("masterplan.json");

        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("fail") {});

        MasterPlanHtmlReportGenerator generator = new MasterPlanHtmlReportGenerator(props, mapper);

        assertThatThrownBy(() -> generator.generateReport(new MasterPlan()))
                .isInstanceOf(IllegalStateException.class);
    }
}
