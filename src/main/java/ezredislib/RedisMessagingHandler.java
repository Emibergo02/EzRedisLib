/*
 * EzRedisLib - A redis library.
 * Copyright (C) 2022 Emiliano Bergonzani
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Contact e-mail: emibergo@gmail.com
 */

package ezredislib;

import com.google.gson.Gson;
import ezredislib.channel.PubSubListener;
import ezredislib.packet.MessagingPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class RedisMessagingHandler {

    private static final List<PubSubListener<?>> channelListeners = Collections.synchronizedList(new ArrayList<>());
    private final JedisPool pool;
    private final Gson gson = new Gson();
    private final ExecutorService scheduler;


    /**
     * Creates a new RedisMessagingHandler.
     * @param host The host of the redis server.
     * @param port The port of the redis server.
     * @param user The user of the redis server (null for none).
     * @param pass The password of the redis server (null for none).
     * @throws InstantiationException If the connection to the redis server fails.
     */
    public RedisMessagingHandler(@NotNull String host, int port, @Nullable String user, @Nullable String pass) throws InstantiationException {
        scheduler = new ForkJoinPool();
        pool = new JedisPool(buildPoolConfig(), host, port, user, pass);

        if(!testConnection()){
            throw new InstantiationException("Could not connect to redis server (down or inaccessible)");
        }
    }
    /**
     * Creates a new RedisMessagingHandler.
     * @param poolConfig The pool config of the redis client.
     * @param host The host of the redis server.
     * @param port The port of the redis server.
     * @param user The user of the redis server (null for none).
     * @param pass The password of the redis server (null for none).
     * @throws InstantiationException If the connection to the redis server fails.
     */
    public RedisMessagingHandler(@NotNull JedisPoolConfig poolConfig,@NotNull String host, int port, @Nullable String user, @Nullable String pass) throws InstantiationException {
        scheduler = new ForkJoinPool();
        pool = new JedisPool(poolConfig, host, port, user, pass);

        if(!testConnection()){
            throw new InstantiationException("Could not connect to redis server (down or inaccessible)");
        }
    }

    /**
     * Test if redis is up and running.
     * @return false if redis is down or inaccessible.
     */
    private boolean testConnection() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.isConnected();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
    /**
     * Registers incoming channel packets
     * @param pubSubListener The channel to register.
     * @return true if the channel is registered successfully.
     */
    public boolean registerChannelListener(@NotNull PubSubListener<?> pubSubListener) {
        if (pubSubListener.getChannelName().trim().length() > 8) {
            return false;
        }
        channelListeners.add(pubSubListener);
        scheduler.execute(() ->{
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(pubSubListener, pubSubListener.getChannelName());
            }
        });
        return true;
    }
    /**
     * Unsubscribe listeners listening to a channel
     * @param channelName The channel to unregister.
     * @return true if the channel is unregistered successfully.
     */
    public boolean unregisterFromChannel(String channelName) {
        if (channelName.trim().length() > 8) {
            return false;
        }
        Iterator<PubSubListener<?>> listenerIterator=channelListeners.iterator();
        while(listenerIterator.hasNext()){
            PubSubListener<?> listener=listenerIterator.next();
            if(listener.getChannelName().equals(channelName)){
                listener.unsubscribe();
                listenerIterator.remove();
            }
        }

        return true;
    }

    public List<PubSubListener<?>> getChannelListeners(String channelName){
        List<PubSubListener<?>> listeners=new ArrayList<>();
        for(PubSubListener<?> listener:channelListeners){
            if(listener.getChannelName().equals(channelName)){
                listeners.add(listener);
            }
        }
        return listeners;
    }

    public boolean isChannelRegistered(String channelName){
        for(PubSubListener<?> listener:channelListeners){
            if(listener.getChannelName().equals(channelName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Publish packet to channel
     * @param channel channel to publish to.
     * @param message message to publish.
     */
    public void sendPacket(String channel, MessagingPacket message) {
            publish(channel, message);

    }
    public void sendPacketAsync(String channel, MessagingPacket message) {
            scheduler.execute(() -> sendPacket(channel, message));
    }

    public void sendPackets(String channel, List<MessagingPacket> messages) {
            messages.forEach(message -> publish(channel, message));
    }
    public void sendPacketsAsync(String channel, List<MessagingPacket> messages) {
            scheduler.execute(() -> sendPackets(channel, messages));

    }

    private void publish(String channel, @NotNull MessagingPacket message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, gson.toJson(message));
        }
    }

    public static @NotNull JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
    public static @NotNull JedisPoolConfig buildPoolConfig(int totalPoolConnections,int maxIdleConnections,int minIdleConnections) {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(totalPoolConnections);
        poolConfig.setMaxIdle(maxIdleConnections);
        poolConfig.setMinIdle(minIdleConnections);
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
