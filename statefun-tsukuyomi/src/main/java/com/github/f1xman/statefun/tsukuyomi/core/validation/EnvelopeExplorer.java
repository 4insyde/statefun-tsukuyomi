package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;

public interface EnvelopeExplorer {

    EnvelopeSummary explore(Envelope envelope);

}
