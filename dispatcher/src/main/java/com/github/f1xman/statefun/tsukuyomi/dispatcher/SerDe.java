package com.github.f1xman.statefun.tsukuyomi.dispatcher;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;

public class SerDe {

    private static final JsonMapper mapper = JsonMapper.builder().build();

    @SneakyThrows
    public static <T> byte[] serialize(T input) {
        return mapper.writeValueAsBytes(input);
    }

    @SneakyThrows
    public static <T> String serializeAsString(T input) {
        return mapper.writeValueAsString(input);
    }

    @SneakyThrows
    public static <T> T deserialize(byte[] raw, Class<T> type) {
        return mapper.readValue(raw, type);
    }

    @SneakyThrows
    public static <T> T deserialize(String json, Class<T> type) {
        return mapper.readValue(json, type);
    }
}
