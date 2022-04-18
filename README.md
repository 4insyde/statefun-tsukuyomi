# statefun-tsukuyomi

Integration test utility for Flink Stateful Functions.

## What's Tsukuyomi?

Since you're observing this repo, you probably know what's Statefun. But what is Tsukuyomi? I borrowed the word
Tsukuyomi from the fantastic Japanese manga and anime "Naruto". In the world of "Naruto" Tsukuyomi means a super
powerful ninja technique that traps the opponent into an illusion. The opponent cannot identify the illusion nor escape.
This project does the same with the function under test — it puts the function into the fake world with egresses and
other functions. Those fake components capture function state and messages to provide the developer with a clean and
nice API.

## Installation
## Add repository
```xml
<repository>
  <id>statefun-tsukuyomi</id>
  <url>https://maven.pkg.github.com/f1xmAn/statefun-tsukuyomi</url>
</repository>
```
### Add dependency
```xml
<dependency>
    <groupId>com.github.f1xman.statefun.tsukuyomi</groupId>
    <artifactId>statefun-tsukuyomi</artifactId>
    <version>v0.1.0</version>
</dependency>
```
## Usage

### Import

```java
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.validation.GivenFunction;
import com.github.f1xman.statefun.tsukuyomi.testutil.IntegrationTest;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;

import static com.github.f1xman.statefun.tsukuyomi.core.capture.StateValue.empty;
import static com.github.f1xman.statefun.tsukuyomi.core.capture.StateValue.havingValue;
import static com.github.f1xman.statefun.tsukuyomi.dsl.BddTsukuyomi.*;
import static com.github.f1xman.statefun.tsukuyomi.dsl.Expectations.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.is;
```

### Have your function under test

```java
static class Testee implements StatefulFunction {

    static TypeName TYPE = TypeName.typeNameFromString("foo/testee");
    static ValueSpec<String> FOO = ValueSpec.named("foo").withUtf8StringType();
    static ValueSpec<String> BAR = ValueSpec.named("bar").withUtf8StringType();

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) {
        AddressScopedStorage storage = context.storage();
        String bar = storage.get(BAR).orElse("");
        storage.set(FOO, "foo");
        String value = message.asUtf8String() + bar;
        Message toFunction = MessageBuilder.forAddress(COLLABORATOR_2, context.self().id())
                .withValue(value)
                .build();
        context.send(toFunction);
        EgressMessage toEgress = EgressMessageBuilder.forEgress(EGRESS)
                .withValue(value)
                .build();
        context.send(toEgress);
        return context.done();
    }
}
```

### Create envelopes

```java
private Envelope outgoingEnvelopeToEgress() {
    return Envelope.builder()
        .to(EGRESS, null)
        .data(Types.stringType(), HELLO + BAR)
        .build();
}

private Envelope outgoingEnvelopeToFunction() {
    return Envelope.builder()
        .from(Testee.TYPE, FUNCTION_ID)
        .to(COLLABORATOR_2, FUNCTION_ID)
        .data(Types.stringType(), HELLO + BAR)
        .build();
}

private Envelope incomingEnvelope() {
    return Envelope.builder()
        .from(COLLABORATOR_1, FUNCTION_ID)
        .to(Testee.TYPE, FUNCTION_ID)
        .data(Types.stringType(), HELLO)
        .build();
}
```

### Run test

```java
@Test
@Timeout(value = 1, unit = MINUTES)
void exchangesMessages() {
    Envelope envelope = incomingEnvelope();
    Envelope expectedToFunction = outgoingEnvelopeToFunction();
    Envelope expectedToEgress = outgoingEnvelopeToEgress();
    GivenFunction testee = given(
        function(Testee.TYPE, new Testee()),
        withState(Testee.FOO, empty()),
        withState(Testee.BAR, havingValue(BAR))
    );

    when(
        testee,
        receives(envelope)
    ).then(
        expectMessage(expectedToFunction, toFunction()),
        expectMessage(expectedToEgress, toEgress()),
        expectState(Testee.FOO, is("foo"))
    );
}
```
