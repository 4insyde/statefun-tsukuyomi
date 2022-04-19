package com.github.f1xman.statefun.tsukuyomi.dispatcher.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.FunctionType;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(makeFinal = true, level = PRIVATE)
@ToString
public class StatefunModule {

    static String DELIMITER = ";";
    static String FUNCTION_NAMESPACE_DELIMITER = "/";
    String functions;
    @Getter
    URI endpoint;
    String egresses;

    public Set<FunctionType> getFunctionTypes() {
        return Arrays.stream(functions.split(DELIMITER))
                .map(f -> {
                    String[] parts = f.split(FUNCTION_NAMESPACE_DELIMITER);
                    String namespace = parts[0];
                    String type = parts[1];
                    return new FunctionType(namespace, type);
                })
                .collect(toSet());
    }

    public Set<String> getEgressIds() {
        if (egresses == null || egresses.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(egresses.split(DELIMITER))
                .collect(toSet());
    }
}
