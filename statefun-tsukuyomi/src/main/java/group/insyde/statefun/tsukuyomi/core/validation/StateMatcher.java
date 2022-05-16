package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.ManagedStateAccessor;
import group.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(staticName = "of")
public class StateMatcher implements Matcher {

    List<StateCriterion> criteria;

    @Override
    public void match(TsukuyomiApi tsukuyomi) {
        ManagedStateAccessor stateAccessor = tsukuyomi.getStateAccessor();
        criteria.forEach(c -> {
            Optional<Object> value = stateAccessor.getStateValue(c.getValueSpec());
            assertThat(value.orElse(null), c.getMatcher());
        });
    }
}
