package com.github.f1xman.statefun.tsukuyomi.core.validation;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public
class ThenImpl implements Then {

    GivenFunction givenFunction;
    Interactor[] interactors;

    @Override
    public void then(ChangeMatcher... matchers) {
        try {
            givenFunction.start(matchers);
            givenFunction.interact(interactors);
            givenFunction.expect(matchers);
        } finally {
            givenFunction.stop();
        }
    }

}
