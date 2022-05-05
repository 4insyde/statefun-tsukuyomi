package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
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
        long count = receivedEnvelopes.stream()
                .filter(isEqual(envelope))
                .count();
        return EnvelopeSummary.of(metas, Math.toIntExact(count));
    }
}
