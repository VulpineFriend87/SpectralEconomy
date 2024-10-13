package dev.vulpine.spectralEconomy;

import dev.vulpine.spectralEconomy.command.BalanceCommand;
import dev.vulpine.spectralEconomy.command.MainCommand;
import dev.vulpine.spectralEconomy.command.PayCommand;
import dev.vulpine.spectralEconomy.listener.PlayerListener;
import dev.vulpine.spectralEconomy.manager.AccountManager;
import dev.vulpine.spectralEconomy.manager.PlaceholderManager;
import dev.vulpine.spectralEconomy.manager.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpectralEconomy extends JavaPlugin {

    AccountManager accountManager;
    StorageManager storageManager;

    @Override
    public void onEnable() {

        String[] ascii = {
                "§3",
                "§3     _____§b _____  ",
                "§3    |   __§b|   __|   §3Spectral§bEconomy",
                "§3    |__   §b|   __|   §3Version §b" + this.getDescription().getVersion(),
                "§3    |_____§b|_____|   §7By VulpineFriend87",
                "§3                    ",
                "§3"
        };

        for (String line : ascii) {

            Bukkit.getConsoleSender().sendMessage(line);

        }

        Bukkit.getConsoleSender().sendMessage("§7    [!] Starting Plugin...");

        Bukkit.getConsoleSender().sendMessage("§7    [!] Registering Commands...");

        getCommand("spectraleconomy").setExecutor(new MainCommand(this));
        getCommand("spectraleconomy").setTabCompleter(new MainCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("pay").setTabCompleter(new PayCommand(this));

        Bukkit.getConsoleSender().sendMessage("§7    [!] Registering Listeners...");

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        Bukkit.getConsoleSender().sendMessage("§7    [!] Registering Placeholders...");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {

            Bukkit.getConsoleSender().sendMessage("§7    [!] PlaceholderAPI found! Hooking...");

            new PlaceholderManager(this).register();

        }

        Bukkit.getConsoleSender().sendMessage("§7    [!] Initializing Configuration...");

        saveDefaultConfig();

        Bukkit.getConsoleSender().sendMessage("§7    [!] Initializing AccountManager...");

        accountManager = new AccountManager(this);

        Bukkit.getConsoleSender().sendMessage("§7    [!] Initializing StorageManager...");

        storageManager = new StorageManager(this);

        Bukkit.getConsoleSender().sendMessage("§a    [!] Plugin Started!");

    }

    @Override
    public void onDisable() {

        storageManager.close();

    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

}
