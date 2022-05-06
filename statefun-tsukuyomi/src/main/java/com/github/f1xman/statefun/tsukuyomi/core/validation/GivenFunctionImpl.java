package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.FunctionDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.InteractionCompletedWaiter;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type.EGRESS;
import static com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type.FUNCTION;
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
    @Deprecated
    public void start(ChangeMatcher[] matchers) {
        Set<Target> targets = Arrays.stream(matchers)
                .filter(MessageMatcher.class::isInstance)
                .map(MessageMatcher.class::cast)
                .map(MessageMatcher::getTarget)
                .collect(toSet());
        doStart(targets);
    }

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
    @Deprecated
    public void expect(ChangeMatcher... matchers) {
        InteractionCompletedWaiter interactionCompletedWaiter = InteractionCompletedWaiter.getFrom(tsukuyomi);
        interactionCompletedWaiter.await();
        matchState(matchers);
        matchMessages(matchers);
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

    private void matchState(ChangeMatcher[] matchers) {
        Arrays.stream(matchers)
                .filter(StateMatcherOld.class::isInstance)
                .map(StateMatcherOld.class::cast)
                .forEach(m -> m.match(tsukuyomi));
    }

    private void matchMessages(ChangeMatcher[] matchers) {
        Map<Target, List<MessageMatcher>> matchersByTarget = Arrays.stream(matchers)
                .filter(MessageMatcher.class::isInstance)
                .map(MessageMatcher.class::cast)
                .collect(
                        groupingBy(
                                MessageMatcher::getTarget));
        val indexBlacklist = new HashSet<Integer>();
        for (List<MessageMatcher> sameTargetMatchers : matchersByTarget.values()) {
            for (int order = 0; order < sameTargetMatchers.size(); order++) {
                MessageMatcher orderedMatcher = sameTargetMatchers.get(order);
                Integer matchedIndex = orderedMatcher.match(order, tsukuyomi, indexBlacklist);
                indexBlacklist.add(matchedIndex);
            }
        }
    }

    @Override
    public void stop() {
        manager.stop();
    }
}
