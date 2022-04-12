package com.github.f1xman.statefun.tsukuyomi.api;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ThenImpl implements Then {

    GivenFunction givenFunction;
    Interactor[] interactors;

    @Override
    public void then(ChangeMatcher... matchers) {
        try {
            givenFunction.interact(interactors);
            givenFunction.expect(matchers);
        } finally {
            givenFunction.shutdown();
        }
    }

}
