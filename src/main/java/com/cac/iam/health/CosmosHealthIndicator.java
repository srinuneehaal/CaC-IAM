package com.cac.iam.health;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Simple Cosmos DB health indicator that issues a lightweight query.
 */
@Component
public class CosmosHealthIndicator implements HealthIndicator {

    private final CosmosContainer container;

    public CosmosHealthIndicator(CosmosContainer container) {
        this.container = container;
    }

    @Override
    public Health health() {
        try {
            container.queryItems("SELECT TOP 1 * FROM c",
                    new CosmosQueryRequestOptions(), JsonNode.class).iterator().hasNext();
            return Health.up().build();
        } catch (CosmosException e) {
            return Health.down(e)
                    .withDetail("statusCode", e.getStatusCode())
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
