package com.github.insyde.statefun.tsukuyomi.dsl;

import com.github.insyde.statefun.tsukuyomi.core.validation.Criterion;

@FunctionalInterface
public interface CriterionFactory {

    Criterion create(int order);

}
