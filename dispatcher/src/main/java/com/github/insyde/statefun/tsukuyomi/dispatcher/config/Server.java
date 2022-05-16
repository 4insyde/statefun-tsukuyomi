package com.github.insyde.statefun.tsukuyomi.dispatcher.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class Server {

    static final String LOCALHOST = "localhost";
    static final int INPUT_PORT = 5555;
    static final int OUTPUT_PORT = 6666;
    @Getter
    String hostname;
    @Getter
    int port;

    public static Server getDefaultInputServer() {
        return new Server(LOCALHOST, INPUT_PORT);
    }

    public static Server getDefaultOutputServer() {
        return new Server(LOCALHOST, OUTPUT_PORT);
    }

}
