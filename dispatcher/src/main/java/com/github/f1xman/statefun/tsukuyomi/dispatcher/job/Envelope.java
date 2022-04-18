package com.github.f1xman.statefun.tsukuyomi.dispatcher.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.SerDe;
import com.google.protobuf.ByteString;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.flink.core.message.RoutableMessage;
import org.apache.flink.statefun.flink.core.message.RoutableMessageBuilder;
import org.apache.flink.statefun.sdk.Address;
import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;

import java.util.Base64;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Getter
@EqualsAndHashCode
@Builder
@ToString
public class Envelope {

    @JsonProperty("from")
    NodeAddress from;
    @JsonProperty("to")
    NodeAddress to;
    @JsonProperty("data")
    Data data;

    public String toJson() {
        return SerDe.serializeAsString(this);
    }

    public static Envelope fromJson(String json) {
        return SerDe.deserialize(json, Envelope.class);
    }

    public static class EnvelopeBuilder {

        NodeAddress from;
        NodeAddress to;
        Data data;

        public EnvelopeBuilder from(String namespace, String name) {
            this.from = NodeAddress.ofType(namespace, name);
            return this;
        }

        public EnvelopeBuilder to(String namespace, String name) {
            this.to = NodeAddress.ofType(namespace, name);
            return this;
        }

        public EnvelopeBuilder data(TypedValue typedValue) {
            this.data = Data.from(typedValue);
            return this;
        }

    }

    @RequiredArgsConstructor(onConstructor_ = @JsonCreator)
    @FieldDefaults(makeFinal = true, level = PRIVATE)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class NodeAddress {

        @JsonProperty("type")
        String type;
        @JsonProperty("id")
        String id;

        public static NodeAddress of(String typeName) {
            String[] parts = typeName.split(";");
            return new NodeAddress(parts[0], parts[1]);
        }

        public static NodeAddress ofType(String namespace, String name) {
            return new NodeAddress(namespace + "/" + name, null);
        }

        public Address toAddress() {
            String[] parts = type.split("/");
            String namespace = parts[0];
            String type = parts[1];
            return new Address(new FunctionType(namespace, type), id);
        }

    }

    @RequiredArgsConstructor(onConstructor_ = @JsonCreator)
    @FieldDefaults(makeFinal = true, level = PRIVATE)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Data {

        @JsonProperty("type")
        String type;
        @JsonProperty("value")
        String value;

        public static Data from(TypedValue typedValue) {
            byte[] value = typedValue.getValue().toByteArray();
            String base64Value = Base64.getEncoder().encodeToString(value);
            return new Data(typedValue.getTypename(), base64Value);
        }

        public TypedValue toTypedValue() {
            return TypedValue.newBuilder()
                    .setHasValue(true)
                    .setValue(ByteString.copyFrom(Base64.getDecoder().decode(value)))
                    .setTypename(type)
                    .build();
        }

    }

    public RoutableMessage toRoutableMessage() {
        RoutableMessageBuilder builder = RoutableMessageBuilder.builder();
        Optional.ofNullable(from).map(NodeAddress::toAddress).ifPresent(builder::withSourceAddress);
        return builder
                .withTargetAddress(to.toAddress())
                .withMessageBody(data.toTypedValue())
                .build();
    }
}
