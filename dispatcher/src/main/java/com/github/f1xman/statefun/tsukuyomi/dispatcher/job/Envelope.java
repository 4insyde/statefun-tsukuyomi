package com.github.f1xman.statefun.tsukuyomi.dispatcher.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.SerDe;
import com.google.protobuf.ByteString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    @RequiredArgsConstructor(onConstructor_ = @JsonCreator)
    @FieldDefaults(makeFinal = true, level = PRIVATE)
    @Getter
    @EqualsAndHashCode
    public static class NodeAddress {

        @JsonProperty("type")
        String type;
        @JsonProperty("id")
        String id;

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
    public static class Data {

        @JsonProperty("type")
        String type;
        @JsonProperty("value")
        String value;

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
