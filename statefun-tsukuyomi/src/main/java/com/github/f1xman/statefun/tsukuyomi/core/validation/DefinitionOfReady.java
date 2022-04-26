package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "getFrom")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class DefinitionOfReady {

    TsukuyomiApi tsukuyomiApi;

    public void await() {
        while (
                !Thread.currentThread().isInterrupted()
                        && !isReady()
                        && tsukuyomiApi.isActive()
        ) {
            Thread.onSpinWait();
        }
    }

    private boolean isReady() {
        Collection<Envelope> receivedEnvelopes = new ArrayList<>(tsukuyomiApi.getReceived());
        Optional<InvocationReport> report = receivedEnvelopes.stream()
                .filter(e -> e.is(InvocationReport.TYPE))
                .map(e -> e.extractData(InvocationReport.TYPE))
                .findAny();
        Integer expectedSize = report.map(InvocationReport::getOutgoingMessagesCount)
                .map(c -> c + 1)
                .orElse(Integer.MAX_VALUE);
        return receivedEnvelopes.size() == expectedSize;
    }
}
