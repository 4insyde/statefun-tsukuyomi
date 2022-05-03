package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
public class Target {

    TypeName typeName;
    Type type;

    public enum Type {
        FUNCTION {
            @Override
            public void doAssert(Envelope expected, TsukuyomiApi tsukuyomi) {
                InvocationReport invocationReport = tsukuyomi.getInvocationReport().orElseThrow();
                assertThat(
                        "Regular message expected, but the actual is delayed",
                        invocationReport.isRegular(expected), is(true));
            }
        }, EGRESS;

        public void doAssert(Envelope expected, TsukuyomiApi tsukuyomiApi) {
        }
    }
}
