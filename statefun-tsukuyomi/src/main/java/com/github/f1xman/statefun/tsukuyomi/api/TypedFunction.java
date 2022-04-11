package com.github.f1xman.statefun.tsukuyomi.api;

import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;

public interface TypedFunction {

    TypeName getTypeName();

    StatefulFunction getInstance();

}
