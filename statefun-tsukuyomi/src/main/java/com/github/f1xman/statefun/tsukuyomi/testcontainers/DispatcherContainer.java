package com.github.f1xman.statefun.tsukuyomi.testcontainers;

import lombok.NonNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class DispatcherContainer extends GenericContainer<DispatcherContainer> {

    private DispatcherContainer(@NonNull DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    public static DispatcherContainer create() {
        return new DispatcherContainer(DockerImageName.parse("statefun-tsukuyomi-dispatcher"))
                .withExposedPorts(5005, 5555)
                .waitingFor(Wait.forLogMessage(".*Job status is RUNNING.*", 1));
    }
}
