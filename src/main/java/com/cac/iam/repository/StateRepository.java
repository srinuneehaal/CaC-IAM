package com.cac.iam.repository;

import com.cac.iam.model.FileCategory;
import com.cac.iam.model.StateSnapshot;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for state persistence operations so implementations can be swapped or mocked easily.
 */
public interface StateRepository {

    StateSnapshot loadSnapshot();

    void upsert(FileCategory category, String id, Object payload);

    void delete(FileCategory category, String id);

    <T> Optional<T> findPayload(FileCategory category, String id, Class<T> payloadType);

    List<String> listKeys(FileCategory category);
}
