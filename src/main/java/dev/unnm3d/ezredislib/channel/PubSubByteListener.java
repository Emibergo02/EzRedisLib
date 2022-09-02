package dev.unnm3d.ezredislib.channel;

import dev.unnm3d.ezredislib.RedisUtils;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.BinaryJedisPubSub;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public abstract class PubSubByteListener<T> extends BinaryJedisPubSub {


    private final byte[] channelName;

    public PubSubByteListener(String channelName) {
        this.channelName=channelName.getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {

        try {
            this.read((T)RedisUtils.deserialize(message));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    abstract public void read(T message);


    public @NotNull String getChannelName(){
        return new String(channelName);
    }
    public @NotNull byte[] getChannelNameRaw(){
        return channelName;
    }
}
