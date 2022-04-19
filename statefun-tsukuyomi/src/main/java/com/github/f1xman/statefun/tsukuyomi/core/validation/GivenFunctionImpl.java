package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.capture.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.validation.Target.Type;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
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
        ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                .typeName(typedFunction.getTypeName())
                .instance(typedFunction.getInstance())
                .stateSetters(List.of(stateSetters))
                .build();
        Set<Target> targets = Arrays.stream(matchers)
                .map(ChangeMatcher::getTarget)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
        Map<Type, Set<TypeName>> targetsByType = targets.stream()
                .collect(
                        groupingBy(Target::getType,
                                mapping(Target::getTypeName,
                                        toSet())));
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(functionDefinition)
                .collaborators(targetsByType.get(FUNCTION))
                .egresses(targetsByType.get(EGRESS))
                .build();
        tsukuyomi = manager.start(moduleDefinition);
    }

    @Override
    public void interact(Interactor interactor) {
        interactor.interact(tsukuyomi);
    }

    @Override
    public void expect(ChangeMatcher... matchers) {
        DefinitionOfReady definitionOfReady = DefinitionOfReady.of(tsukuyomi);
        Arrays.stream(matchers).forEach(m -> m.exportReadyDefinition(definitionOfReady));
        definitionOfReady.await();

        Map<Optional<Target>, List<ChangeMatcher>> matchersByTarget = Arrays.stream(matchers)
                .collect(
                        groupingBy(ChangeMatcher::getTarget,
                                toList()));
        for (List<ChangeMatcher> sameTargetMatchers : matchersByTarget.values()) {
            for (int order = 0; order < sameTargetMatchers.size(); order++) {
                ChangeMatcher orderedMatcher = sameTargetMatchers.get(order);
                orderedMatcher.match(order, tsukuyomi);
            }
        }
    }

    @Override
    public void stop() {
        manager.stop();
    }
}
