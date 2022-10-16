package dev.unnm3d.ezredislib.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;

public class PingPacket<T, K> implements MessagingPacket, Serializable {

    private final long timestamp;
    private final String from;
    private final String to;
    private final HashMap<T, K> antonio;

    public PingPacket() {
        this.from = "a";
        this.to = "b";
        this.timestamp = System.currentTimeMillis();
        antonio = new HashMap<>();
    }

    public PingPacket(String from, String to) {
        this.from = from;
        this.to = to;
        this.timestamp = System.currentTimeMillis();
        antonio = new HashMap<>();
    }

    public void setRoba(T a, K b) {
        antonio.put(a, b);
    }


    public @Nullable String getTarget() {
        return this.to;
    }

    public @Nullable HashMap<T, K> getMapo() {
        return antonio;
    }

    public @NotNull String getSender() {
        return this.from;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
