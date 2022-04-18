package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.validation.ChangeMatcher;
import com.github.f1xman.statefun.tsukuyomi.core.validation.GivenFunction;
import com.github.f1xman.statefun.tsukuyomi.core.validation.Interactor;
import com.github.f1xman.statefun.tsukuyomi.core.validation.ThenImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ThenImplTest {

    @Mock
    GivenFunction mockedGivenFunction;
    @Mock
    Interactor mockedInteractor;
    @Mock
    ChangeMatcher mockedChangeMatcher;

    @Test
    void startsFunctionThenInteractsThenExpectsThenStops() {
        ThenImpl then = ThenImpl.of(mockedGivenFunction, new Interactor[]{mockedInteractor});

        then.then(mockedChangeMatcher);

        then(mockedGivenFunction).should().start(new ChangeMatcher[]{mockedChangeMatcher});
        then(mockedGivenFunction).should().interact(new Interactor[]{mockedInteractor});
        then(mockedGivenFunction).should().expect(mockedChangeMatcher);
        then(mockedGivenFunction).should().stop();
    }

    @Test
    void shutsDownIfExceptionOccurredDuringInteraction() {
        ThenImpl then = ThenImpl.of(mockedGivenFunction, new Interactor[]{mockedInteractor});
        willThrow(RuntimeException.class).given(mockedGivenFunction).start(
                new ChangeMatcher[]{mockedChangeMatcher}
        );

        try {
            then.then(mockedChangeMatcher);
        } catch (Exception ignore) {
            // noop
        }

        then(mockedGivenFunction).should().stop();
    }
}