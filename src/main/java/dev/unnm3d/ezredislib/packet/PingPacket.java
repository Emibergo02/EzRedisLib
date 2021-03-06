package dev.unnm3d.ezredislib.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public @NotNull String getSender() {
        return this.from;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
