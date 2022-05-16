package group.insyde.statefun.tsukuyomi.core.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ValidationRunnerImplTest {

    @Mock
    GivenFunction mockedGivenFunction;
    @Mock
    Interactor mockedInteractor;
    @Mock
    Criterion mockedCriterion;

    @Test
    void startsFunctionThenInteractsThenExpectsThenStops() {
        ValidationRunnerImpl then = ValidationRunnerImpl.of(mockedGivenFunction, mockedInteractor);

        then.validate(mockedCriterion);

        then(mockedGivenFunction).should().start(mockedCriterion);
        then(mockedGivenFunction).should().interact(mockedInteractor);
        then(mockedGivenFunction).should().expect(mockedCriterion);
        then(mockedGivenFunction).should().stop();
    }

    @Test
    void shutsDownIfExceptionOccurredDuringInteraction() {
        ValidationRunnerImpl then = ValidationRunnerImpl.of(mockedGivenFunction, mockedInteractor);
        willThrow(RuntimeException.class).given(mockedGivenFunction).start(mockedCriterion);

        try {
            then.validate(mockedCriterion);
        } catch (Exception ignore) {
            // noop
        }

        then(mockedGivenFunction).should().stop();
    }
}