package com.github.f1xman.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.TypeName;

class Egresses {

    static TypeName CAPTURED_MESSAGES = TypeName.typeNameFromString("com.github.f1xman.statefun.tsukuyomi/captured-messages");

}
