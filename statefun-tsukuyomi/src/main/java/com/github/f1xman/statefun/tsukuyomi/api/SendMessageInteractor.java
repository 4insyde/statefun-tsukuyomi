package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class SendMessageInteractor implements Interactor {

    Envelope envelope;

    @Override
    public void interact(TsukuyomiApi tsukuyomi) {
        tsukuyomi.send(envelope);
    }
}
