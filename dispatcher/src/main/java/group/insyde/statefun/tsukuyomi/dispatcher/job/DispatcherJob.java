package group.insyde.statefun.tsukuyomi.dispatcher.job;

import group.insyde.statefun.tsukuyomi.dispatcher.config.DispatcherConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.statefun.flink.core.StatefulFunctionsConfig;
import org.apache.flink.statefun.flink.core.message.RoutableMessage;
import org.apache.flink.statefun.flink.datastream.StatefulFunctionDataStreamBuilder;
import org.apache.flink.statefun.flink.datastream.StatefulFunctionEgressStreams;
import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.io.EgressIdentifier;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.flink.statefun.flink.core.message.MessageFactoryType.WITH_KRYO_PAYLOADS;
import static org.apache.flink.statefun.flink.datastream.RequestReplyFunctionBuilder.requestReplyFunctionBuilder;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class DispatcherJob implements FlinkDispatcherJob {

    static final EgressIdentifier<TypedValue> CAPTURED_MESSAGES = new EgressIdentifier<>(
            "group.insyde.statefun.tsukuyomi", "captured-messages", TypedValue.class);

    DispatcherConfig config;

    @SneakyThrows
    public JobClient start() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStateBackend(new EmbeddedRocksDBStateBackend());

        DataStream<Envelope> input = env.addSource(new DispatcherSocketSource());
        DataStream<RoutableMessage> ingress = input.map(Envelope::toRoutableMessage);

        StatefulFunctionsConfig statefunConfig = StatefulFunctionsConfig.fromEnvironment(env);
        statefunConfig.setFactoryType(WITH_KRYO_PAYLOADS);
        StatefulFunctionDataStreamBuilder statefunBuilder = StatefulFunctionDataStreamBuilder.builder("statefun")
                .withDataStreamAsIngress(ingress)
                .withEgressId(CAPTURED_MESSAGES)
                .withConfiguration(statefunConfig);
        bindFunctions(config, statefunBuilder);

        Set<EgressIdentifier<TypedValue>> egressIdentifiers = config.getEgressIdentifiers();
        egressIdentifiers.forEach(statefunBuilder::withEgressId);

        StatefulFunctionEgressStreams statefunStreams = statefunBuilder.build(env);

        DispatcherSocketSink sink = new DispatcherSocketSink();
        DataStream<Envelope> toFunctionEnvelopes = statefunStreams
                .getDataStreamForEgressId(CAPTURED_MESSAGES)
                .map(t -> Envelope.fromJson(new String(t.getValue().toByteArray(), StandardCharsets.UTF_8)));

        DataStream<Envelope> envelopes = egressIdentifiers.stream()
                .peek(e -> log.info("Configuring custom egress {}/{}", e.namespace(), e.name()))
                .map(e -> statefunStreams.getDataStreamForEgressId(e)
                        .map(t -> {
                                    log.info("Captured a message sent to egress {}/{}", e.namespace(), e.name());
                                    return Envelope.builder()
                                            .to(e.namespace(), e.name())
                                            .data(t)
                                            .build();
                                }
                        ))
                .map(s -> (DataStream<Envelope>) s)
                .reduce(toFunctionEnvelopes, DataStream::union);
        envelopes.addSink(sink);

        return env.executeAsync("statefun-tsukuyomi");
    }

    private void bindFunctions(DispatcherConfig config, StatefulFunctionDataStreamBuilder statefunBuilder) {
        Set<FunctionType> functionTypes = config.getFunctionTypes();
        for (FunctionType type : functionTypes) {
            statefunBuilder.withRequestReplyRemoteFunction(requestReplyFunctionBuilder(type, config.getEndpoint()));
        }
    }

}
