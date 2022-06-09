package examples;

import ezredislib.RedisMessagingHandler;
import ezredislib.channel.DefaultChannels;
import ezredislib.channel.PubSubListener;
import ezredislib.packet.PingPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InstantiationException, InterruptedException {


        //MAIN ACCESS POINTS
        //RedisMessagingHandler redisMessagingHandler = new RedisMessagingHandler("localhost", 6379, null, null);
        //Initialize redis connection pool
        //redisMessagingHandler.registerChannelListener(new MioListener());
        //register listener
        //redisMessagingHandler.sendPacketAsync(channel,packet);
        //send packet

        RedisMessagingHandler handler = new RedisMessagingHandler("localhost", 6379, null, null);
        handler.registerChannelListener(new MioListener());
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            QualcosaPacket packet = new QualcosaPacket("client1", "client2");
            handler.sendPacketAsync(DefaultChannels.CHANNEL_A.getName(), packet);
        }
        , 0, 100, TimeUnit.MILLISECONDS);

        //Compact anonymous version
        RedisMessagingHandler handler2 = new RedisMessagingHandler("localhost", 6379, null, null);
        handler2.registerChannelListener(new PubSubListener<PingPacket>("channel1") {

            @Override
            public void read(PingPacket message) {
                System.out.println("Packet timestamp: "+ message.getTimestamp()+" current:"+System.currentTimeMillis());
            }
        });
        handler2.sendPacket("channel1", new PingPacket("client1", "client2"));


    }
}
