import ezredislib.channel.ChannelListener;
import ezredislib.channel.DefaultChannels;

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
