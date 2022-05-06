package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvelopeCriterion implements Criterion {

    @Getter
    int order;
    @Getter
    Envelope envelope;
    Target.Type targetType;

    public static EnvelopeCriterion toFunction(int index, Envelope envelope) {
        return new EnvelopeCriterion(index, envelope, Target.Type.FUNCTION);
    }

    public static EnvelopeCriterion toFunction(Envelope envelope) {
        return new EnvelopeCriterion(Integer.MIN_VALUE, envelope, Target.Type.FUNCTION);
    }

    public static EnvelopeCriterion toEgress(int index, Envelope envelope) {
        return new EnvelopeCriterion(index, envelope, Target.Type.EGRESS);
    }

    public static EnvelopeCriterion toEgress(Envelope envelope) {
        return new EnvelopeCriterion(Integer.MIN_VALUE, envelope, Target.Type.EGRESS);
    }

    public boolean isOrdered() {
        return order >= 0;
    }

    public Target getTarget() {
        Envelope.NodeAddress to = envelope.getTo();
        String typeNameString = to.getType();
        TypeName typeName = TypeName.typeNameFromString(typeNameString);
        return Target.of(typeName, targetType);
    }
}
