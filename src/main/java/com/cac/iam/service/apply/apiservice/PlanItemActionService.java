package com.cac.iam.service.apply.apiservice;

public interface PlanItemActionService<T> {

    /**
     * Creates a resource for the given key using the supplied payload.
     *
     * @param key     logical key
     * @param payload request payload
     */
    void create(String key, T payload);

    /**
     * Updates a resource for the given key using the supplied payload.
     *
     * @param key     logical key
     * @param payload request payload
     */
    void update(String key, T payload);

    /**
     * Deletes a resource for the given key.
     *
     * @param key     logical key
     * @param payload original payload (when relevant)
     */
    void delete(String key, T payload);
}
