package group.insyde.statefun.tsukuyomi.core.capture;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "havingValue")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
public class StateValue<T> implements Supplier<T> {

    T value;

    public static <T> StateValue<T> empty() {
        return new StateValue<>(null);
    }

    @Override
    public T get() {
        return value;
    }
}
