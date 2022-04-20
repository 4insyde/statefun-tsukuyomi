package com.github.f1xman.statefun.tsukuyomi.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "byKey")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Distinct<T, K> implements Predicate<T> {

    Set<K> seen = new HashSet<>();
    Function<T, K> getKey;

    @Override
    public boolean test(T t) {
        return seen.add(getKey.apply(t));
    }
}
