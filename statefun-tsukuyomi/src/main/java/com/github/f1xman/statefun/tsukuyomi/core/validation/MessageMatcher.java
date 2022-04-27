package com.github.f1xman.statefun.tsukuyomi.core.validation;

import java.util.Set;

public interface MessageMatcher extends ChangeMatcher {

    Integer match(int order, TsukuyomiApi tsukuyomi, Set<Integer> indexBlacklist);

    Target getTarget();

}
