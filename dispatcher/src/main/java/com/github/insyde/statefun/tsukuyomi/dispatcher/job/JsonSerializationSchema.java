package com.github.insyde.statefun.tsukuyomi.dispatcher.job;

import com.github.insyde.statefun.tsukuyomi.dispatcher.SerDe;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class JsonSerializationSchema<T> implements SerializationSchema<T> {
    @Override
    public byte[] serialize(T input) {
        return SerDe.serialize(input);
    }
}
