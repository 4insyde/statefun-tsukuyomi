package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope.NodeAddress;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ExpectMessage implements ChangeMatcher {

    Envelope expected;
    Target.Type targetType;

    @Override
    public void match(int order, TsukuyomiApi tsukuyomi) {
        Envelope envelope = getEnvelope(order, tsukuyomi::getReceived);
        assertThat(envelope, is(expected));
    }

    @Override
    public Optional<Target> getTarget() {
        return Optional.of(expected.getTo())
                .map(NodeAddress::getType)
                .map(TypeName::typeNameFromString)
                .map(t -> Target.of(t, targetType));
    }

    @Override
    public void adjustDefinitionOfReady(DefinitionOfReady definitionOfReady) {
        definitionOfReady.incrementExpectedEnvelopes();
    }

    private Envelope getEnvelope(int order, Supplier<Collection<Envelope>> receivedSupplier) {
        Collection<Envelope> envelopes = receivedSupplier.get();
        List<Envelope> thisTargetScopedEnvelopes = envelopes.stream()
                .filter(e -> Objects.equals(e.getTo(), expected.getTo()))
                .collect(Collectors.toList());
        return thisTargetScopedEnvelopes.get(order);
    }
}
