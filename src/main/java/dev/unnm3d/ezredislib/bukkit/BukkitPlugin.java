package dev.unnm3d.ezredislib.bukkit;

import dev.unnm3d.ezredislib.RedisMessagingHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BukkitPlugin extends JavaPlugin {

    private static RedisMessagingHandler redisMessagingHandler;

    public static RedisMessagingHandler getRedisLib() {
        return redisMessagingHandler;
    }

    @Override
    public void onEnable() {
        this.getCommand("ezredislibreload").setExecutor((commandSender, command, s, strings) -> {
            if(reload()) {
                commandSender.sendMessage("Reloaded");
            } else {
                commandSender.sendMessage("Error reloading check the console");
            }
            return false;
        });
        reload();

    }

    private boolean reload() {
        this.saveDefaultConfig();
        try {
            redisMessagingHandler = new RedisMessagingHandler(getConfig().getString("redis.host", "localhost"), getConfig().getInt("redis.port", 6379), getConfig().getString("redis.user", ""), getConfig().getString("redis.password", ""));
            return true;
        } catch (InstantiationException e) {
            getLogger().severe("Error while reloading plugin: cannot create Connection with Redis");
            this.getPluginLoader().disablePlugin(this);
            return false;
        }
    }
}
