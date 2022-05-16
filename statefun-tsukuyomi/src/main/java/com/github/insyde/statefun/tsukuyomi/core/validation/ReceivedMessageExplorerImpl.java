package com.github.insyde.statefun.tsukuyomi.core.validation;

import com.github.insyde.statefun.tsukuyomi.core.capture.Envelope;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.function.Predicate.not;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReceivedMessageExplorerImpl implements ReceivedMessageExplorer {

    Set<Integer> hiddenIndexes = new HashSet<>();
    List<Envelope> receivedMessages;

    @Override
    public boolean findAndHide(Envelope envelope) {
        Integer index = IntStream.range(0, receivedMessages.size())
                .boxed()
                .filter(not(hiddenIndexes::contains))
                .filter(i -> receivedMessages.get(i).equals(envelope))
                .findAny()
                .orElse(-1);
        return index >= 0 && hiddenIndexes.add(index);
    }
}
