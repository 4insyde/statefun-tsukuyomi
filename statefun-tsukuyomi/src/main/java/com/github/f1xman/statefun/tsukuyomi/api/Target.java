package com.github.f1xman.statefun.tsukuyomi.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
class Target {

    TypeName typeName;
    Type type;

    enum Type {
        FUNCTION, EGRESS
    }
}
