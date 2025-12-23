package com.cac.iam.health;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CosmosHealthIndicatorTest {

    @Test
    void healthIsUpWhenQuerySucceeds() {
        CosmosContainer container = mock(CosmosContainer.class);
        var iterable = mock(com.azure.cosmos.util.CosmosPagedIterable.class);
        when(container.queryItems(any(String.class), any(CosmosQueryRequestOptions.class), any(Class.class)))
                .thenReturn(iterable);
        when(iterable.iterator()).thenReturn(Collections.<JsonNode>emptyIterator());

        CosmosHealthIndicator indicator = new CosmosHealthIndicator(container);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void healthIsDownWhenQueryFails() {
        CosmosContainer container = mock(CosmosContainer.class);
        CosmosException cosmosException = mock(CosmosException.class);
        when(cosmosException.getStatusCode()).thenReturn(503);
        when(container.queryItems(any(String.class), any(CosmosQueryRequestOptions.class), any(Class.class)))
                .thenThrow(cosmosException);

        CosmosHealthIndicator indicator = new CosmosHealthIndicator(container);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("DOWN");
    }

    @Test
    void healthIsDownOnUnexpectedException() {
        CosmosContainer container = mock(CosmosContainer.class);
        when(container.queryItems(any(String.class), any(CosmosQueryRequestOptions.class), any(Class.class)))
                .thenThrow(new RuntimeException("boom"));

        CosmosHealthIndicator indicator = new CosmosHealthIndicator(container);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("DOWN");
    }
}
