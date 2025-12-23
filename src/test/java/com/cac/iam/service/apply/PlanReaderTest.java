package com.cac.iam.service.apply;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanReaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoggerProvider loggerProvider = new LoggerProvider();

    @Test
    void readsPlanAndConvertsPayloads(@TempDir Path tempDir) throws IOException {
        Path planDir = Files.createDirectories(tempDir.resolve("plan"));
        Path planFile = planDir.resolve("masterplan.json");
        String json = """
                {"items":[
                  {"action":"NEW","fileCategory":"POLICIES","key":"p1","sourcePath":"s1","payload":{"code":"c1"}},
                  {"action":"NEW","fileCategory":"USERS","key":"u1","sourcePath":"s2","payload":{"login":"u1","emailAddress":"u1@example.com"}}
                ]}
                """;
        Files.writeString(planFile, json);

        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(planDir.toString());
        props.setMasterPlanFile("masterplan.json");

        PlanReader reader = new PlanReader(props, objectMapper, loggerProvider);

        MasterPlan plan = reader.read();

        assertThat(plan.getItems()).hasSize(2);
        PlanItem policy = plan.getItems().getFirst();
        assertThat(policy.getKey()).isEqualTo("p1");
        assertThat(policy.getPayload()).isNotNull();
        PlanItem user = plan.getItems().get(1);
        assertThat(user.getKey()).isEqualTo("u1");
        assertThat(user.getPayload()).isNotNull();
    }

    @Test
    void returnsEmptyPlanWhenItemsMissing(@TempDir Path tempDir) throws IOException {
        Path planDir = Files.createDirectories(tempDir.resolve("plan"));
        Path planFile = planDir.resolve("masterplan.json");
        Files.writeString(planFile, "{}");

        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(planDir.toString());
        props.setMasterPlanFile("masterplan.json");

        PlanReader reader = new PlanReader(props, objectMapper, loggerProvider);

        MasterPlan plan = reader.read();

        assertThat(plan.getItems()).isEmpty();
    }

    @Test
    void allowsNullPayloads(@TempDir Path tempDir) throws IOException {
        Path planDir = Files.createDirectories(tempDir.resolve("plan"));
        Path planFile = planDir.resolve("masterplan.json");
        Files.writeString(planFile, """
                {"items":[{"action":"NEW","fileCategory":null,"key":"k","sourcePath":"s","payload":null}]}
                """);

        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(planDir.toString());
        props.setMasterPlanFile("masterplan.json");

        PlanReader reader = new PlanReader(props, objectMapper, loggerProvider);

        MasterPlan plan = reader.read();

        assertThat(plan.getItems()).hasSize(1);
        assertThat(plan.getItems().getFirst().getPayload()).isNull();
    }

    @Test
    void throwsWhenFileMissing(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setPlanDir(tempDir.toString());
        props.setMasterPlanFile("missing.json");

        PlanReader reader = new PlanReader(props, objectMapper, loggerProvider);

        assertThatThrownBy(reader::read).isInstanceOf(IllegalStateException.class);
    }
}
