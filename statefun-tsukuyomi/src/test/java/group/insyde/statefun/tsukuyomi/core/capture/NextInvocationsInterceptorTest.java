package group.insyde.statefun.tsukuyomi.core.capture;

import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NextInvocationsInterceptorTest {

    @Mock
    StatefulFunction functionUnderTest;
    @Mock
    StatefulFunction captureFunction;
    @Mock
    Context context;
    @Mock
    Message message;

    @Test
    void firstInvocationGoesToFunctionUnderTestAndFollowingInvocationsCaptured() throws Throwable {
        NextInvocationsInterceptor interceptor = NextInvocationsInterceptor.of(functionUnderTest, captureFunction);

        interceptor.apply(context, message);
        interceptor.apply(context, message);
        interceptor.apply(context, message);

        then(functionUnderTest).should().apply(context, message);
        then(captureFunction).should(times(2)).apply(context, message);
    }
}