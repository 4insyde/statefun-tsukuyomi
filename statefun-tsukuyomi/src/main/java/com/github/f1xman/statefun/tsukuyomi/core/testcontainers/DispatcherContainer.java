package com.github.f1xman.statefun.tsukuyomi.core.testcontainers;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class DispatcherContainer extends GenericContainer<DispatcherContainer> {

    private DispatcherContainer(@NonNull DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    @SneakyThrows
    public static DispatcherContainer create() {
        @Cleanup
        InputStream is = DispatcherContainer.class.getResourceAsStream("/artifact.properties");
        Properties properties = new Properties();
        properties.load(is);
        String version = properties.getProperty("version");
        return new DispatcherContainer(DockerImageName.parse("f1xman/statefun-tsukuyomi-dispatcher:" + version))
                .withExposedPorts(5005, 5555)
                .waitingFor(Wait.forLogMessage(".*Job status is RUNNING.*", 1));
    }
}
