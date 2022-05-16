package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;

public interface InvocationReportExplorer {
    boolean findAndHide(Envelope envelope);
}
