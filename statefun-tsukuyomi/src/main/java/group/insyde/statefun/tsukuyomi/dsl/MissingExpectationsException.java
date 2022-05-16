package group.insyde.statefun.tsukuyomi.dsl;

public class MissingExpectationsException extends RuntimeException {
    public MissingExpectationsException(String message) {
        super(message);
    }
}
