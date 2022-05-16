package group.insyde.statefun.tsukuyomi.dispatcher.socket;

public class DispatcherSocketHolder {

    private static volatile DispatcherSocket socket;

    static void setSocket(DispatcherSocket socket) {
        DispatcherSocketHolder.socket = socket;
    }

    public static DispatcherSocket getSocket() {
        return socket;
    }

}
