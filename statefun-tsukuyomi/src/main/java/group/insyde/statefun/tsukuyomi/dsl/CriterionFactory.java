package group.insyde.statefun.tsukuyomi.dsl;

import group.insyde.statefun.tsukuyomi.core.validation.Criterion;

@FunctionalInterface
public interface CriterionFactory {

    Criterion create(int order);

}
