import messaging.ChannelListener;
import messaging.DefaultChannels;
import org.jetbrains.annotations.NotNull;

public class PacketListener implements ChannelListener<QualcosaPacket> {
    @Override
    public void read(QualcosaPacket message) {
        System.out.println("Packet timestamp: "+ message.getTimestamp());
    }

    @Override
    public String getChannelName() {
        return DefaultChannels.CHANNEL_A.getName();
    }
}
