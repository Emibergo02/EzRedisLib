package examples;

import ezredislib.channel.DefaultChannels;
import ezredislib.channel.PubSubListener;

public class MioListener extends PubSubListener<QualcosaPacket> {

    public MioListener() {
        super(DefaultChannels.CHANNEL_A.getName());
    }

    @Override
    public void read(QualcosaPacket message) {
        System.out.println("Packet timestamp: "+ message.getTimestamp()+" current:"+System.currentTimeMillis());
    }
}
