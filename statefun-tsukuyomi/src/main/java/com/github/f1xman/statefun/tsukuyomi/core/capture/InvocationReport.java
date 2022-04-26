package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.util.SerDe;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

@RequiredArgsConstructor(staticName = "of", onConstructor_ = {@JsonCreator})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
public class InvocationReport {

    public static final Type<InvocationReport> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.statefun.tsukuyomi/invocation-report"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, InvocationReport.class)
    );

    @JsonProperty("outgoingMessagesCount")
    Integer outgoingMessagesCount;

}
