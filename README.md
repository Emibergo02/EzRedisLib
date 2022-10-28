# EzRedisLib
Simple Redis pubsub library for Proxy and Spigot servers

## Spigot Installation
Copy the same JAR into the plugins directory of your Proxy and Spigot installations.
Bungeecord and spigot generates a config.yml with the redis connection options
if you don't use any user or password keep the parameters empty.
It is possible that the plugin gives a DNS error on startup, 
90% of the times your redis server unreachable. Please check with redis-cli if it is reachable

## Lib Installation on your software
Just read the maven/gradle sections

## Use case
ChannelObjectListener is suggested for more performance since it relies on java Object serialization
You don't need to specify the class in registerChannelObjectListener(channel,lambda) since deserialization
do it by itself
There might be some issues with sending and receiving two different versions of a packet object since the
deserialization will use a different class than serialization (this might happen when updating distributed systems)

ChannelListener (JSON) is less efficient but allows to debug packet contents more easily (it will be transmitted as a JSON string)
You need to specify the class used to deserialize incoming packets

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
  <version>3.3-SNAPSHOT</version>
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

Add the dependency and replace `3.3-SNAPSHOT` with the latest release version:
```gradle
dependencies {
        implementation 'com.github.Emibergo02:EzRedisLib:3.3-SNAPSHOT'
}
```

## Main usage

```java
        //MAIN ACCESS POINTS
        int timeout = 0;//Connection timeout (0 for no timeout)
        int database = 0;//Redis resource database (default is 0)
        RedisMessagingHandler redisMessagingHandler = new RedisMessagingHandler("localhost", 6379, "user", "password",0,0);
        //Initializes redis connection pool
        
        //Register listener on channel (there is an example below)
        redisMessagingHandler.registerChannelObjectListener(....
        
        
        //sends packet        
        redisMessagingHandler.sendPacket("channel1", new PingPacket("client1", "client2"));
        
        //You can use Jedis normally with
        redisMessagingHandler.getJedis().get("key");
        
        //Or with a CompletableFuture blocking request with timeout (true if you want Exceptions to be printed)
        redisMessagingHandler.jedisResource(jedis -> jedis.smembers("kalyachat_playerlist") ,1000,true);
        //The same but more compact and simplified
        redisMessagingHandler.jedisResource(jedis -> jedis.smembers("kalyachat_playerlist"));
        
        //Or with a non-blocking CompletableFuture!
        redisMessagingHandler.jedisResourceFuture(jedis -> jedis.smembers("kalyachat_playerlist"),1000);
        redisMessagingHandler.jedisResourceFuture(jedis -> jedis.smembers("kalyachat_playerlist")).thenApply(stringSet-> 
            System.out.println("Is empty?: "+stringSet.isEmpty()));
            return stringSet;
            );
        
```

## Example of a packet

```java
package examples;

import ezredislib.packet.MessagingPacket;
/**
 * MessagingPacket is for JSON serialization
 * Serializable is for Java Object serialization
 */
public class QualcosaPacket implements MessagingPacket, Serializable {

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

## Example of registering an Object channel

```java
       ezRedisMessenger.registerChannelObjectListener(Channel.CHAT.getChannelName(), (packet) -> {

            ChatPacket chatPacket = (ChatPacket) packet;

            if (chatPacket.isPrivate()) {
                if(!KalyaChat.getInstance().getRedisDataManager().isIgnoring(chatPacket.getReceiverName(),chatPacket.getSenderName()))//Check ignoring
                    KalyaChat.getInstance().getChatListener().onPrivateChat(chatPacket.getSenderName(), chatPacket.getReceiverName(), chatPacket.getMessage());
            } else {
                KalyaChat.getInstance().getChatListener().onPublicChat(chatPacket.getMessage());
            }

        }, ChatPacket.class);
```

## Support
Feel free to contact me for every problem here in the issues or on discord https://discord.gg/vfWt3pPw
