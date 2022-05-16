package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import com.github.insyde.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(staticName = "of")
public class EnvelopeMatcher implements Matcher {

    List<EnvelopeCriterion> criteria;

    @Override
    public void match(TsukuyomiApi tsukuyomi) {
        InvocationReport invocationReport = tsukuyomi.getInvocationReport().orElseThrow();
        EnvelopeExplorer explorer = EnvelopeExplorerImpl.of(tsukuyomi);
        Envelope expectedEnvelope = criteria.get(0).getEnvelope();
        EnvelopeSummary summary = explorer.explore(expectedEnvelope);
        assertOrder(invocationReport, expectedEnvelope, summary);
        assertUnorderedEnvelopesReceived(invocationReport, expectedEnvelope, summary);
    }

    private void assertUnorderedEnvelopesReceived(InvocationReport invocationReport, Envelope expectedEnvelope, EnvelopeSummary summary) {
        if (criteria.size() > summary.getTotalReceived()) {
            throw new AssertionError(String.format(
                    "Missing envelope %s is expected to be sent in any order. " +
                            "See the invocation report for more details: %s",
                    expectedEnvelope, invocationReport
            ));
        }
    }

    private void assertOrder(InvocationReport invocationReport,
                             Envelope expectedEnvelope,
                             EnvelopeSummary summary) {
        Set<Integer> expectedIndexes = criteria.stream()
                .filter(EnvelopeCriterion::isOrdered)
                .map(EnvelopeCriterion::getOrder)
                .collect(toUnmodifiableSet());
        Set<Integer> actualIndexes = summary.getEnvelopeMetas().stream()
                .map(EnvelopeMeta::getIndex)
                .collect(toUnmodifiableSet());
        if (!actualIndexes.containsAll(expectedIndexes)) {
            throw new AssertionError(String.format(
                    "Envelope %s is expected to be sent in the following order: %s, but the actual order is: %s. " +
                            "See the invocation report for more details: %s",
                    expectedEnvelope, expectedIndexes, actualIndexes, invocationReport));
        }
    }
}
