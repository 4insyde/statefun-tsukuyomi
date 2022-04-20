package com.github.f1xman.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.StatefulFunctions;

public interface StatefunServer {

    void start(StatefulFunctions statefulFunctions);

    void stop();

    int getPort();

}
