package com.github.f1xman.statefun.tsukuyomi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerDeTest {

    static final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void serializesFoo() throws JsonProcessingException {
        Foo foo = new Foo("foobarbaz");
        byte[] expected = mapper.writeValueAsBytes(foo);

        byte[] actual = SerDe.serialize(foo);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void deserializesFoo() throws JsonProcessingException {
        Foo expected = new Foo("foobarbaz");
        byte[] bytes = mapper.writeValueAsBytes(expected);

        Foo actual = SerDe.deserialize(bytes, Foo.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static final class Foo {

        private String bar;

    }
}