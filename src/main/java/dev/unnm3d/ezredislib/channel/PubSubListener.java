package dev.unnm3d.ezredislib.channel;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class PubSubListener extends JedisPubSub {

    private final Gson gson = new Gson();
    private final Class<?> filterClass;
    private final ReadPacketFunction rpf;
    private final String channelName;


    public PubSubListener(String channelName,ReadPacketFunction rpf) {
        this.channelName = channelName;
        this.rpf=rpf;
        this.filterClass = null;
    }
    public PubSubListener(String channelName,ReadPacketFunction rpf,  Class<?> filterClass) {
        this.channelName = channelName;
        this.rpf=rpf;
        this.filterClass = filterClass;
    }

    @Override
    public void onMessage(String channel, String message) {
        Object o=gson.fromJson(message, this.filterClass);
        if(filterClass!=null){
            if(!filterClass.isInstance(o))return;
        }
        rpf.read(o);
    }


    public  Class<?> getPacketType(){
        return filterClass;
    }

    public @NotNull String getChannelName(){
        return channelName;
    }

    @FunctionalInterface
    public interface ReadPacketFunction {
        void read(Object message);
    }
}
