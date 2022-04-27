package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ExpectMessageInAnyOrder extends AbstractExpectMessage {

    Envelope expected;
    Target.Type targetType;

    @Override
    public Integer match(int order, TsukuyomiApi tsukuyomi, Set<Integer> indexBlacklist) {
        List<Envelope> received = new ArrayList<>(tsukuyomi.getReceived());
        indexBlacklist.forEach(i -> received.set(i, null));
        assertThat(received, hasItem(expected));
        return received.indexOf(expected);
    }

    @Override
    protected @NotNull Envelope.NodeAddress getTo() {
        return expected.getTo();
    }

    @Override
    protected Target.Type getTargetType() {
        return targetType;
    }
}
