package dev.unnm3d.ezredislib.bukkit;

import dev.unnm3d.ezredislib.EzRedisMessenger;
import org.bukkit.plugin.java.JavaPlugin;

public class EzRedisLibPlugin extends JavaPlugin {

    private static EzRedisMessenger redisMessagingHandler;

    public static EzRedisMessenger getRedisMessenger() {
        return redisMessagingHandler;
    }

    @Override
    public void onEnable() {
        this.getCommand("ezredislibreload").setExecutor((commandSender, command, s, strings) -> {
            if(reload()) {
                commandSender.sendMessage("§bEzRedisLib reloaded. Connection to redis established.");
            } else {
                commandSender.sendMessage("§cError reloading RedisLib check the console");
            }
            return false;
        });
        if(reload())getLogger().info("Connection established");

    }

    private boolean reload() {
        this.saveDefaultConfig();
        try {
            String user= this.getConfig().getString("redis.user", "");
            String pass= this.getConfig().getString("redis.password", "");
            user= user.isBlank() ? null : user;
            pass= pass.isBlank() ? null : pass;
            redisMessagingHandler = new EzRedisMessenger(getConfig().getString("redis.host", "127.0.0.1"), getConfig().getInt("redis.port", 6379), user, pass);
            return true;
        } catch (InstantiationException e) {
            getLogger().severe("Error while reloading plugin: cannot create Connection with Redis");
            return false;
        }
    }
}
