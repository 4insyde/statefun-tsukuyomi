package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;

public interface ReceivedMessageExplorer {

    boolean findAndHide(Envelope envelope);

}
