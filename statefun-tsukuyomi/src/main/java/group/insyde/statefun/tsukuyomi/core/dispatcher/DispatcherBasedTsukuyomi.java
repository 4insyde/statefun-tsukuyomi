package group.insyde.statefun.tsukuyomi.core.dispatcher;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import group.insyde.statefun.tsukuyomi.core.capture.InvocationReport;
import group.insyde.statefun.tsukuyomi.core.capture.ManagedStateAccessor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class DispatcherBasedTsukuyomi implements TsukuyomiApi {

    DispatcherClient client;
    ManagedStateAccessor stateAccessor;
    Supplier<Boolean> activityStatusSupplier;

    @Override
    public void send(Envelope envelope) {
        client.send(envelope);
        InteractionCompletedWaiter.getFrom(this).await();
    }

    @Override
    public Collection<Envelope> getReceived() {
        return new ArrayList<>(client.getReceived()).stream()
                .filter(not(e -> e.is(InvocationReport.TYPE)))
                .collect(toUnmodifiableList());
    }

    @Override
    public ManagedStateAccessor getStateAccessor() {
        return stateAccessor;
    }

    @Override
    public boolean isStateUpdated() {
        return stateAccessor.isStateUpdated();
    }

    @Override
    public boolean isActive() {
        return activityStatusSupplier.get();
    }

    @Override
    public Optional<InvocationReport> getInvocationReport() {
        return new ArrayList<>(client.getReceived()).stream()
                .filter(e -> e.is(InvocationReport.TYPE))
                .map(e -> e.extractValue(InvocationReport.TYPE))
                .findAny();
    }
}
