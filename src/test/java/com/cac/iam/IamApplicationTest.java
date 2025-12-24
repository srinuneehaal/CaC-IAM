package com.cac.iam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class IamApplicationTest {

    @Test
    void mainStartsSpringApplication() {
        try (var app = mockStatic(SpringApplication.class)) {
            app.when(() -> SpringApplication.run(IamApplication.class, new String[]{})).thenReturn(null);

            IamApplication.main(new String[]{});

            app.verify(() -> SpringApplication.run(IamApplication.class, new String[]{}));
        }
    }
}
