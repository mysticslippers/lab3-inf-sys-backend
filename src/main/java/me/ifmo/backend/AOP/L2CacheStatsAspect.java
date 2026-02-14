package me.ifmo.backend.AOP;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class L2CacheStatsAspect {

    private final EntityManagerFactory emf;

    @Value("${app.cache.l2.stats.enabled:false}")
    private boolean enabled;

    @Around("@annotation(me.ifmo.backend.AOP.LogL2CacheStats) || @within(me.ifmo.backend.AOP.LogL2CacheStats)")
    public Object logL2Stats(ProceedingJoinPoint pjp) throws Throwable {
        if (!enabled) return pjp.proceed();

        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();

        Snapshot before = Snapshot.capture(stats);

        Throwable error = null;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            Snapshot after = Snapshot.capture(stats);
            Snapshot delta = after.minus(before);

            String method = pjp.getSignature().toShortString();
            log.info("[L2CACHE] {} | hits={} misses={} puts={}", method, delta.hits, delta.misses, delta.puts);

            delta.topRegions(5).forEach(r ->
                    log.info("[L2CACHE]   region={} | hits={} misses={} puts={}", r.region, r.hits, r.misses, r.puts)
            );

            if (error != null) {
                log.info("[L2CACHE] {} ended with exception: {}", method, error.getClass().getSimpleName());
            }
        }
    }

    private record RegionDelta(String region, long hits, long misses, long puts) {}

    private static final class Snapshot {
        private final long hits;
        private final long misses;
        private final long puts;
        private final Map<String, RegionDelta> regions;

        private Snapshot(long hits, long misses, long puts, Map<String, RegionDelta> regions) {
            this.hits = hits;
            this.misses = misses;
            this.puts = puts;
            this.regions = regions;
        }

        static Snapshot capture(Statistics stats) {
            String[] names = stats.getSecondLevelCacheRegionNames();

            Map<String, RegionDelta> perRegion = new HashMap<>();
            long totalHits = 0, totalMisses = 0, totalPuts = 0;

            for (String region : names) {
                Object regionStats = getRegionStats(stats, region);
                long h = callLong(regionStats, "getHitCount");
                long m = callLong(regionStats, "getMissCount");
                long p = callLong(regionStats, "getPutCount");

                perRegion.put(region, new RegionDelta(region, h, m, p));
                totalHits += h;
                totalMisses += m;
                totalPuts += p;
            }

            return new Snapshot(totalHits, totalMisses, totalPuts, perRegion);
        }

        Snapshot minus(Snapshot other) {
            Map<String, RegionDelta> deltaRegions = new HashMap<>();
            Set<String> all = new HashSet<>();
            all.addAll(this.regions.keySet());
            all.addAll(other.regions.keySet());

            for (String r : all) {
                RegionDelta a = this.regions.getOrDefault(r, new RegionDelta(r, 0, 0, 0));
                RegionDelta b = other.regions.getOrDefault(r, new RegionDelta(r, 0, 0, 0));
                deltaRegions.put(r, new RegionDelta(r, a.hits - b.hits, a.misses - b.misses, a.puts - b.puts));
            }

            return new Snapshot(
                    this.hits - other.hits,
                    this.misses - other.misses,
                    this.puts - other.puts,
                    deltaRegions
            );
        }

        List<RegionDelta> topRegions(int n) {
            return regions.values().stream()
                    .filter(r -> (r.hits + r.misses + r.puts) > 0)
                    .sorted(Comparator.comparingLong((RegionDelta r) -> (r.hits + r.misses + r.puts)).reversed())
                    .limit(n)
                    .collect(Collectors.toList());
        }

        private static Object getRegionStats(Statistics stats, String region) {
            Object r = call(stats, "getSecondLevelCacheStatistics", region);
            if (r != null) return r;
            return call(stats, "getCacheRegionStatistics", region);
        }

        private static Object call(Object target, String method, String arg) {
            if (target == null) return null;
            try {
                Method m = target.getClass().getMethod(method, String.class);
                return m.invoke(target, arg);
            } catch (Exception exception) {
                return null;
            }
        }

        private static long callLong(Object target, String method) {
            if (target == null) return 0;
            try {
                Method m = target.getClass().getMethod(method);
                Object v = m.invoke(target);
                if (v instanceof Number num) return num.longValue();
                return 0;
            } catch (Exception exception) {
                return 0;
            }
        }
    }
}
