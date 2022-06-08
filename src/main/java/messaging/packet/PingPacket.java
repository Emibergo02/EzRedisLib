package messaging.packet;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class PingPacket implements MessagingPacket,TargetedPacket {

    private final long timestamp;
    private final String from;
    private final String to;

    public PingPacket(String from,String to) {
        this.from= from;
        this.to= to;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public @Nullable String getTarget() {
        return this.to;
    }

    @Override
    public String getSender() {
        return this.from;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
