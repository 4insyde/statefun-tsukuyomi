package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeCriterionTest {

    @Test
    void isOrderedReturnsTrueIfCriterionIsOrdered() {
        EnvelopeCriterion criterion = EnvelopeCriterion.ordered(0, envelope());

        assertThat(criterion.isOrdered()).isTrue();
    }

    @Test
    void isOrderedReturnsFalseIfCriterionIsUnordered() {
        EnvelopeCriterion criterion = EnvelopeCriterion.unordered(envelope());

        assertThat(criterion.isOrdered()).isFalse();
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}