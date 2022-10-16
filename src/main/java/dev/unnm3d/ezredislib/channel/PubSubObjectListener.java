package dev.unnm3d.ezredislib.channel;

import redis.clients.jedis.BinaryJedisPubSub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class PubSubObjectListener extends BinaryJedisPubSub implements PubSubListener {


    private final ReadPacketFunction rpf;
    private final Class<?> filterClass;
    private final String channelName;

    public PubSubObjectListener(ReadPacketFunction rpf, String channelName) {
        this.rpf = rpf;
        this.filterClass = null;
        this.channelName = channelName;
    }

    public PubSubObjectListener(ReadPacketFunction rpf, Class<?> filterClass, String channelName) {
        this.rpf = rpf;
        this.filterClass = filterClass;
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }


    @Override
    public void onMessage(byte[] channel, byte[] message) {

        try {
            Object obj = deserialize(message);
            if (filterClass != null) {
                if (!filterClass.isInstance(obj)) return;
            }
            this.rpf.read(obj);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}

