package hello.proxy.config.v2_dynamicproxy.handler;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.util.PatternMatchUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LogTraceFilterHandler implements InvocationHandler {

    private final Object target;
    private final LogTrace logTrace;
    private final String[] partterns;

    public LogTraceFilterHandler(Object target, LogTrace logTrace, String[] partterns) {
        this.target = target;
        this.logTrace = logTrace;
        this.partterns = partterns;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //메서드 이름 필터
        String methodName = method.getName();
        //save,request,reque*,*est
        if(!PatternMatchUtils.simpleMatch(partterns,methodName)){
            return method.invoke(target,args); // logtrace 없이 실제 메서드 바로 실행
        }

        TraceStatus status = null;
        try {
            String message =  method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
            status = logTrace.begin(message);

            //로직 호출
            Object result = method.invoke(target,args);
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
