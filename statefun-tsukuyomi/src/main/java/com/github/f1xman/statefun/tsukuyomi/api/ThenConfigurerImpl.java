package com.github.f1xman.statefun.tsukuyomi.api;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ThenConfigurerImpl implements ThenConfigurer {

    GivenFunction givenFunction;
    Interactor[] interactors;

    @Override
    public void then(ChangeMatcher... matchers) {
        givenFunction.interact(interactors);
        givenFunction.expect(matchers);
        givenFunction.shutdown();
    }

}
