package group.insyde.statefun.tsukuyomi.dispatcher.config;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.io.EgressIdentifier;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;

import java.net.URI;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(makeFinal = true, level = PRIVATE)
@ToString
public class DispatcherConfig {

    StatefunModule module;

    public Set<EgressIdentifier<TypedValue>> getEgressIdentifiers() {
        return module.getEgressIds()
                .stream()
                .map(id -> {
                    String[] parts = id.split("/");
                    return new EgressIdentifier<>(parts[0], parts[1], TypedValue.class);
                })
                .collect(toSet());
    }

    public Set<FunctionType> getFunctionTypes() {
        return module.getFunctionTypes();
    }

    public URI getEndpoint() {
        return module.getEndpoint();
    }
}
