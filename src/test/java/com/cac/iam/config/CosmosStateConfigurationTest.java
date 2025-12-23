package com.cac.iam.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CosmosStateConfigurationTest {

    @Test
    void buildsCosmosClientUsingBuilder() {
        CosmosClient client = mock(CosmosClient.class);
        CosmosClientBuilder builder = mock(CosmosClientBuilder.class);
        when(builder.endpoint(any(String.class))).thenReturn(builder);
        when(builder.key(any(String.class))).thenReturn(builder);
        when(builder.consistencyLevel(any())).thenReturn(builder);
        when(builder.contentResponseOnWriteEnabled(true)).thenReturn(builder);
        when(builder.buildClient()).thenReturn(client);

        CosmosStateConfiguration configuration = new CosmosStateConfiguration() {
            @Override
            CosmosClientBuilder cosmosClientBuilder() {
                return builder;
            }
        };
        CosmosStateProperties props = new CosmosStateProperties();
        props.setUri("uri");
        props.setKey("key");

        CosmosClient result = configuration.cosmosClient(props);

        assertThat(result).isSameAs(client);
        verify(builder).endpoint("uri");
        verify(builder).key("key");
    }

    @Test
    void buildsContainerFromDatabase() {
        CosmosClient client = mock(CosmosClient.class);
        CosmosDatabase database = mock(CosmosDatabase.class);
        CosmosContainer container = mock(CosmosContainer.class);
        when(client.getDatabase("db")).thenReturn(database);
        when(database.getContainer("container")).thenReturn(container);

        CosmosStateConfiguration configuration = new CosmosStateConfiguration();
        CosmosStateProperties props = new CosmosStateProperties();
        props.setDatabase("db");
        props.setContainer("container");

        CosmosContainer result = configuration.stateContainer(client, props);

        assertThat(result).isSameAs(container);
        verify(client).getDatabase("db");
        verify(database).getContainer("container");
    }
}
