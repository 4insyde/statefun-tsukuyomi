package com.github.insyde.statefun.tsukuyomi.core.dispatcher;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.testcontainers.utility.DockerImageName;

import java.io.InputStream;
import java.util.Properties;

class DispatcherImageName {

    private static final String IMAGE = "f1xman/statefun-tsukuyomi-dispatcher";
    static final DockerImageName INSTANCE = create();

    @SneakyThrows
    private static DockerImageName create() {
        @Cleanup
        InputStream is = DispatcherContainer.class.getResourceAsStream("/artifact.properties");
        Properties properties = new Properties();
        properties.load(is);
        String version = properties.getProperty("version");
        return DockerImageName.parse(IMAGE + ":" + version);
    }

}
