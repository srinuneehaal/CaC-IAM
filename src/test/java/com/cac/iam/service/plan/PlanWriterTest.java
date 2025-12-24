package com.cac.iam.service.plan;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.MasterPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class PlanWriterTest {

    @Test
    void writesPlanToConfiguredPath(@TempDir Path tempDir) throws Exception {
        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(tempDir.toString());
        props.setMasterPlanFile("masterplan.json");

        PlanWriter writer = new PlanWriter(props, new ObjectMapper());
        MasterPlan plan = new MasterPlan();

        Path written = writer.write(plan);

        assertThat(written).exists();
        assertThat(Files.readString(written)).contains("items");
    }

    @Test
    void wrapsIoException(@TempDir Path tempDir) throws Exception {
        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(tempDir.toString());
        props.setMasterPlanFile("masterplan.json");

        ObjectMapper mapper = mock(ObjectMapper.class);
        doThrow(new java.io.IOException("fail")).when(mapper).writeValue(any(File.class), any());

        PlanWriter writer = new PlanWriter(props, mapper);

        assertThatThrownBy(() -> writer.write(new MasterPlan()))
                .isInstanceOf(IllegalStateException.class);
    }
}
