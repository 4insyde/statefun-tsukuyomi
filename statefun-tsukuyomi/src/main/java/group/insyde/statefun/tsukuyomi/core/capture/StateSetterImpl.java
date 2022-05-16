package group.insyde.statefun.tsukuyomi.core.capture;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.ValueSpec;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class StateSetterImpl<T> implements StateSetter<T> {

    ValueSpec<T> spec;
    T value;

    @Override
    public ValueSpec<T> getValueSpec() {
        return spec;
    }

    @Override
    public void setStateValue(AddressScopedStorage storage) {
        if (value != null) {
            storage.set(spec, value);
        }
    }
}
