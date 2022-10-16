package dev.unnm3d.ezredislib.channel;

public interface PubSubListener {

    String getChannelName();

    @FunctionalInterface
    interface ReadPacketFunction {
        void read(Object message);
    }
}
