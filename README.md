# statefun-tsukuyomi

Integration test utility for Flink Stateful Functions.

## What's Tsukuyomi?

Since you're observing this repo, you probably know what's Statefun. But what is Tsukuyomi? I borrowed the word
Tsukuyomi from the fantastic Japanese manga and anime "Naruto". In the world of "Naruto" Tsukuyomi means a super
powerful ninja technique that traps the opponent into an illusion. The opponent cannot identify the illusion nor escape.
This project does the same with the function under test — it puts the function into the fake world with egresses and
other functions. Those fake components capture function state and messages to provide the developer with a clean and
nice API.

## Features
### BDD-style DSL
```java
GivenFunction testee = given(
    // Define your function under test        
);

when(
    // Define interaction with the function
).then(
    // Verify your expectations
);
```
### Initial state
```java
GivenFunction testee = given(
    function(Testee.TYPE, new Testee()),
    withState(Testee.FOO, empty()),
    withState(Testee.BAR, havingValue(BAR))
);
```
### Verification of outgoing messages
```java
then(
    expectMessage(expectedToFunction),
    expectEgressMessage(expectedToEgress),
    expectMessage(expectedToSelf),
);
```
### Verification of message order
When a function sends multiple messages to the **same destination** (e.g. the same egress, or another function of the same 
type and id), it might be essential to ensure that messages have a specific order. For instance, CREATE operation goes 
before the UPDATE or DELETE. Statefun Tsukuyomi validates that the order of outgoing messages is the same as you declare
it in a then(..) block.
**Statefun Tsukuyomi does not verify the order of messages with different destinations** since real-life use cases rarely 
require it due to the async nature of event-driven applications.
```java
then(
    // Verifies the target function receives this message first
    expectMessage(expectedToTheSameFunction),
    // Does not care about the order
    expectEgressMessage(expectedToEgress),
    // Verifies the target function then receives this message
    expectMessage(expectedToTheSameFunction),
);
```
### Verification of state after interaction
```java
.then(
    expectState(Testee.FOO, is("foo")) // Hamcrest matchers supported
);
```
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
Envelope is an object that describes a Message. Envelope has three main parts:
#### From (Optional)
Who sends this message. Do not set if message sender is an ingress.
#### To (Mandatory)
Who receives this message. Destination can be either function or egress.
#### Data (Mandatory)
A message content.
```java
private Envelope outgoingEnvelopeToSelf() {
    return Envelope.builder()
        .from(Testee.TYPE, FUNCTION_ID)
        .to(Testee.TYPE, FUNCTION_ID)
        .data(Types.stringType(), HELLO + BAR)
        .build();
}
private Envelope outgoingEnvelopeToEgress() {
    return Envelope.builder()
        .toEgress(EGRESS)
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
@Timeout(60)
void exchangesMessages() {
    // Define your envelopes
    Envelope envelope = incomingEnvelope();
    Envelope expectedToFunction = outgoingEnvelopeToFunction();
    Envelope expectedToEgress = outgoingEnvelopeToEgress();
    Envelope expectedToSelf = outgoingEnvelopeToSelf();
    // Define function under test and its initial state
    GivenFunction testee = given(
        function(Testee.TYPE, new Testee()),
        withState(Testee.FOO, empty()), // Empty values must be defined as well
        withState(Testee.BAR, havingValue(BAR))
    );

    // When function under test receives that envelope
    when(
        testee,
        receives(envelope)
    ).then(
        // Then expect it sends the following messages
        expectMessage(expectedToFunction),
        expectEgressMessage(expectedToEgress),
        expectMessage(expectedToSelf),
        // and has the following state value after invocation
        expectState(Testee.FOO, is("foo")) // Hamcrest matchers supported
    );
}
```
