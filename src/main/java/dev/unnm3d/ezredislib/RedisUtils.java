package dev.unnm3d.ezredislib;

import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisUtils {

    /**
     * Creates a new JedisPoolConfig with default parameters.
     *
     * @return the generated config
     */
    public static @NotNull JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(32);
        poolConfig.setMinIdle(16);
        poolConfig.setMaxWait(Duration.ofSeconds(10));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    /**
     * Creates a new JedisPoolConfig custom parameters
     *
     * @param totalPoolConnections The maximum number of connections in the pool.
     * @param maxIdleConnections   The maximum number of idle connections in the pool.
     * @param minIdleConnections   The minimum number of idle connections in the pool.
     * @return the generated config
     */
    public static @NotNull JedisPoolConfig buildPoolConfig(int totalPoolConnections, int maxIdleConnections, int minIdleConnections) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(totalPoolConnections);
        poolConfig.setMaxIdle(maxIdleConnections);
        poolConfig.setMinIdle(minIdleConnections);
        poolConfig.setMaxWait(Duration.ofSeconds(10));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}
