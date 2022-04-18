package com.github.f1xman.statefun.tsukuyomi.core.dispatcher;

import com.github.f1xman.statefun.tsukuyomi.core.capture.ModuleDefinition;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static java.util.Objects.requireNonNullElse;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class DispatcherContainer extends GenericContainer<DispatcherContainer> {

    static final int DISPATCHER_PORT = 5555;
    static final int DEBUGGER_PORT = 5005;
    static String FUNCTIONS_ENV = "FUNCTIONS";
    static String ENDPOINT_ENV = "ENDPOINT";
    static String EGRESSES_ENV = "EGRESSES";

    @NonNull
    Integer statefunPort;
    @NonNull
    ModuleDefinition moduleDefinition;

    public DispatcherClient createClient() {
        String host = this.getHost();
        int dispatcherPort = this.getMappedPort(DISPATCHER_PORT);
        return new SocketDispatcherClient(host, dispatcherPort);
    }

    @Override
    protected void configure() {
        super.configure();
        Testcontainers.exposeHostPorts(statefunPort);
        this.withExposedPorts(DEBUGGER_PORT, DISPATCHER_PORT);
        this.waitingFor(Wait.forLogMessage(".*Job status is RUNNING.*", 1));
        this.addEnv(FUNCTIONS_ENV, moduleDefinition.generateFunctionsString());
        this.addEnv(ENDPOINT_ENV, String.format("http://host.testcontainers.internal:%d", statefunPort));
        this.addEnv(EGRESSES_ENV, moduleDefinition.generateEgressesString());
        this.withLogConsumer(new Slf4jLogConsumer(log));
    }

    @Builder
    private DispatcherContainer(DockerImageName image, Integer statefunPort, ModuleDefinition moduleDefinition) {
        super(requireNonNullElse(image, DispatcherImageName.INSTANCE));
        this.statefunPort = statefunPort;
        this.moduleDefinition = moduleDefinition;
    }
}
