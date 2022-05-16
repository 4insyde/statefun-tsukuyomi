package com.github.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.StatefulFunctions;

import java.util.function.Consumer;

public interface StatefunServer {

    void start(StatefulFunctions statefulFunctions);

    void stop();

    int getPort();

    void setUncaughtExceptionHandler(Consumer<Throwable> exceptionHandler);

}
