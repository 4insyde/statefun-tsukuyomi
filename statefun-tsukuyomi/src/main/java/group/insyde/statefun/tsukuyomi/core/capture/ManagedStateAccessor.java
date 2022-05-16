package group.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.ValueSpec;

import java.util.Optional;

public interface ManagedStateAccessor {
    <T> Optional<T> getStateValue(ValueSpec<T> spec);

    boolean isStateUpdated();
}
