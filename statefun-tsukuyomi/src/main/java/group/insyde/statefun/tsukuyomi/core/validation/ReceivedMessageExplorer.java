package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;

public interface ReceivedMessageExplorer {

    boolean findAndHide(Envelope envelope);

}
