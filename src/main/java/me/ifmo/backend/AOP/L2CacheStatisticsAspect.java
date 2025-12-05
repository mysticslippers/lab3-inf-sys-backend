package me.ifmo.backend.AOP;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ifmo.backend.config.L2CacheProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class L2CacheStatisticsAspect {

    private final EntityManagerFactory entityManagerFactory;
    private final L2CacheProperties l2CacheProperties;

    @Around("execution(* me.ifmo.backend.services..*(..))")
    public Object logL2CacheStats(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();

        if (!l2CacheProperties.isLogStatistics()) {
            return result;
        }

        Statistics stats = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();

        log.info("L2CACHE after {}.{}: hits={}, misses={}, puts={}",
                pjp.getSignature().getDeclaringType().getSimpleName(),
                pjp.getSignature().getName(),
                stats.getSecondLevelCacheHitCount(),
                stats.getSecondLevelCacheMissCount(),
                stats.getSecondLevelCachePutCount()
        );

        return result;
    }
}
