package com.cac.iam.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CosmosStatePropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationFailsWhenRequiredBlank() {
        CosmosStateProperties props = new CosmosStateProperties();
        props.setUri("");
        props.setKey("");
        props.setDatabase("");
        props.setContainer("");
        props.setPartitionKey("");

        Set<ConstraintViolation<CosmosStateProperties>> violations = validator.validate(props);

        assertThat(violations).hasSize(5);
    }
}
