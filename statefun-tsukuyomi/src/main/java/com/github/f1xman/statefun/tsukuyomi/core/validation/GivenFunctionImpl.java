package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.FunctionDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.*;

import static com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type.EGRESS;
import static com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type.FUNCTION;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class GivenFunctionImpl implements GivenFunction {

    TypedFunction typedFunction;
    StateSetter<?>[] stateSetters;
    TsukuyomiManager manager;
    @NonFinal
    @Setter
    TsukuyomiApi tsukuyomi;

    @Override
    public void start(ChangeMatcher[] matchers) {
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
                .typeName(typedFunction.getTypeName())
                .instance(typedFunction.getInstance())
                .stateSetters(List.of(stateSetters))
                .build();
        Set<Target> targets = Arrays.stream(matchers)
                .filter(MessageMatcher.class::isInstance)
                .map(MessageMatcher.class::cast)
                .map(MessageMatcher::getTarget)
                .collect(toSet());
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
    public void expect(ChangeMatcher... matchers) {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.getFrom(tsukuyomi);
        definitionOfReady.await();
        matchState(matchers);
        matchMessages(matchers);
    }

    private void matchState(ChangeMatcher[] matchers) {
        Arrays.stream(matchers)
                .filter(StateMatcher.class::isInstance)
                .map(StateMatcher.class::cast)
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
