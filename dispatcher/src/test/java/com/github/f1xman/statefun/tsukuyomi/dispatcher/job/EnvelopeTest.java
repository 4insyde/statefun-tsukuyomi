package com.github.f1xman.statefun.tsukuyomi.dispatcher.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeTest {

    @Test
    void serializes() throws JsonProcessingException {
        Envelope envelope = new Envelope(
                new Envelope.NodeAddress("foo/from", "bar", Envelope.NodeAddress.Type.FUNCTION),
                new Envelope.NodeAddress("foo/to", "baz", Envelope.NodeAddress.Type.FUNCTION),
                new Envelope.Data("foo/data", "foobarbaz"),
                Duration.ofHours(1)
        );
        String expected = JsonMapper.builder().addModule(new JavaTimeModule()).build().writeValueAsString(envelope);

        String actual = envelope.toJson();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void deserializes() throws JsonProcessingException {
        Envelope expected = new Envelope(
                new Envelope.NodeAddress("foo/from", "bar", Envelope.NodeAddress.Type.FUNCTION),
                new Envelope.NodeAddress("foo/to", "baz", Envelope.NodeAddress.Type.FUNCTION),
                new Envelope.Data("foo/data", "foobarbaz"),
                Duration.ofHours(1)
        );
        String json = JsonMapper.builder().addModule(new JavaTimeModule()).build().writeValueAsString(expected);

        Envelope actual = Envelope.fromJson(json);

        assertThat(actual).isEqualTo(expected);
    }
}