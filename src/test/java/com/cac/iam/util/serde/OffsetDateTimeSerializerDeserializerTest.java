package com.cac.iam.util.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OffsetDateTimeSerializerDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                    .addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer())
                    .addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer()));

    @Test
    void serializesAndDeserializesRoundTrip() throws JsonProcessingException {
        OffsetDateTime now = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        String json = mapper.writeValueAsString(now);
        OffsetDateTime decoded = mapper.readValue(json, OffsetDateTime.class);
        assertThat(decoded).isEqualTo(now);
    }

    @Test
    void deserializerReturnsNullOnNullToken() throws IOException {
        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // move to VALUE_NULL
        OffsetDateTimeDeserializer deserializer = new OffsetDateTimeDeserializer();
        assertThat(deserializer.deserialize(parser, mapper.getDeserializationContext())).isNull();
    }

    @Test
    void deserializerReturnsNullOnBlankOrLiteralNull() throws IOException {
        OffsetDateTimeDeserializer deserializer = new OffsetDateTimeDeserializer();

        JsonParser blankParser = mapper.getFactory().createParser("\"  \"");
        blankParser.nextToken();
        assertThat(deserializer.deserialize(blankParser, mapper.getDeserializationContext())).isNull();

        JsonParser textNullParser = mapper.getFactory().createParser("\"null\"");
        textNullParser.nextToken();
        assertThat(deserializer.deserialize(textNullParser, mapper.getDeserializationContext())).isNull();
    }

    @Test
    void serializerHandlesNullValues() throws IOException {
        OffsetDateTimeSerializer serializer = new OffsetDateTimeSerializer();
        com.fasterxml.jackson.core.JsonGenerator generator = mock(com.fasterxml.jackson.core.JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);

        serializer.serialize(null, generator, provider);

        verify(provider).defaultSerializeNull(generator);
    }
}
