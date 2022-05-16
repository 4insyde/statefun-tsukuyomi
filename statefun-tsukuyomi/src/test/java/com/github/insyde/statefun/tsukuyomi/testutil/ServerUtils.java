package com.github.insyde.statefun.tsukuyomi.testutil;

import com.github.insyde.statefun.tsukuyomi.core.capture.StatefunModule;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.ServerSocket;

@UtilityClass
public class ServerUtils {
    public static boolean isPortAvailable(Integer port) {
        boolean canBind;
        try (var ignore = new ServerSocket(port)) {
            canBind = true;
        } catch (IOException e) {
            canBind = false;
        }
        return canBind;
    }

    public static boolean isStatefunModuleRunning(StatefunModule statefunModule) {
        Integer port = statefunModule.getPort();
        return !isPortAvailable(port);
    }
}
