package group.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class StateSetterImplTest {

    static final String FOO = "foo";
    static ValueSpec<String> SPEC = ValueSpec.named(FOO).withUtf8StringType();

    @Mock
    AddressScopedStorage storage;

    @Test
    void setsStateValue() {
        StateSetterImpl<String> setter = StateSetterImpl.of(SPEC, FOO);

        setter.setStateValue(storage);

        then(storage).should().set(SPEC, FOO);
    }

    @Test
    void doesNothingIfStateValueIsNull() {
        StateSetterImpl<String> setter = StateSetterImpl.of(SPEC, null);

        setter.setStateValue(storage);

        then(storage).shouldHaveZeroInteractions();
    }
}