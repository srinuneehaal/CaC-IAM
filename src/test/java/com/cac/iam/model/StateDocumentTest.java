package com.cac.iam.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateDocumentTest {

    @Test
    void gettersAndSetters() {
        StateDocument doc = new StateDocument();
        ObjectNode data = new ObjectMapper().createObjectNode().put("k", "v");

        doc.setId("id1");
        doc.setTypeOfItem("POLICIES");
        doc.setData(data);

        assertThat(doc.getId()).isEqualTo("id1");
        assertThat(doc.getTypeOfItem()).isEqualTo("POLICIES");
        assertThat(doc.getData()).isEqualTo(data);
    }
}
