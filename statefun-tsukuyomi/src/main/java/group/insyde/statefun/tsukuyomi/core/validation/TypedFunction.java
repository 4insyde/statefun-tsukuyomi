package group.insyde.statefun.tsukuyomi.core.validation;

import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;

public interface TypedFunction {

    TypeName getTypeName();

    StatefulFunction getInstance();

}
