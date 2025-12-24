package com.cac.iam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigurationTest {

    @Test
    void objectMapperConfiguredWithCustomModules() throws Exception {
        JacksonConfiguration configuration = new JacksonConfiguration();

        ObjectMapper mapper = configuration.objectMapper();

        assertThat(mapper.getRegisteredModuleIds()).isNotEmpty();
        assertThat(mapper.canSerialize(OffsetDateTime.class)).isTrue();
        String json = mapper.writeValueAsString(OffsetDateTime.now());
        OffsetDateTime parsed = mapper.readValue(json, OffsetDateTime.class);
        assertThat(parsed).isNotNull();
        assertThat(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
    }
}
