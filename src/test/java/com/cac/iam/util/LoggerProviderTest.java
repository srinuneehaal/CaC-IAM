package com.cac.iam.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class LoggerProviderTest {

    @Test
    void returnsLoggerForClass() {
        LoggerProvider provider = new LoggerProvider();

        Logger logger = provider.getLogger(getClass());

        assertThat(logger).isNotNull();
        assertThat(logger.getName()).contains("LoggerProviderTest");
    }
}
