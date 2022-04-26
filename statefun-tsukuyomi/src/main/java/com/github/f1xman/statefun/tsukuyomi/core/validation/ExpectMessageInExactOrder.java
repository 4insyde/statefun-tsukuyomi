package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope.NodeAddress;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ExpectMessageInExactOrder extends AbstractExpectMessage {

    Envelope expected;
    Target.Type targetType;

    @Override
    public void match(int order, TsukuyomiApi tsukuyomi) {
        Envelope envelope = getEnvelope(order, tsukuyomi::getReceived);
        assertThat(envelope, is(expected));
    }

    @Override
    @NotNull
    protected NodeAddress getTo() {
        return expected.getTo();
    }

    @Override
    protected Target.Type getTargetType() {
        return targetType;
    }

    private Envelope getEnvelope(int order, Supplier<Collection<Envelope>> receivedSupplier) {
        Collection<Envelope> envelopes = receivedSupplier.get();
        List<Envelope> thisTargetScopedEnvelopes = envelopes.stream()
                .filter(e -> Objects.equals(e.getTo(), getTo()))
                .collect(Collectors.toList());
        return thisTargetScopedEnvelopes.get(order);
    }
}
