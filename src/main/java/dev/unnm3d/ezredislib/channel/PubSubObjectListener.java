package dev.unnm3d.ezredislib.channel;

import redis.clients.jedis.BinaryJedisPubSub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;

public class PubSubObjectListener extends BinaryJedisPubSub {


    private final ReadPacketFunction rpf;
    private final Type filterClass;

    public PubSubObjectListener(ReadPacketFunction rpf) {
        this.rpf=rpf;
        this.filterClass = Object.class;
    }
    public PubSubObjectListener(ReadPacketFunction rpf, Type filterClass) {
        this.rpf=rpf;
        this.filterClass = filterClass;
    }


    @Override
    public void onMessage(byte[] channel, byte[] message) {

        try {
            Object obj=deserialize(message);
            if(obj.getClass().equals(filterClass))
                this.rpf.read(obj);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    @FunctionalInterface
    public interface ReadPacketFunction {
        void read(Object message);
    }
}
