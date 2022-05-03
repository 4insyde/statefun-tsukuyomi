package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

import static java.util.function.Predicate.isEqual;
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
        Optional<InvocationReport> report = tsukuyomiApi.getInvocationReport();
        return report.map(InvocationReport::getOutgoingMessagesCount)
                .filter(isEqual(tsukuyomiApi.getReceived().size()))
                .isPresent();
    }
}
