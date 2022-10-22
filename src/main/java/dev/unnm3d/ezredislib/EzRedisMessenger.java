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

package dev.unnm3d.ezredislib;

import com.google.gson.Gson;
import dev.unnm3d.ezredislib.channel.PubSubJsonListener;
import dev.unnm3d.ezredislib.channel.PubSubListener;
import dev.unnm3d.ezredislib.channel.PubSubObjectListener;
import dev.unnm3d.ezredislib.packet.MessagingPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class EzRedisMessenger {

    private static final List<PubSubListener> channelListeners = Collections.synchronizedList(new ArrayList<>());
    private final JedisPool pool;
    private final Gson gson = new Gson();
    private final ExecutorService scheduler;


    /**
     * Creates a new EzRedisMessenger.
     *
     * @param host The host of the redis server.
     * @param port The port of the redis server.
     * @param user The user of the redis server (null for none).
     * @param pass The password of the redis server (null for none).
     * @throws InstantiationException If the connection to the redis server fails.
     */
    public EzRedisMessenger(@NotNull String host, int port, @Nullable String user, @Nullable String pass) throws InstantiationException {
        scheduler = Executors.newCachedThreadPool();

        pool = new JedisPool(RedisUtils.buildPoolConfig(), host, port, user, pass);

        if (!testConnection()) {
            throw new InstantiationException("Could not connect to redis server (down or inaccessible)");
        }
    }

    /**
     * Creates a new EzRedisMessenger.
     *
     * @param host       The host of the redis server.
     * @param port       The port of the redis server.
     * @param user       The user of the redis server (null for none).
     * @param password   The password of the redis server (null for none).
     * @param timeout    Timeout of the connection if it is idle.
     * @param database   The database to use.
     * @param clientName The name of the client.
     * @throws InstantiationException If the connection to the redis server fails.
     */
    public EzRedisMessenger(@NotNull String host, int port, @Nullable String user, @Nullable String password, int timeout, int database, String clientName) throws InstantiationException {

        scheduler = Executors.newCachedThreadPool();

        pool = new JedisPool(RedisUtils.buildPoolConfig(), host, port, timeout, user, password, database, clientName);

        if (!testConnection()) {
            throw new InstantiationException("Could not connect to redis server (down or inaccessible)");
        }


    }

    /**
     * Creates a new EzRedisMessenger.
     *
     * @param host The host of the redis server.
     * @param port The port of the redis server.
     * @throws InstantiationException If the connection to the redis server fails.
     */
    public EzRedisMessenger(@NotNull String host, int port) throws InstantiationException {
        scheduler = Executors.newCachedThreadPool();
        pool = new JedisPool(RedisUtils.buildPoolConfig(), host, port, null, null);

        if (!testConnection()) {
            throw new InstantiationException("Could not connect to redis server (down or inaccessible)");
        }
    }

    /**
     * Creates a new EzRedisMessenger.
     *
     * @param poolConfig The pool config of the redis client.
     * @param host       The host of the redis server.
     * @param port       The port of the redis server.
     * @param user       The user of the redis server (null for none).
     * @param pass       The password of the redis server (null for none).
     * @throws InstantiationException If the connection to the redis server fails.
     */
    public EzRedisMessenger(@NotNull JedisPoolConfig poolConfig, @NotNull String host, int port, @Nullable String user, @Nullable String pass) throws InstantiationException {
        scheduler = Executors.newCachedThreadPool();
        pool = new JedisPool(poolConfig, host, port, user, pass);

        if (!testConnection()) {
            throw new InstantiationException("Could not connect to redis server (down or inaccessible)");
        }
    }

    /**
     * Test if redis is up and running.
     *
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
     *
     * @param rpf     The packet listener.
     * @param channel The channel to listen to.
     * @return true if the channel is registered successfully.
     */
    public PubSubJsonListener registerChannelListener(String channel, PubSubJsonListener.ReadPacketFunction rpf) {
        PubSubJsonListener pubSubListener = new PubSubJsonListener(channel, rpf);
        channelListeners.add(pubSubListener);
        scheduler.execute(() -> subWithRestart(pubSubListener, channel));

        return pubSubListener;
    }

    /**
     * Registers incoming channel packets
     *
     * @param rpf         The packet listener.
     * @param channel     The channel to listen to.
     * @param classFilter The class of the incoming packets. No other class are permitted
     * @return the PubSubListener registered
     */
    public PubSubJsonListener registerChannelListener(String channel, PubSubJsonListener.ReadPacketFunction rpf, Class<?> classFilter) {
        PubSubJsonListener pubSubListener = new PubSubJsonListener(channel, rpf, classFilter);
        channelListeners.add(pubSubListener);
        scheduler.execute(() -> subWithRestart(pubSubListener, channel));

        return pubSubListener;
    }

    /**
     * Registers incoming channel packets
     *
     * @param rpf     The packet listener.
     * @param channel The channel to listen to.
     * @return the PubSubObjectListener registered
     */
    public PubSubObjectListener registerChannelObjectListener(String channel, PubSubObjectListener.ReadPacketFunction rpf) {
        PubSubObjectListener pubSubObjectListener = new PubSubObjectListener(rpf, channel);
        channelListeners.add(pubSubObjectListener);
        scheduler.execute(() -> subWithRestart(pubSubObjectListener, channel.getBytes(StandardCharsets.US_ASCII)));
        return pubSubObjectListener;
    }

    /**
     * Actual registration with subscription.
     * Restarts on the same thread on error
     */
    public void subWithRestart(PubSubJsonListener psl, String channel) {
        if (!pool.isClosed())
            try (Jedis jedis = pool.getResource()) {
                System.out.println("Subscribing to channel " + channel);
                jedis.subscribe(psl, channel);

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Subscription error channel: " + channel+" retrying in 2 seconds");
                try {
                    Thread.sleep(2000);
                    System.out.println(getThreadPoolStatus());
                    System.out.println(getJedisPoolStatus());
                    if (channelListeners.stream().anyMatch(psl::equals))
                        subWithRestart(psl, channel);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
    }

    /**
     * Actual registration with subscription.
     * Restarts on the same thread on error
     */
    public void subWithRestart(BinaryJedisPubSub psl, byte[] channel) {
        if (!pool.isClosed())
            try (Jedis jedis = pool.getResource()) {
                System.out.println("Subscribing to channel " + Arrays.toString(channel));
                jedis.subscribe(psl, channel);
            }catch (Exception e){//Restart on failure
                e.printStackTrace();
                System.out.println("Subscription error channel: " + new String(channel) +" retrying in 2 seconds");
                try {
                    Thread.sleep(2000);
                    System.out.println(getThreadPoolStatus());
                    System.out.println(getJedisPoolStatus());
                    if (channelListeners.stream().anyMatch(psl::equals))
                        subWithRestart(psl, channel);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

    }

    /**
     * Registers incoming channel packets
     *
     * @param channel     the channel to listen
     * @param rpf         packet read function
     * @param classFilter filters incoming packets. they must be subclasses of this class
     * @return the PubSubObjectListener registered
     */
    public PubSubObjectListener registerChannelObjectListener(String channel, PubSubObjectListener.ReadPacketFunction rpf, Class<?> classFilter) {
        PubSubObjectListener pubSubObjectListener = new PubSubObjectListener(rpf, classFilter, channel);
        channelListeners.add(pubSubObjectListener);
        scheduler.execute(() -> subWithRestart(pubSubObjectListener, channel.getBytes(StandardCharsets.US_ASCII)));

        return pubSubObjectListener;
    }

    /**
     * Unsubscribe listeners listening to a channel
     *
     * @param channelName The channel to unregister.
     */
    public void unregisterFromChannel(String channelName) {
        Iterator<PubSubListener> listenerIterator = channelListeners.iterator();
        while (listenerIterator.hasNext()) {
            PubSubListener listener = listenerIterator.next();
            if (listener.getChannelName().equals(channelName)) {
                if(listener instanceof PubSubJsonListener j)
                    j.unsubscribe();
                else if(listener instanceof PubSubObjectListener o)
                    o.unsubscribe();
                listenerIterator.remove();
            }
        }

    }

    /**
     * Get all listeners listening on a channel
     *
     * @param channelName The channel to get listeners.
     */
    public List<PubSubListener> getChannelListeners(String channelName) {
        List<PubSubListener> listeners = new ArrayList<>();
        for (PubSubListener listener : channelListeners) {
            if (listener.getChannelName().equals(channelName)) {
                listeners.add(listener);
            }
        }
        return listeners;
    }

    /**
     * Checks if a channel is registered.
     *
     * @param channelName The channel to check.
     * @return true if the channel is already registered.
     */
    public boolean isChannelRegistered(String channelName) {
        for (PubSubListener listener : channelListeners) {
            if (listener.getChannelName().equals(channelName)) {
                return true;
            }
        }
        for (PubSubListener listener : channelListeners) {
            if (listener.getChannelName().equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a redis connection from the pool.
     * This is for who wants to use the redis client for storage.
     *
     * @return The redis connection.
     */
    public Jedis getJedis() {
        if (pool.isClosed()) {
            throw new IllegalStateException("Redis pool is closed");
        }
        return pool.getResource();
    }

    /**
     * Returns resource requested in the function (with timeout 1)
     * It is blocking operation
     *
     * @param thing   jedis operation
     * @param timeout timeout in milliseconds
     * @param verbose if true, it will print stacktrace if error occurs
     * @return result of the operation, null if timeouts or an error occurs
     */
    public <R> @Nullable R jedisResource(@NotNull Function<Jedis, R> thing, long timeout, boolean verbose) {
        try {
            return CompletableFuture.supplyAsync(this::getJedis).thenApply(jedis -> {
                R a = thing.apply(jedis);
                pool.returnResource(jedis);
                return a;
            }).completeOnTimeout(null, timeout, TimeUnit.MILLISECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            if (verbose)
                e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns resource requested in the function (with timeout 1)
     * It is blocking operation
     * Default verbose is false, timeout 1 second
     *
     * @param thing jedis operation
     * @return result of the operation, null if timeouts or an error occurs
     */
    public <R> @Nullable R jedisResource(@NotNull Function<Jedis, R> thing) {
        return jedisResource(thing, 1000, false);
    }

    /**
     * Returns a future of the resource requested in the function
     *
     * @param thing   jedis operation
     * @param timeout timeout in milliseconds
     * @return future of the result of the operation, null if timeouts or an error occurs
     */
    public <R> CompletableFuture<R> jedisResourceFuture(@NotNull Function<Jedis, R> thing, long timeout) {
        return CompletableFuture.supplyAsync(this::getJedis).thenApply(jedis -> {
            R a = thing.apply(jedis);
            pool.returnResource(jedis);
            return a;
        }).completeOnTimeout(null, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a future of the resource requested in the function (with timeout 1)
     *
     * @param thing jedis operation
     * @return future of the result of the operation, null if timeouts
     */
    public <R> CompletableFuture<R> jedisResourceFuture(@NotNull Function<Jedis, R> thing) {
        return jedisResourceFuture(thing, 1000);
    }

    public void destroy() {
        channelListeners.forEach(listener->{
            if(listener instanceof PubSubJsonListener j)
                j.unsubscribe();
            else if(listener instanceof PubSubObjectListener o)
                o.unsubscribe();
        });
        channelListeners.clear();
        pool.close();
        scheduler.shutdown();
    }

    /**
     * Publish packet to channel
     *
     * @param channel channel to publish to.
     * @param message message to publish.
     * @return how many clients received the message.
     */
    public long sendPacket(String channel, MessagingPacket message) {
        return publish(channel, message);
    }

    /**
     * Publish packet to channel
     *
     * @param channel    channel to publish to.
     * @param objMessage message to publish.
     * @return how many clients received the message.
     */
    public long sendObjectPacket(String channel, Object objMessage) {
        return publishBytes(channel, objMessage);
    }

    /**
     * Publish packet to channel asynchronously
     *
     * @param channel    channel to publish to.
     * @param objMessage message to publish.
     */
    public void sendObjectPacketAsync(String channel, Object objMessage) {
        scheduler.execute(() -> sendObjectPacket(channel, objMessage));
    }

    /**
     * Publish packet to channel asynchronously
     *
     * @param channel channel to publish to.
     * @param message message to publish.
     */
    public void sendPacketAsync(String channel, MessagingPacket message) {
        scheduler.execute(() -> sendPacket(channel, message));
    }

    /**
     * Publish multiple packets to channel
     *
     * @param channel  channel to publish to.
     * @param messages messages to publish.
     */
    public void sendPackets(String channel, List<MessagingPacket> messages) {
        messages.forEach(message -> publish(channel, message));
    }

    /**
     * Publish multiple packets to channel
     *
     * @param channel    channel to publish to.
     * @param objectList messages to publish.
     */
    public void sendObjectPackets(String channel, List<Object> objectList) {
        objectList.forEach(message -> publishBytes(channel, message));
    }

    /**
     * Publish multiple packets to channel asynchronously
     *
     * @param channel  channel to publish to.
     * @param messages messages to publish.
     */
    public void sendPacketsAsync(String channel, List<MessagingPacket> messages) {
        scheduler.execute(() -> sendPackets(channel, messages));

    }

    /**
     * Publish multiple packets to channel asynchronously
     *
     * @param channel    channel to publish to.
     * @param objectList messages to publish.
     */
    public void sendObjectPacketsAsync(String channel, List<Object> objectList) {
        scheduler.execute(() -> sendObjectPackets(channel, objectList));

    }

    private long publish(String channel, @NotNull MessagingPacket message) {
        if (!pool.isClosed())
            try (Jedis jedis = pool.getResource()) {
                return jedis.publish(channel, gson.toJson(message));
            } catch (Exception exception) {
                exception.printStackTrace();
                System.out.println(getThreadPoolStatus());
                System.out.println(getJedisPoolStatus());
                return 0;
            }
        return -1;
    }

    private long publishBytes(String channel, @NotNull Object objMessage) {
        if (!pool.isClosed())
            try (Jedis jedis = pool.getResource()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(objMessage);
                oos.flush();
                return jedis.publish(channel.getBytes(StandardCharsets.US_ASCII), bos.toByteArray());
            } catch (Exception exception) {
                exception.printStackTrace();
                System.out.println(getThreadPoolStatus());
                System.out.println(getJedisPoolStatus());
                return 0;
            }
        return -1;
    }

    public String getThreadPoolStatus() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) scheduler;
        return "Active: " + threadPoolExecutor.getActiveCount() + " Completed: " + threadPoolExecutor.getCompletedTaskCount() + " PoolSize: " + threadPoolExecutor.getPoolSize() + " QueueSize: " + threadPoolExecutor.getQueue().size() + " TaskCount: " + threadPoolExecutor.getTaskCount() + " MaxPoolSize: " + threadPoolExecutor.getMaximumPoolSize() + " CorePoolSize: " + threadPoolExecutor.getCorePoolSize() + " KeepAliveTime: " + threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);

    }
    public String getJedisPoolStatus(){
        return "Active: " + pool.getNumActive() + " Idle: " + pool.getNumIdle() + " MaxTotal: " + pool.getMaxTotal() + " MaxIdle: " + pool.getMaxIdle() + " MinIdle: " + pool.getMinIdle();
    }


}
