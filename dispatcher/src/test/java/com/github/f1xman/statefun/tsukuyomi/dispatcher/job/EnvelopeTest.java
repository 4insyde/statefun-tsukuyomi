package com.github.f1xman.statefun.tsukuyomi.dispatcher.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeTest {

    @Test
    void serializes() throws JsonProcessingException {
        Envelope envelope = new Envelope(
                new Envelope.NodeAddress("foo/from", "bar"),
                new Envelope.NodeAddress("foo/to", "baz"),
                new Envelope.Data("foo/data", "foobarbaz")
        );
        String expected = JsonMapper.builder().build().writeValueAsString(envelope);

        String actual = envelope.toJson();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void deserializes() throws JsonProcessingException {
        Envelope expected = new Envelope(
                new Envelope.NodeAddress("foo/from", "bar"),
                new Envelope.NodeAddress("foo/to", "baz"),
                new Envelope.Data("foo/data", "foobarbaz")
        );
        String json = JsonMapper.builder().build().writeValueAsString(expected);

        Envelope actual = Envelope.fromJson(json);

        assertThat(actual).isEqualTo(expected);
    }
}