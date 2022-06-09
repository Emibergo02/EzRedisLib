import ezredislib.channel.ChannelListener;
import ezredislib.channel.DefaultChannels;

public class PacketListener implements ChannelListener<QualcosaPacket> {
    static int counter = 0;
    @Override
    public void read(QualcosaPacket message) {
        System.out.println("Packet timestamp: "+ message.getTimestamp()+" current:"+System.currentTimeMillis());
        counter++;
    }

    @Override
    public String getChannelName() {
        return DefaultChannels.CHANNEL_A.getName();
    }
}
