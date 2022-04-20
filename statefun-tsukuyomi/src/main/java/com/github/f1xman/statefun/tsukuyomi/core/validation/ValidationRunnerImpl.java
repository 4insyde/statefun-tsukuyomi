package com.github.f1xman.statefun.tsukuyomi.core.validation;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public
class ValidationRunnerImpl implements ValidationRunner {

    GivenFunction givenFunction;
    Interactor interactor;

    @Override
    public void validate(ChangeMatcher... matchers) {
        try {
            givenFunction.start(matchers);
            givenFunction.interact(interactor);
            givenFunction.expect(matchers);
        } finally {
            givenFunction.stop();
        }
    }

}
