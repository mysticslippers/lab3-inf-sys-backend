package me.ifmo.backend.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Pointcut("@annotation(me.ifmo.backend.AOP.LogExecution)")
    public void logExecutionMethods() {
    }

    @Around("logExecutionMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        Object[] args = pjp.getArgs();

        String username = extractUsername(args);

        log.info("SERVICE START {}.{} user={} args={}",
                className, methodName, username, shortenArgs(args));

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("SERVICE END   {}.{} user={} duration={}ms result={}",
                    className, methodName, username, duration, shortenResult(result));

            return result;
        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - start;
            log.error("SERVICE ERROR {}.{} user={} duration={}ms message={}",
                    className, methodName, username, duration, exception.getMessage(), exception);
            throw exception;
        }
    }

    private String extractUsername(Object[] args) {
        return Arrays.stream(args)
                .filter(a -> a instanceof Principal)
                .map(a -> ((Principal) a).getName())
                .findFirst()
                .orElse("anonymous");
    }

    private String shortenArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        return Arrays.stream(args)
                .map(a -> {
                    if (a == null) return "null";
                    if (a instanceof byte[] b) return "byte[" + b.length + "]";
                    String s = a.toString();
                    return s.length() > 200 ? s.substring(0, 197) + "..." : s;
                })
                .toList()
                .toString();
    }

    private String shortenResult(Object result) {
        if (result == null) return "null";
        String s = result.toString();
        return s.length() > 200 ? s.substring(0, 197) + "..." : s;
    }
}
