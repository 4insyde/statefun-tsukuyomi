package com.github.f1xman.statefun.tsukuyomi.core.capture;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.message.Message;

import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class NextInvocationsInterceptor implements StatefulFunction {

    StatefulFunction functionUnderTest;
    StatefulFunction messageCaptureFunction;
    @NonFinal
    volatile boolean captureAll;

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) throws Throwable {
        if (captureAll) {
            log.info("Forwarding {} message to the capture function...", message);
            return messageCaptureFunction.apply(context, message);
        }
        log.info("Forwarding {} message to the function under test", message);
        captureAll = true;
        log.info("Mode changed to capture all");
        return functionUnderTest.apply(context, message);
    }
}
