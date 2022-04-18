# statefun-tsukuyomi

Integration test utility for Flink Stateful Functions.

## What's Tsukuyomi?

Since you're observing this repo, you probably know what's Statefun. But what is Tsukuyomi? I borrowed the word
Tsukuyomi from the fantastic Japanese manga and anime "Naruto". In the world of "Naruto" Tsukuyomi means a super
powerful ninja technique that traps the opponent into an illusion. The opponent cannot identify the illusion nor escape.
This project does the same with the function under test — it puts the function into the fake world with egresses and
other functions. Those fake components capture function state and messages to provide the developer with a clean and
nice API.

## Usage

### Import

```java
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static com.github.f1xman.statefun.tsukuyomi.dsl.StateValue.empty;
import static com.github.f1xman.statefun.tsukuyomi.dsl.StateValue.havingValue;
import static com.github.f1xman.statefun.tsukuyomi.dsl.Tsukuyomi.*;
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
        Message outgoingMessage = MessageBuilder.forAddress(COLLABORATOR, context.self().id())
                .withValue(message.asUtf8String() + bar)
                .build();
        context.send(outgoingMessage);
        return context.done();
    }
}
```

### Create envelopes

```java
    private Envelope outgoingEnvelope() {
        return Envelope.builder()
            .from(Testee.TYPE, FUNCTION_ID)
            .to(COLLABORATOR, FUNCTION_ID)
            .data(Types.stringType(), HELLO + BAR)
            .build();
    }

    private Envelope incomingEnvelope() {
        return Envelope.builder()
            .from(COLLABORATOR, FUNCTION_ID)
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
        Envelope expected = outgoingEnvelope();
        GivenFunction testee = given(
            function(Testee.TYPE, new Testee()),
            withState(Testee.FOO, empty()),
            withState(Testee.BAR, havingValue(BAR))
        );

        when(
            testee,
            receives(envelope)
        ).then(
            expectMessage(is(expected)),
            expectState(Testee.FOO, is("foo"))
        );
   }
```
