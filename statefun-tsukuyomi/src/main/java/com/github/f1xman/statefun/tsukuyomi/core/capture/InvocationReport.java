package com.github.f1xman.statefun.tsukuyomi.core.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.statefun.tsukuyomi.util.SerDe;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.function.Predicate.not;

@RequiredArgsConstructor(staticName = "of", onConstructor_ = {@JsonCreator})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public class InvocationReport {

    public static final Type<InvocationReport> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.statefun.tsukuyomi/invocation-report"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, InvocationReport.class)
    );

    @JsonProperty("outgoingMessagesCount")
    Integer outgoingMessagesCount;
    @JsonProperty("envelopes")
    List<Envelope> envelopes;

    public boolean isRegular(Envelope envelope) {
        return envelopes.contains(envelope);
    }

    public boolean containsAt(Envelope envelope, int index) {
        return envelopes.indexOf(envelope) == index;
    }

    public int indexOf(Envelope envelope, Set<Integer> exclude) {
        return IntStream.range(0, envelopes.size())
                .boxed()
                .filter(not(exclude::contains))
                .filter(i -> envelopes.get(i).equals(envelope))
                .findAny()
                .orElse(-1);
    }

    @RequiredArgsConstructor(staticName = "of", onConstructor_ = {@JsonCreator})
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    private static class DelayedEnvelope {

        @JsonProperty("delay")
        Duration delay;
        @JsonProperty("envelope")
        Envelope envelope;

    }

}
