package dev.vulpine.spectralEconomy.listener;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SpectralEconomy plugin;

    public PlayerListener(SpectralEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        plugin.getAccountManager().loadAccount(event.getPlayer().getUniqueId());

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        plugin.getAccountManager().unloadAccount(event.getPlayer().getUniqueId());

    }

}
