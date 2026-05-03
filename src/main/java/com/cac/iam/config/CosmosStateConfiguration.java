package com.cac.iam.config;

import com.azure.cosmos.*;
import com.azure.cosmos.models.PriorityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Creates Cosmos DB client and container beans used for state persistence.
 */
@Configuration
public class CosmosStateConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CosmosStateConfiguration.class);

    @Bean(destroyMethod = "close")
    public CosmosClient cosmosClient(CosmosStateProperties properties) {
        return cosmosClientBuilder()
                .endpoint(properties.getUri())
                .key(properties.getKey())
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true)
                .buildClient();
    }

    @Bean
    public CosmosContainer stateContainer(CosmosClient client, CosmosStateProperties properties) {
        CosmosDatabase database = client.getDatabase(properties.getDatabase());
        CosmosContainer container = database.getContainer(properties.getContainer());
        ThroughputControlGroupConfig groupConfig = buildThroughputControlGroupConfig(properties);
        if (groupConfig != null) {
            container.enableLocalThroughputControlGroup(groupConfig);
        }
        return container;
    }

    CosmosClientBuilder cosmosClientBuilder() {
        return new CosmosClientBuilder();
    }

    private ThroughputControlGroupConfig buildThroughputControlGroupConfig(CosmosStateProperties properties) {
        String groupName = properties.getThroughputControlGroupName();
        if (!StringUtils.hasText(groupName)) {
            return null;
        }
        Integer targetThroughput = properties.getThroughputControlTargetThroughput();
        Double targetThroughputThreshold = properties.getThroughputControlTargetThroughputThreshold();
        if (targetThroughput == null && targetThroughputThreshold == null) {
            log.warn("Throughput control group {} configured without throughput or threshold; skipping.", groupName);
            return null;
        }
        if (targetThroughput != null && targetThroughputThreshold != null) {
            log.warn("Both throughput control target throughput and threshold set for {}. Using target throughput.", groupName);
        }
        ThroughputControlGroupConfigBuilder builder = new ThroughputControlGroupConfigBuilder()
                .groupName(groupName)
                .priorityLevel(PriorityLevel.HIGH)
                .defaultControlGroup(properties.isThroughputControlDefaultGroup())
                .continueOnInitError(properties.isThroughputControlContinueOnInitError());
        if (targetThroughput != null) {
            builder.targetThroughput(targetThroughput);
        } else {
            builder.targetThroughputThreshold(targetThroughputThreshold);
        }
        return builder.build();
    }
}
