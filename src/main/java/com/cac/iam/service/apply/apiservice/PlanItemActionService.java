package com.cac.iam.service.apply.apiservice;

public interface PlanItemActionService<T> {

    /**
     * Creates a resource for the given scope and key using the supplied payload.
     *
     * @param scope   scope value
     * @param key     logical key
     * @param payload request payload
     */
    void create(String scope, String key, T payload);

    /**
     * Updates a resource for the given scope and key using the supplied payload.
     *
     * @param scope   scope value
     * @param key     logical key
     * @param payload request payload
     */
    void update(String scope, String key, T payload);

    /**
     * Deletes a resource for the given scope and key.
     *
     * @param scope   scope value
     * @param key     logical key
     * @param payload original payload (when relevant)
     */
    void delete(String scope, String key, T payload);
}
