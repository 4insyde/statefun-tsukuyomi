package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.util.SerDe;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.slice.Slices;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.statefun.sdk.java.types.TypeSerializer;

import java.io.Serializable;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
@ToString
public class Envelope implements Serializable {

    private static Map<String, ValueRenderer> renderersByType = new ConcurrentHashMap<>();

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

        public EnvelopeBuilder toEgress(TypeName typeName) {
            to = NodeAddress.of(typeName.asTypeNameString(), null);
            return this;
        }

        public EnvelopeBuilder to(NodeAddress to) {
            this.to = to;
            return this;
        }

        EnvelopeBuilder data(Data data) {
            this.data = data;
            return this;
        }

        public <T> EnvelopeBuilder data(Type<T> type, T value) {
            Base64.Encoder encoder = Base64.getEncoder();
            TypeSerializer<T> serializer = type.typeSerializer();
            String typeNameString = type.typeName().asTypeNameString();
            renderersByType.put(typeNameString, TypeValueRenderer.of(type));
            this.data = Data.of(
                    typeNameString,
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
    public static class Data implements Serializable {

        @JsonProperty("type")
        String type;
        @JsonProperty("value")
        String value;

        @Override
        public String toString() {
            ValueRenderer renderer = renderersByType.computeIfAbsent(type, t -> new NoOpValueRenderer());
            return "Data{" +
                    "type='" + type + '\'' +
                    ", value='" + renderer.render(value) + '\'' +
                    '}';
        }
    }

    private interface ValueRenderer {

        String render(String value);

    }

    private static class NoOpValueRenderer implements ValueRenderer {

        @Override
        public String render(String value) {
            return value;
        }
    }

    @RequiredArgsConstructor(staticName = "of")
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    private static class TypeValueRenderer implements ValueRenderer {

        Type<?> type;

        @Override
        public String render(String value) {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedValue = decoder.decode(value);
            TypeSerializer<?> serializer = type.typeSerializer();
            Object deserializedValue = serializer.deserialize(Slices.wrap(decodedValue));
            return deserializedValue.toString();
        }
    }

}
