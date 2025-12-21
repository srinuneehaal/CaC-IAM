package com.cac.iam.config;

import com.azure.cosmos.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates Cosmos DB client and container beans used for state persistence.
 */
@Configuration
public class CosmosStateConfiguration {

    @Bean(destroyMethod = "close")
    public CosmosClient cosmosClient(CosmosStateProperties properties) {
        return new CosmosClientBuilder()
                .endpoint(properties.getUri())
                .key(properties.getKey())
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true)
                .buildClient();
    }

    @Bean
    public CosmosContainer stateContainer(CosmosClient client, CosmosStateProperties properties) {
        CosmosDatabase database = client.getDatabase(properties.getDatabase());
        return database.getContainer(properties.getContainer());
    }
}
