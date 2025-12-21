package com.cac.iam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Cosmos DB state persistence.
 */
@ConfigurationProperties(prefix = "azure.cosmos")
public class CosmosStateProperties {

    private String uri;

    private String key;

    private String database;

    private String container;

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
