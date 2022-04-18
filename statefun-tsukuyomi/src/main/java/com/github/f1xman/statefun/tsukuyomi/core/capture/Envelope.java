package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.util.SerDe;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.statefun.sdk.java.types.TypeSerializer;

import java.io.Serializable;
import java.util.Base64;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
@ToString
public class Envelope implements Serializable {

    public static final Type<Envelope> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.statefun.tsukuyomi/envelope"),
            Envelope::toJson, Envelope::fromJson
    );
    @JsonProperty("from")
    NodeAddress from;
    @JsonProperty("to")
    NodeAddress to;
    @JsonProperty("data")
    Data data;

    public static Envelope fromJson(byte[] bytes) {
        return SerDe.deserialize(bytes, Envelope.class);
    }

    public static Envelope fromJson(String json) {
        return SerDe.deserialize(json, Envelope.class);
    }

    public byte[] toJson() {
        return SerDe.serialize(this);
    }

    public String toJsonAsString() {
        return SerDe.serializeAsString(this);
    }

    public static class EnvelopeBuilder {

        NodeAddress from;
        NodeAddress to;
        Data data;

        public EnvelopeBuilder from(TypeName typeName, String id) {
            from = NodeAddress.of(typeName.asTypeNameString(), id);
            return this;
        }

        public EnvelopeBuilder from(NodeAddress from) {
            this.from = from;
            return this;
        }

        public EnvelopeBuilder to(TypeName typeName, String id) {
            to = NodeAddress.of(typeName.asTypeNameString(), id);
            return this;
        }

        public EnvelopeBuilder to(NodeAddress to) {
            this.to = to;
            return this;
        }

        public EnvelopeBuilder data(Data data) {
            this.data = data;
            return this;
        }

        public <T> EnvelopeBuilder data(Type<T> type, T value) {
            Base64.Encoder encoder = Base64.getEncoder();
            TypeSerializer<T> serializer = type.typeSerializer();
            this.data = Data.of(
                    type.typeName().asTypeNameString(),
                    encoder.encodeToString(serializer.serialize(value).toByteArray())
            );
            return this;
        }

    }

    @RequiredArgsConstructor(staticName = "of", onConstructor_ = @JsonCreator)
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class NodeAddress implements Serializable {

        @JsonProperty("type")
        String type;
        @JsonProperty("id")
        String id;

    }

    @RequiredArgsConstructor(staticName = "of", onConstructor_ = @JsonCreator)
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Data implements Serializable {

        @JsonProperty("type")
        String type;
        @JsonProperty("value")
        String value;

    }
}
