# statefun-tsukuyomi
Integration test utility for Flink Stateful Functions

## Usage
### Import
```java
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.testutil.IntegrationTest;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;

import static com.github.f1xman.statefun.tsukuyomi.api.StateValue.empty;
import static com.github.f1xman.statefun.tsukuyomi.api.StateValue.havingValue;
import static com.github.f1xman.statefun.tsukuyomi.api.Tsukuyomi.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.is;
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
                expectMessage(is(expected))
        );
    }
```
