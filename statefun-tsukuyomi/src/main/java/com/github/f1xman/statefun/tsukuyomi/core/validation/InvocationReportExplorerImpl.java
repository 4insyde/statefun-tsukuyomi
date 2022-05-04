package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvocationReportExplorerImpl implements InvocationReportExplorer {

    Set<Integer> hiddenIndexes = new HashSet<>();
    InvocationReport report;

    @Override
    public boolean findAndHide(Envelope envelope) {
        int index = report.indexOf(envelope, hiddenIndexes);
        return index >= 0 && hiddenIndexes.add(index);
    }
}
