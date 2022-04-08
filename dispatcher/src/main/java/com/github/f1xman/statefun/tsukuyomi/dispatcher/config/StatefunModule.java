package com.github.f1xman.statefun.tsukuyomi.dispatcher.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.FunctionType;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class StatefunModule {

    static final String FUNCTION_TYPES_DELIMITER = ";";
    static final String FUNCTION_NAMESPACE_DELIMITER = "/";
    String functions;
    @Getter
    URI endpoint;

    public Set<FunctionType> getFunctionTypes() {
        return Arrays.stream(functions.split(FUNCTION_TYPES_DELIMITER))
                .map(f -> {
                    String[] parts = f.split(FUNCTION_NAMESPACE_DELIMITER);
                    String namespace = parts[0];
                    String type = parts[1];
                    return new FunctionType(namespace, type);
                })
                .collect(toSet());
    }
}
