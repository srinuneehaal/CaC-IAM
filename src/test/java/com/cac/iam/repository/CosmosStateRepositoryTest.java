package com.cac.iam.repository;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.cac.iam.config.JacksonConfiguration;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.StateDocument;
import com.cac.iam.model.StateSnapshot;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.identity.model.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosmosStateRepositoryTest {

    @Mock
    private CosmosContainer container;

    @Mock
    private CosmosItemResponse<StateDocument> itemResponse;

    private final ObjectMapper objectMapper = JacksonConfiguration.createObjectMapper();
    private CosmosStateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CosmosStateRepository(container, objectMapper, new LoggerProvider());
    }

    @Test
    void loadSnapshotReadsAllCategories() {
        StateDocument policy = document("p1", "{\"code\":\"p1\"}");
        StateDocument role = document("r1", "{\"code\":\"r1\"}");
        StateDocument user = document("u1", "{\"login\":\"u1\"}");

        when(container.readAllItems(new PartitionKey(FileCategory.POLICIES.name()), StateDocument.class))
                .thenReturn(iterable(List.of(policy)));
        when(container.readAllItems(new PartitionKey(FileCategory.ROLES.name()), StateDocument.class))
                .thenReturn(iterable(List.of(role)));
        when(container.readAllItems(new PartitionKey(FileCategory.USERS.name()), StateDocument.class))
                .thenReturn(iterable(List.of(user)));

        StateSnapshot snapshot = repository.loadSnapshot();

        assertThat(snapshot.getPolicies()).containsKey("p1");
        assertThat(snapshot.getRoles()).containsKey("r1");
        assertThat(snapshot.getUsers()).containsKey("u1");
    }

    @Test
    void findPayloadHandlesMissingIdAndErrors() {
        assertThat(repository.findPayload(FileCategory.POLICIES, " ", PolicyCreationRequest.class)).isEmpty();

        CosmosException notFound = mock(CosmosException.class);
        when(notFound.getStatusCode()).thenReturn(404);
        when(container.readItem(eq("absent"), any(PartitionKey.class), eq(StateDocument.class)))
                .thenThrow(notFound);
        assertThat(repository.findPayload(FileCategory.POLICIES, "absent", PolicyCreationRequest.class)).isEmpty();

        when(container.readItem(eq("fail"), any(PartitionKey.class), eq(StateDocument.class)))
                .thenThrow(new RuntimeException("boom"));
        assertThat(repository.findPayload(FileCategory.POLICIES, "fail", PolicyCreationRequest.class)).isEmpty();
    }

    @Test
    void findPayloadReturnsMappedPayload() {
        StateDocument document = document("p1", "{\"code\":\"p1\"}");
        when(itemResponse.getItem()).thenReturn(document);
        when(container.readItem(eq("p1"), any(PartitionKey.class), eq(StateDocument.class))).thenReturn(itemResponse);

        Optional<PolicyCreationRequest> payload =
                repository.findPayload(FileCategory.POLICIES, "p1", PolicyCreationRequest.class);

        assertThat(payload).isPresent();
        assertThat(payload.get().getCode()).isEqualTo("p1");
    }

    @Test
    void listKeysReturnsIdsAndHandlesErrors() {
        StateDocument doc = document("k1", "{\"code\":\"c\"}");
        when(container.queryItems(any(SqlQuerySpec.class), any(CosmosQueryRequestOptions.class), eq(StateDocument.class)))
                .thenReturn(iterable(List.of(doc)));

        assertThat(repository.listKeys(FileCategory.POLICIES)).containsExactly("k1");

        when(container.queryItems(any(SqlQuerySpec.class), any(CosmosQueryRequestOptions.class), eq(StateDocument.class)))
                .thenThrow(new RuntimeException("fail"));
        assertThat(repository.listKeys(FileCategory.POLICIES)).isEmpty();
    }

    @Test
    void upsertAndDeleteHandleHappyAndErrorPaths() {
        repository.upsert(FileCategory.POLICIES, "", new Object());
        verify(container, never()).upsertItem(any(StateDocument.class), any(), any(CosmosItemRequestOptions.class));

        repository.upsert(FileCategory.POLICIES, "p1", new PolicyCreationRequest().code("p1"));
        verify(container).upsertItem(any(StateDocument.class), any(PartitionKey.class), any(CosmosItemRequestOptions.class));

        repository.delete(FileCategory.POLICIES, "");
        verify(container, never()).deleteItem(anyString(), any(PartitionKey.class), any());

        CosmosException notFound = mock(CosmosException.class);
        when(notFound.getStatusCode()).thenReturn(404);
        doThrow(notFound).when(container).deleteItem(eq("missing"), any(PartitionKey.class), isNull());
        repository.delete(FileCategory.POLICIES, "missing");

        doThrow(new RuntimeException("boom")).when(container).deleteItem(eq("p1"), any(PartitionKey.class), isNull());
        repository.delete(FileCategory.POLICIES, "p1");
    }

    @Test
    void loadByCategoryMapsKeysAndSkipsBadPayloads() {
        StateDocument valid = document("fallback", "{\"description\":\"d\"}");
        StateDocument invalid = document("bad", "\"just-string\"");
        when(container.readAllItems(new PartitionKey(FileCategory.ROLES.name()), StateDocument.class))
                .thenReturn(iterable(List.of(valid, invalid)));

        var result = repository.loadByCategory(FileCategory.ROLES, RoleCreationRequest.class);

        assertThat(result).containsKey("fallback");
        assertThat(result).doesNotContainKey("bad");
    }

    @Test
    void loadByCategoryHandlesCosmosException() {
        CosmosException cosmosError = mock(CosmosException.class);
        when(container.readAllItems(new PartitionKey(FileCategory.USERS.name()), StateDocument.class))
                .thenThrow(cosmosError);

        assertThat(repository.loadByCategory(FileCategory.USERS, CreateUserRequest.class)).isEmpty();
    }

    private StateDocument document(String id, String json) {
        StateDocument doc = new StateDocument();
        doc.setId(id);
        doc.setTypeOfItem(FileCategory.POLICIES.name());
        try {
            doc.setData(objectMapper.readTree(json));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    private CosmosPagedIterable<StateDocument> iterable(List<StateDocument> docs) {
        return mock(CosmosPagedIterable.class, invocation -> {
            String name = invocation.getMethod().getName();
            if ("iterator".equals(name)) {
                return docs.iterator();
            }
            if ("spliterator".equals(name)) {
                return docs.spliterator();
            }
            return null;
        });
    }
}
