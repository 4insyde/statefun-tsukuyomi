package com.github.f1xman.statefun.tsukuyomi.dsl;

import com.github.f1xman.statefun.tsukuyomi.core.validation.TsukuyomiManagerImpl;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetterImpl;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateValue;
import com.github.f1xman.statefun.tsukuyomi.core.validation.*;
import lombok.NoArgsConstructor;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class BddTsukuyomi {

    public static GivenFunction given(TypedFunction function, StateSetter<?>... states) {
        return GivenFunctionImpl.of(function, states, new TsukuyomiManagerImpl());
    }

    public static TypedFunction function(TypeName type, StatefulFunction instance) {
        return TypedFunctionImpl.of(type, instance);
    }

    public static <T> StateSetter<T> withState(ValueSpec<T> spec, StateValue<T> value) {
        return StateSetterImpl.of(spec, value.get());
    }

    public static Then when(GivenFunction givenFunction, Interactor interactor) {
        return ThenImpl.of(givenFunction, interactor);
    }

    public static Interactor receives(Envelope envelope) {
        return SendMessageInteractor.of(envelope);
    }

}
