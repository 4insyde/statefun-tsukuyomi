# statefun-tsukuyomi
Integration test utility for Flink Stateful Functions

## Usage
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
