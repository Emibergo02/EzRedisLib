package dev.unnm3d.ezredislib.platforms;

import dev.unnm3d.ezredislib.EzRedisMessenger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EzRedisLibBungee extends Plugin {
    private static EzRedisMessenger redisMessenger;

    public static EzRedisMessenger getRedisMessenger() {
        return redisMessenger;
    }

    @Override
    public void onEnable() {
        // All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 15499; // <-- Replace with the id of your plugin!
        new Metrics(this, pluginId);
        try {
            if(reload())getLogger().info("Connection established");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getProxy().getPluginManager().registerCommand(this, new Command("ezredislibreload","ezredislib.reload") {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                try {
                    if(reload()) {
                        commandSender.sendMessage("§bEzRedisLib reloaded. Connection to redis established.");
                    } else {
                        commandSender.sendMessage("§cError reloading RedisLib check the console");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private boolean reload() throws IOException {
        makeConfig();
        redisMessenger.destroy();
        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            String user= configuration.getString("redis.user", "");
            String pass= configuration.getString("redis.password", "");
            user= user.isBlank() ? null : user;
            pass= pass.isBlank() ? null : pass;
            redisMessenger = new EzRedisMessenger(configuration.getString("redis.host", "127.0.0.1"), configuration.getInt("redis.port", 6379), user, pass);
            return true;
        } catch (InstantiationException e) {
            getLogger().severe("Error while reloading plugin: cannot create Connection with Redis");
            return false;
        }
    }
    public void makeConfig() throws IOException {
        // Create plugin config folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "config.yml");

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
            InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
            in.transferTo(outputStream); // Throws IOException
        }
    }

}