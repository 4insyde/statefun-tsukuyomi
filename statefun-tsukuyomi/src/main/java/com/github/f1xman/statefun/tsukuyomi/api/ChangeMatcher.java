package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;

public interface ChangeMatcher {

    void match(int order, TsukuyomiApi tsukuyomi);
}
