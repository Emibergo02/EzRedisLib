package dev.unnm3d.ezredislib.channel;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;


public class PubSubJsonListener extends JedisPubSub implements PubSubListener {

    private final Gson gson = new Gson();
    private final Class<?> filterClass;
    private final ReadPacketFunction rpf;
    private final String channelName;


    public PubSubJsonListener(String channelName, ReadPacketFunction rpf, Class<?> filterClass) {
        this.channelName = channelName;
        this.rpf = rpf;
        this.filterClass = filterClass;
    }

    @Override
    public void onMessage(String channel, String message) {
        rpf.read(gson.fromJson(message, this.filterClass));
    }


    public Class<?> getPacketType() {
        return filterClass;
    }

    public @NotNull String getChannelName() {
        return channelName;
    }

}
