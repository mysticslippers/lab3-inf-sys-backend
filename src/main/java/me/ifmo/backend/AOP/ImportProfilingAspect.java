package me.ifmo.backend.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ImportProfilingAspect {

    @Pointcut("@annotation(me.ifmo.backend.AOP.ProfileImport)")
    public void profileImportOperation() {
    }

    @Around("profileImportOperation()")
    public Object measureImportTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("IMPORT OK   {}.{} took {} ms, result={}",
                    pjp.getSignature().getDeclaringType().getSimpleName(),
                    pjp.getSignature().getName(),
                    duration,
                    result);

            return result;
        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - start;

            log.warn("IMPORT FAIL {}.{} failed after {} ms: {}",
                    pjp.getSignature().getDeclaringType().getSimpleName(),
                    pjp.getSignature().getName(),
                    duration,
                    exception.getMessage());

            throw exception;
        }
    }
}
