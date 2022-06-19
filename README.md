# EzRedisLib
Simple Redis pubsub library for Proxy and Spigot servers

## Installation
Copy the same JAR into the plugins directory of your Proxy and Spigot installations.
Bungeecord and spigot generates a config.yml with the redis connection options
if you don't use any user or password keep the parameters empty.
It is possible that the plugin gives a DNS error on startup, 
90% of the times your redis server unreachable. Please check with redis-cli if it is reachable

## Commands
`/ezredislibreload`
  - Permission: `ezredislib.reload` 
  - Description: Reloads the redis pool


### Maven
Add this repository to your `pom.xml`:
```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>  
```

Add the dependency and replace `<version>...</version>` with the latest release version:
```xml
<dependency>
  <groupId>com.github.Emibergo02</groupId>
  <artifactId>EzRedisLib</artifactId>
  <version>1.1-SNAPSHOT</version>
</dependency>
```

### Gradle
Add it in your root `build.gradle` at the end of repositories:
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Add the dependency and replace `1.1-SNAPSHOT` with the latest release version:
```gradle
dependencies {
        implementation 'com.github.Emibergo02:EzRedisLib:1.1-SNAPSHOT'
}
```

## Main usage

```java
        //MAIN ACCESS POINTS
        //RedisMessagingHandler redisMessagingHandler = new RedisMessagingHandler("localhost", 6379, null, null);
        //Initializes redis connection pool
        //redisMessagingHandler.registerChannelListener(new MioListener());
        //registers listener
        //redisMessagingHandler.sendPacketAsync(channel,packet);
        //sends packet

        RedisMessagingHandler handler = new RedisMessagingHandler("localhost", 6379, null, null);
        handler.registerChannelListener(new MioListener());
        //This only delays packet sending
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            QualcosaPacket packet = new PingPacket("client1", "client2");
            handler.sendPacketAsync(DefaultChannels.CHANNEL_A.getName(), packet);
        }
        , 0, 100, TimeUnit.MILLISECONDS);

        //Compact version
        RedisMessagingHandler handler2 = new RedisMessagingHandler("localhost", 6379, null, null);
        handler2.registerChannelListener(new PubSubListener<PingPacket>("channel1") {

            @Override
            public void read(PingPacket message) {
                System.out.println("Packet timestamp: "+ message.getTimestamp()+" current:"+System.currentTimeMillis());
            }
        });
        handler2.sendPacket("channel1", new PingPacket("client1", "client2"));
        
        //You can use Jedis normally with
        handler2.getJedis().get("key");
```

## Example of a packet

```java
package examples;

import ezredislib.packet.MessagingPacket;

public class QualcosaPacket implements MessagingPacket {

        private final long timestamp;
        private final String from;
        private final String to;

        public QualcosaPacket(String from,String to) {
            this.from= from;
            this.to= to;
            this.timestamp = System.currentTimeMillis();
        }


        public String getTarget() {
            return this.to;
        }

        public String getSender() {
            return this.from;
        }

        public long getTimestamp() {
            return timestamp;
        }
}
```

## Example of a listener

```java
package examples;

import ezredislib.channel.PubSubListener;

public class ExampleListener extends PubSubListener<QualcosaPacket> {

    public ExampleListener() {
        //     channel1 is the name of the channel
        super("channel1");
    }

    @Override
    public void read(QualcosaPacket message) {
        System.out.println("Packet timestamp: "+ message.getTimestamp()+" current:"+System.currentTimeMillis());
    }
}
```

## Support
Feel free to contact me for every problem here in the issues or on discord https://discord.gg/vfWt3pPw
