package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
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
        targetType.doAssert(expected, tsukuyomi);
        return received.indexOf(expected);
    }

    @Override
    protected Envelope.NodeAddress getTo() {
        return expected.getTo();
    }

    @Override
    protected Target.Type getTargetType() {
        return targetType;
    }
}
