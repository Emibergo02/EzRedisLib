package dev.unnm3d.ezredislib.channel;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class PubSubListener<T> extends JedisPubSub {

    private final Gson gson = new Gson();
    private final String channelName;
    private final Type packetType;

    public PubSubListener(String channelName) {
        this.channelName=channelName;
        this.packetType=((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    }

    @Override
    public void onMessage(String channel, String message) {
        this.read(gson.fromJson(message, this.packetType));
    }

    abstract public void read(T message);

    public Type getPacketType(){
        return packetType;
    }

    public @NotNull String getChannelName(){
        return channelName;
    }
}
