package com.github.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.testing.SideEffects;
import org.apache.flink.statefun.sdk.java.testing.TestContext;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ReportableContextImplTest {

    static final Address SELF = new Address(TypeName.typeNameFromString("foo/self"), "baz");
    static final Address CALLER = new Address(TypeName.typeNameFromString("foo/caller"), "baz");
    static final Address TARGET = new Address(TypeName.typeNameFromString("foo/target"), "foobaz");
    static final TypeName EGRESS = TypeName.typeNameFromString("foo/egress");
    static final String CANCELLATION_TOKEN = "CANCELLATION_TOKEN";
    static final String VALUE = "foobarbaz";
    public static final Duration DELAY = Duration.ofHours(1);

    @Mock
    Message mockedMessage;

    @Test
    void returnsSelfFromContext() {
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(TestContext.forTarget(SELF));

        Address actualSelf = reportableContext.self();

        assertThat(actualSelf).isEqualTo(SELF);
    }

    @Test
    void returnsCallerFromContext() {
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(TestContext.forTargetWithCaller(SELF, CALLER));

        Optional<Address> actualCaller = reportableContext.caller();

        assertThat(actualCaller).contains(CALLER);
    }

    @Test
    void returnsStorageFromContext() {
        TestContext context = TestContext.forTarget(SELF);
        AddressScopedStorage expectedStorage = context.storage();
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);

        AddressScopedStorage actualStorage = reportableContext.storage();

        assertThat(actualStorage).isSameAs(expectedStorage);
    }

    @Test
    void throwsIllegalArgumentExceptionIfGivenContextIsNull() {
        assertThatThrownBy(() -> ReportableContextImpl.spyOn(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void countOfOutgoingMessagesIsZeroOnANewContext() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);

        assertThat(reportableContext.getNumberOfOutgoingMessages()).isZero();
    }

    @Test
    void sendsAMessageAndIncrementsCountOfOutgoingMessages() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Message expectedMessage = MessageBuilder.forAddress(TARGET)
                .withValue(0)
                .build();

        reportableContext.send(expectedMessage);

        assertThat(reportableContext.getNumberOfOutgoingMessages()).isEqualTo(1);
        assertThat(context.getSentMessages()).contains(new SideEffects.SendSideEffect(expectedMessage));
    }

    @Test
    void sendsADelayedMessageAndIncrementsCountOfOutgoingMessages() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Message message = MessageBuilder.forAddress(SELF)
                .withValue(VALUE)
                .build();

        reportableContext.sendAfter(DELAY, message);

        assertThat(reportableContext.getNumberOfOutgoingMessages()).isEqualTo(1);
        assertThat(context.getSentDelayedMessages())
                .contains(
                        new SideEffects.SendAfterSideEffect(DELAY, message)
                );
    }

    @Test
    void sendsADelayedMessageWithCancellationTokenAndIncrementsCountOfOutgoingMessages() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Message message = MessageBuilder.forAddress(SELF)
                .withValue(VALUE)
                .build();

        reportableContext.sendAfter(DELAY, CANCELLATION_TOKEN, message);

        assertThat(reportableContext.getNumberOfOutgoingMessages()).isEqualTo(1);
        assertThat(context.getSentDelayedMessages())
                .contains(
                        new SideEffects.SendAfterSideEffect(DELAY, message, CANCELLATION_TOKEN)
                );
    }

    @Test
    void cancelsADelayedMessageAndDecrementsCountOfOutgoingMessages() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Message message = MessageBuilder.forAddress(SELF)
                .withValue(VALUE)
                .build();

        reportableContext.sendAfter(DELAY, CANCELLATION_TOKEN, message);
        reportableContext.cancelDelayedMessage(CANCELLATION_TOKEN);

        assertThat(reportableContext.getNumberOfOutgoingMessages()).isZero();
        assertThat(context.getSentDelayedMessages()).isEmpty();
    }

    @Test
    void sendsAnEgressMessageAndIncrementsCountOfOutgoingMessages() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        EgressMessage egressMessage = EgressMessageBuilder.forEgress(EGRESS)
                .withValue(VALUE)
                .build();

        reportableContext.send(egressMessage);

        assertThat(reportableContext.getNumberOfOutgoingMessages()).isEqualTo(1);
        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(egressMessage));
    }

    @Test
    void sendsNumberOfOutgoingMessages() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(List.of()))
                .build();
        EgressMessage expectedMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();

        reportableContext.report();

        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(expectedMessage));
    }

    @Test
    void sendsReportWithRegularEnvelopesIncluded() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(List.of(envelope())))
                .build();
        EgressMessage expectedMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();

        Message message = MessageBuilder.forAddress(TARGET)
                .withValue(VALUE)
                .build();
        reportableContext.send(message);
        reportableContext.report();

        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(expectedMessage));
    }

    @Test
    void sendsReportWithEgressEnvelopesIncluded() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(List.of(egressEnvelope())))
                .build();
        EgressMessage expectedMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();

        EgressMessage egressMessage = EgressMessageBuilder.forEgress(EGRESS)
                .withValue(VALUE)
                .build();
        reportableContext.send(egressMessage);
        reportableContext.report();

        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(expectedMessage));
    }

    @Test
    void sendsReportWithDelayedEnvelopesIncluded() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(List.of(delayedEnvelope())))
                .build();
        EgressMessage expectedMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();

        Message message = MessageBuilder.forAddress(TARGET)
                .withValue(VALUE)
                .build();
        reportableContext.sendAfter(DELAY, message);
        reportableContext.report();

        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(expectedMessage));
    }

    @Test
    void sendsReportWithDelayedEnvelopesHavingCancellationTokenIncluded() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(List.of(delayedEnvelope())))
                .build();
        EgressMessage expectedMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();

        Message message = MessageBuilder.forAddress(TARGET)
                .withValue(VALUE)
                .build();
        reportableContext.sendAfter(DELAY, CANCELLATION_TOKEN, message);
        reportableContext.report();

        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(expectedMessage));
    }

    @Test
    void sendsEmptyReportIfAllDelayedEnvelopesCancelled() {
        TestContext context = TestContext.forTarget(SELF);
        ReportableContextImpl reportableContext = ReportableContextImpl.spyOn(context);
        Envelope envelope = Envelope.builder()
                .toEgress(Egresses.CAPTURED_MESSAGES)
                .data(InvocationReport.TYPE, InvocationReport.of(List.of()))
                .build();
        EgressMessage expectedMessage = EgressMessageBuilder.forEgress(Egresses.CAPTURED_MESSAGES)
                .withCustomType(Envelope.TYPE, envelope)
                .build();

        Message message = MessageBuilder.forAddress(TARGET)
                .withValue(VALUE)
                .build();
        reportableContext.sendAfter(DELAY, CANCELLATION_TOKEN, message);
        reportableContext.cancelDelayedMessage(CANCELLATION_TOKEN);
        reportableContext.report();

        assertThat(context.getSentEgressMessages()).contains(new SideEffects.EgressSideEffect(expectedMessage));
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(SELF.type(), SELF.id())
                .toFunction(TARGET.type(), TARGET.id())
                .data(Types.stringType(), VALUE)
                .build();
    }

    private Envelope egressEnvelope() {
        return Envelope.builder()
                .toEgress(EGRESS)
                .data(Types.stringType(), VALUE)
                .build();
    }

    private Envelope delayedEnvelope() {
        return Envelope.builder()
                .from(SELF.type(), SELF.id())
                .toFunction(TARGET.type(), TARGET.id())
                .data(Types.stringType(), VALUE)
                .delay(DELAY)
                .build();
    }
}