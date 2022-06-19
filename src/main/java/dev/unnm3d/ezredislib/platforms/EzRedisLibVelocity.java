package dev.unnm3d.ezredislib.platforms;


import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;

import javax.inject.Inject;
import java.util.logging.Logger;

@Plugin(id = "ezredislib", name = "EzRedisLib", version = "1.1-SNAPSHOT",
        url = "https://github.com/Emibergo02/EzRedisLib", description = "Velocity implementation", authors = {"unnm3d"})
public class EzRedisLibVelocity {

    private final Metrics.Factory metricsFactory;

    @Inject
    public EzRedisLibVelocity(ProxyServer server, Logger logger, Metrics.Factory metricsFactory) {
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // All you have to do is adding the following two lines in your onProxyInitialization method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 15499; // <-- Replace with the id of your plugin!
        metricsFactory.make(this, pluginId);

    }
}