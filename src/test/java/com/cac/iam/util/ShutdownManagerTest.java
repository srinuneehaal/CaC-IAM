package com.cac.iam.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShutdownManagerTest {

    @Test
    void noOpDoesNothing() {
        ShutdownManager manager = ShutdownManager.noOp();
        manager.shutdown(5); // should be a no-op (nothing to assert)
    }

    @Test
    void usesExitStrategyWithComputedCode() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        try (var mockedSpringExit = mockStatic(SpringApplication.class)) {
            mockedSpringExit.when(() -> SpringApplication.exit(eq(context), any())).thenReturn(7);
            var exitRecorder = new int[1];

            ShutdownManager manager = new ShutdownManager(context, true, code -> exitRecorder[0] = code);
            manager.shutdown(3); // requested 3, mocked SpringApplication.exit returns 7

            mockedSpringExit.verify(() -> SpringApplication.exit(eq(context), any()));
            assertThat(exitRecorder[0]).isEqualTo(7);
        }
    }

    @Test
    void fallsBackToRequestedCodeWhenContextNull() {
        var exitRecorder = new int[1];
        ShutdownManager manager = new ShutdownManager(null, true, code -> exitRecorder[0] = code);

        manager.shutdown(9);

        assertThat(exitRecorder[0]).isEqualTo(9);
    }
}
