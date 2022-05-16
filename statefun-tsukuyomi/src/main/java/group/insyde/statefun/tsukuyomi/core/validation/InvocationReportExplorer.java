package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;

public interface InvocationReportExplorer {
    boolean findAndHide(Envelope envelope);
}
