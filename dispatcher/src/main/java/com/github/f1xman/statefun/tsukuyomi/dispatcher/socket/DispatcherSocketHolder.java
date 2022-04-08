package com.github.f1xman.statefun.tsukuyomi.dispatcher.socket;

public class DispatcherSocketHolder {

    private static volatile DispatcherSocket socket;

    static void setSocket(DispatcherSocket socket) {
        DispatcherSocketHolder.socket = socket;
    }

    public static DispatcherSocket getSocket() {
        return socket;
    }

}
