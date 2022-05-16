package group.insyde.statefun.tsukuyomi.core.validation;

import group.insyde.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeCriterionTest {

    public static final TypeName TO_TYPE_NAME = TypeName.typeNameFromString("foo/baz");

    @Test
    void isOrderedReturnsTrueIfCriterionIsOrdered() {
        EnvelopeCriterion criterion = EnvelopeCriterion.ofOrdered(0, envelope());

        assertThat(criterion.isOrdered()).isTrue();
    }

    @Test
    void isOrderedReturnsFalseIfCriterionIsUnordered() {
        EnvelopeCriterion criterion = EnvelopeCriterion.of(envelope());

        assertThat(criterion.isOrdered()).isFalse();
    }

    @Test
    void returnsTargetWithTypeNameOfToAndGivenType() {
        EnvelopeCriterion criterion = EnvelopeCriterion.of(envelope());

        Target target = criterion.getTarget();

        assertThat(target.getTypeName()).isEqualTo(TO_TYPE_NAME);
        assertThat(target.getType()).isEqualTo(Target.Type.FUNCTION);
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .toFunction(TO_TYPE_NAME, "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}