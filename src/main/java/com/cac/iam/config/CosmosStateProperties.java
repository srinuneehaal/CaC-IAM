package com.cac.iam.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Cosmos DB state persistence.
 */
@ConfigurationProperties(prefix = "azure.cosmos")
@Validated
public class CosmosStateProperties {

    @NotBlank
    private String uri;

    @NotBlank
    private String key;

    @NotBlank
    private String database;

    @NotBlank
    private String container;

    @NotBlank
    private String partitionKey = "/typeOfItem";

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }
}
