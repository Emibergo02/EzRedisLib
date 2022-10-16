package dev.unnm3d.ezredislib;

import dev.unnm3d.ezredislib.channel.PubSubObjectListener;
import dev.unnm3d.ezredislib.packet.MessagingPacket;
import dev.unnm3d.ezredislib.packet.PingPacket;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EzRedisMessengerTest {
    private static EzRedisMessenger ezRedisMessenger;


    @org.junit.jupiter.api.Test
    void connectionCheck() {
        try {
            ezRedisMessenger=new EzRedisMessenger("redis-15907.c55.eu-central-1-1.ec2.cloud.redislabs.com",15907,"default","cuz1K3qyH4ybQCtgDTb027eEV6xYBmUH",10000,0,"biagio");

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    @org.junit.jupiter.api.Test
    void registerChannelListener() {
        try {
            ezRedisMessenger=new EzRedisMessenger("redis-15907.c55.eu-central-1-1.ec2.cloud.redislabs.com",15907,"default","cuz1K3qyH4ybQCtgDTb027eEV6xYBmUH",10000,0,"biagio");

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        CompletableFuture<String> completableFuture=new CompletableFuture<>();
        ezRedisMessenger.registerChannelListener("test", (o) -> {
            CiaoNotFinal pingPacket=(CiaoNotFinal) o;
            assertEquals("a", pingPacket.name);
            completableFuture.complete(pingPacket.address);
        }, CiaoNotFinal.class);


        HashMap<Integer,HashMap<String,String>> map=new HashMap<>();
        map.put(1,new HashMap<>(Map.of("a","b")));

        CiaoNotFinal c=new CiaoNotFinal("a","b", map);
        CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS).execute(()->ezRedisMessenger.sendPacket("test",c));

        assertEquals("b", completableFuture.completeOnTimeout("c", 5000, TimeUnit.MILLISECONDS).join());

    }

    @org.junit.jupiter.api.Test
    void registerChannelObjectListener() throws ExecutionException, InterruptedException {
        try {
            ezRedisMessenger=new EzRedisMessenger("redis-15907.c55.eu-central-1-1.ec2.cloud.redislabs.com",15907,"default","cuz1K3qyH4ybQCtgDTb027eEV6xYBmUH",10000,0,"biagio");

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        CompletableFuture<String> completableFuture=new CompletableFuture<>();
        PubSubObjectListener pubSubObjectListener=ezRedisMessenger.registerChannelObjectListener("test", (o) -> {
            Ciao ciao=(Ciao) o;
            assertEquals("a", ciao.name);
            assertEquals("b", ciao.antonio.get(1).get("a"));
            completableFuture.complete(ciao.address);
        });

        HashMap<Integer,HashMap<String,String>> map=new HashMap<>();
        map.put(1,new HashMap<>(Map.of("a","b")));

        Ciao c=new Ciao("a","b", map);
        CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS).execute(()->ezRedisMessenger.sendObjectPacket("test",c));


        assertEquals("b", completableFuture.completeOnTimeout(null, 2000, TimeUnit.MILLISECONDS).get());

        assertTrue(ezRedisMessenger.isChannelRegistered("test"));
        ezRedisMessenger.unregisterFromChannel("test");
        assertFalse(ezRedisMessenger.isChannelRegistered("test"));

        Thread.sleep(1000);

        assertFalse(pubSubObjectListener.isSubscribed());



        ezRedisMessenger.registerChannelObjectListener("test", (o) -> {
            Ciao ciao=(Ciao) o;
            assertEquals("a", ciao.name);
            completableFuture.complete(ciao.address);
        });
        ezRedisMessenger.registerChannelObjectListener("test", (o) -> {
            Ciao ciao=(Ciao) o;
            assertEquals("a", ciao.name);
            completableFuture.complete(ciao.address);
        });
        ezRedisMessenger.registerChannelObjectListener("test", (o) -> {
            Ciao ciao=(Ciao) o;
            assertEquals("a", ciao.name);
            completableFuture.complete(ciao.address);
        });
        assertEquals(3,ezRedisMessenger.getChannelListeners("test").size());
    }

    @org.junit.jupiter.api.Test
    void destroy() {
        ezRedisMessenger.destroy();
    }


    public record Ciao (String name, String address, HashMap<Integer,HashMap<String,String>> antonio) implements Serializable, MessagingPacket {}
    public static class CiaoNotFinal implements Serializable, MessagingPacket {
        public String name;
        public String address;
        public HashMap<Integer, HashMap<String, String>> antonio;

        CiaoNotFinal(String name, String address, HashMap<Integer,HashMap<String,String>> antonio) {
            this.name = name;
            this.address = address;
            this.antonio = antonio;
        }

    }



}