package com.cac.iam.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FileLocationPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationFailsWhenRequiredBlank() {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("");
        props.setPlanDir("");
        props.setMasterPlanFile("");

        Set<ConstraintViolation<FileLocationProperties>> violations = validator.validate(props);

        assertThat(violations).hasSize(3);
    }

    @Test
    void resolvesPathsWithOverrides() {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setPlanDir("plan");
        props.setMasterPlanFile("masterplan.json");

        assertThat(props.changedFilesRoot().toString()).contains("changed");
        assertThat(props.planDirPath().toString()).contains("plan");
        assertThat(props.masterPlanPath().toString()).endsWith("plan\\masterplan.json");

        props.setEnvLookup(key -> key.equals("PLAN_DIR") ? "overridePlan" : "overrideFile");
        assertThat(props.planDirPath().toString()).contains("overridePlan");
        assertThat(props.masterPlanPath().toString()).contains("overrideFile");
    }
}
