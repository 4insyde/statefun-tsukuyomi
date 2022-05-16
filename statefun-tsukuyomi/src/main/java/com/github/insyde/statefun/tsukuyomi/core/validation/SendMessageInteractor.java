package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import com.github.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public
class SendMessageInteractor implements Interactor {

    Envelope envelope;

    @Override
    public void interact(TsukuyomiApi tsukuyomi) {
        tsukuyomi.send(envelope);
    }
}
