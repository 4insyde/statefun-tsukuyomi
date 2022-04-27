package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
    public Integer match(int order, TsukuyomiApi tsukuyomi, Set<Integer> indexBlacklist) {
        List<Envelope> received = new ArrayList<>(tsukuyomi.getReceived());
        Envelope envelope = getEnvelope(order, received);
        assertThat(envelope, is(expected));
        return received.indexOf(expected);
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

    private Envelope getEnvelope(int order, Collection<Envelope> envelopes) {
        List<Envelope> thisTargetScopedEnvelopes = envelopes.stream()
                .filter(e -> Objects.equals(e.getTo(), getTo()))
                .collect(Collectors.toList());
        return thisTargetScopedEnvelopes.size() > order
                ? thisTargetScopedEnvelopes.get(order)
                : null;
    }
}
