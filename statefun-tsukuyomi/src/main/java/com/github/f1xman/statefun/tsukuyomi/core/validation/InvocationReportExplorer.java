package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;

public interface InvocationReportExplorer {
    boolean findAndHide(Envelope envelope);
}
