package com.github.f1xman.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.Context;

public interface ReportableContext extends Context {

    void report();

}
