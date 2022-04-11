package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.StateSetterImpl;
import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiManagerImpl;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

public class Tsukuyomi {

    public static GivenFunction given(TypedFunction function, StateSetter<?>... states) {
        return GivenFunctionImpl.of(function, states, new TsukuyomiManagerImpl());
    }

    public static TypedFunction function(TypeName type, StatefulFunction instance) {
        return TypedFunctionImpl.of(type, instance);
    }

    public static <T> StateSetter<T> withState(ValueSpec<T> spec, StateValue<T> value) {
        return StateSetterImpl.of(spec, value.get());
    }

    public static Then when(GivenFunction givenFunction, Interactor... interactors) {
        return ThenImpl.of(givenFunction, interactors);
    }

    public static Interactor receives(Envelope envelope) {
        return SendMessageInteractor.of(envelope);
    }

    public static ChangeMatcher expectMessage(Matcher<Envelope> matcher) {
        return ExpectMessage.of(matcher);
    }

}
