package group.insyde.statefun.tsukuyomi.core.capture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import group.insyde.statefun.tsukuyomi.core.validation.EnvelopeMeta;
import group.insyde.statefun.tsukuyomi.util.SerDe;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.javatuples.Pair;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableList;

@RequiredArgsConstructor(staticName = "of", onConstructor_ = {@JsonCreator})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public class InvocationReport {

    public static final Type<InvocationReport> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("group.insyde.statefun.tsukuyomi/invocation-report"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, InvocationReport.class)
    );

    @JsonProperty("envelopes")
    List<Envelope> envelopes;

    public List<EnvelopeMeta> find(Envelope envelope) {
        return IntStream.range(0, envelopes.size())
                .mapToObj(i -> Pair.with(i, envelopes.get(i)))
                .filter(p -> p.getValue1().equals(envelope))
                .map(p -> EnvelopeMeta.of(p.getValue0()))
                .collect(toUnmodifiableList());
    }

    public int countOutgoingMessages() {
        return envelopes.size();
    }
}
