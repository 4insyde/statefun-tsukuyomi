package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

import java.util.Set;

public interface MessageMatcher extends ChangeMatcher {

    Integer match(int order, TsukuyomiApi tsukuyomi, Set<Integer> indexBlacklist);

    Target getTarget();

}
