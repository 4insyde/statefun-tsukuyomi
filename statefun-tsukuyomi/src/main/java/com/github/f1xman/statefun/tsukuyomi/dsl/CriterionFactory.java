package com.github.f1xman.statefun.tsukuyomi.dsl;

import com.github.f1xman.statefun.tsukuyomi.core.validation.Criterion;

@FunctionalInterface
public interface CriterionFactory {

    Criterion create(int order);

}
