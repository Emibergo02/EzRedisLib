import ezredislib.channel.PubSubListener;
import ezredislib.channel.DefaultChannels;

public class MioListener extends PubSubListener<QualcosaPacket> {

    public MioListener() {
        super(DefaultChannels.CHANNEL_A.getName());
    }

    @Override
    public void read(QualcosaPacket message) {
        System.out.println("Packet timestamp: "+ message.getTimestamp()+" current:"+System.currentTimeMillis());
    }
}
