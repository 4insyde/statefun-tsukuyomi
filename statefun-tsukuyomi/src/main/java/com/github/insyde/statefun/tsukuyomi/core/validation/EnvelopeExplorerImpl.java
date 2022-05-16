package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import com.github.insyde.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

import static java.util.function.Predicate.isEqual;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(staticName = "of")
public class EnvelopeExplorerImpl implements EnvelopeExplorer {

    TsukuyomiApi tsukuyomi;

    @Override
    public EnvelopeSummary explore(Envelope envelope) {
        InvocationReport invocationReport = tsukuyomi.getInvocationReport().orElseThrow();
        List<EnvelopeMeta> metas = invocationReport.find(envelope);
        Collection<Envelope> receivedEnvelopes = tsukuyomi.getReceived();
        Envelope envelopeWithoutDelay = dropDelay(envelope);
        long count = receivedEnvelopes.stream()
                .filter(isEqual(envelopeWithoutDelay))
                .count();
        return EnvelopeSummary.of(metas, Math.toIntExact(count));
    }

    private Envelope dropDelay(Envelope envelope) {
        return envelope.toBuilder()
                .delay(null)
                .build();
    }
}
