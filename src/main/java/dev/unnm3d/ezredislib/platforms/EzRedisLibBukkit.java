package dev.unnm3d.ezredislib.platforms;

import dev.unnm3d.ezredislib.EzRedisMessenger;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class EzRedisLibBukkit extends JavaPlugin {

    private EzRedisMessenger redisMessenger;
    private static EzRedisLibBukkit instance;

    public EzRedisMessenger getRedisMessenger() {
        return redisMessenger;
    }

    @Override
    public void onEnable() {
        this.instance=this;
        this.getCommand("ezredislibreload").setExecutor((commandSender, command, s, strings) -> {
            if(reload()) {
                commandSender.sendMessage("§bEzRedisLib reloaded. Connection to redis established.");
            } else {
                commandSender.sendMessage("§cError reloading RedisLib check the console");
            }
            return false;
        });
        if(reload())getLogger().info("Connection established");

        //bStats
        new Metrics(this, 15499);

    }

    private boolean reload() {
        this.saveDefaultConfig();
        //remove old handler
        if(redisMessenger!=null)
            redisMessenger.destroy();
        try {
            String user= this.getConfig().getString("redis.user", "");
            String pass= this.getConfig().getString("redis.password", "");
            user= user.isBlank() ? null : user;
            pass= pass.isBlank() ? null : pass;
            redisMessenger = new EzRedisMessenger(getConfig().getString("redis.host", "127.0.0.1"), getConfig().getInt("redis.port", 6379), user, pass);
            return true;
        } catch (InstantiationException e) {
            getLogger().severe("Error while reloading plugin: cannot create Connection with Redis");
            return false;
        }
    }
    public static EzRedisLibBukkit getInstance(){
        return instance;
    }
}
