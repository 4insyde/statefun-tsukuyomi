package com.github.f1xman.statefun.tsukuyomi.dispatcher.job;

import com.github.f1xman.statefun.tsukuyomi.dispatcher.config.StatefunModule;
import com.github.f1xman.statefun.tsukuyomi.dispatcher.config.DispatcherConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
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
public class DispatcherJob implements FlinkDispatcherJob {

    static final EgressIdentifier<TypedValue> CAPTURED_MESSAGES = new EgressIdentifier<>(
            "com.github.f1xman.statefun.tsukuyomi", "captured-messages", TypedValue.class);

    DispatcherConfig config;

    @SneakyThrows
    public JobClient start() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Envelope> input = env.addSource(new DispatcherSocketSource());
        DataStream<RoutableMessage> ingress = input.map(Envelope::toRoutableMessage);

        StatefulFunctionsConfig statefunConfig = StatefulFunctionsConfig.fromEnvironment(env);
        statefunConfig.setFactoryType(WITH_KRYO_PAYLOADS);
        StatefulFunctionDataStreamBuilder statefunBuilder = StatefulFunctionDataStreamBuilder.builder("statefun")
                .withDataStreamAsIngress(ingress)
                .withEgressId(CAPTURED_MESSAGES)
                .withConfiguration(statefunConfig);
        bindFunctions(config, statefunBuilder);
        StatefulFunctionEgressStreams statefunStreams = statefunBuilder.build(env);

        statefunStreams
                .getDataStreamForEgressId(CAPTURED_MESSAGES)
                .map(t -> Envelope.fromJson(new String(t.getValue().toByteArray(), StandardCharsets.UTF_8)))
                .addSink(new DispatcherSocketSink());

        return env.executeAsync("zorbility");
    }

    private void bindFunctions(DispatcherConfig config, StatefulFunctionDataStreamBuilder statefunBuilder) {
        StatefunModule module = config.getModule();
        Set<FunctionType> functionTypes = module.getFunctionTypes();
        for (FunctionType type : functionTypes) {
            statefunBuilder.withRequestReplyRemoteFunction(requestReplyFunctionBuilder(type, module.getEndpoint()));
        }
    }

}
