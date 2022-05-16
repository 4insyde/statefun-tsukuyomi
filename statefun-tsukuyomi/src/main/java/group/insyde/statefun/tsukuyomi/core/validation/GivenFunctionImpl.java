package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import group.insyde.statefun.tsukuyomi.core.capture.FunctionDefinition;
import group.insyde.statefun.tsukuyomi.core.capture.StateSetter;
import group.insyde.statefun.tsukuyomi.core.capture.StatefunModule;
import group.insyde.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import group.insyde.statefun.tsukuyomi.core.validation.Target.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static group.insyde.statefun.tsukuyomi.core.validation.Target.Type.EGRESS;
import static group.insyde.statefun.tsukuyomi.core.validation.Target.Type.FUNCTION;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = PRIVATE)
@Builder
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class GivenFunctionImpl implements GivenFunction {

    TypedFunction typedFunction;
    StateSetter<?>[] stateSetters;
    TsukuyomiManager manager;
    @NonFinal
    @Setter
    TsukuyomiApi tsukuyomi;

    @Override
    public void start(Criterion... criteria) {
        Set<Target> targets = Arrays.stream(criteria)
                .filter(EnvelopeCriterion.class::isInstance)
                .map(EnvelopeCriterion.class::cast)
                .map(EnvelopeCriterion::getTarget)
                .collect(toUnmodifiableSet());
        doStart(targets);
    }

    private void doStart(Set<Target> targets) {
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(typedFunction.getTypeName())
                .instance(typedFunction.getInstance())
                .stateSetters(List.of(stateSetters))
                .build();
        Map<Type, Set<TypeName>> targetsByType = targets.stream()
                .collect(
                        groupingBy(Target::getType,
                                mapping(Target::getTypeName,
                                        toSet())));
        StatefunModule statefunModule = StatefunModule.builder()
                .functionUnderTest(functionDefinition)
                .collaborators(targetsByType.get(FUNCTION))
                .egresses(targetsByType.get(EGRESS))
                .build();
        tsukuyomi = manager.start(statefunModule);
    }

    @Override
    public void interact(Interactor interactor) {
        interactor.interact(tsukuyomi);
    }

    @Override
    public void expect(Criterion... criteria) {
        validateEnvelopes(criteria);
        validateState(criteria);
    }

    private void validateState(Criterion[] criteria) {
        Stream<StateCriterion> stateCriteria = Arrays.stream(criteria)
                .filter(StateCriterion.class::isInstance)
                .map(StateCriterion.class::cast);
        StateMatcher stateMatcher = stateCriteria.collect(
                collectingAndThen(toUnmodifiableList(),
                        StateMatcher::of));
        stateMatcher.match(tsukuyomi);
    }

    private void validateEnvelopes(Criterion[] criteria) {
        Stream<EnvelopeCriterion> envelopeCriteria = Arrays.stream(criteria)
                .filter(EnvelopeCriterion.class::isInstance)
                .map(EnvelopeCriterion.class::cast);
        Map<Envelope, EnvelopeMatcher> envelopeMatchers = envelopeCriteria.collect(
                groupingBy(EnvelopeCriterion::getEnvelope,
                        collectingAndThen(toList(),
                                EnvelopeMatcher::of)));
        envelopeMatchers.values().forEach(m -> m.match(tsukuyomi));
    }

    @Override
    public void stop() {
        manager.stop();
    }
}
