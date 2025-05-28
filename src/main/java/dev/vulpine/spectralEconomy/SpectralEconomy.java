package dev.vulpine.spectralEconomy;

import dev.vulpine.spectralEconomy.command.BalanceCommand;
import dev.vulpine.spectralEconomy.command.MainCommand;
import dev.vulpine.spectralEconomy.command.PayCommand;
import dev.vulpine.spectralEconomy.instance.StorageMethod;
import dev.vulpine.spectralEconomy.listener.PlayerListener;
import dev.vulpine.spectralEconomy.manager.AccountManager;
import dev.vulpine.spectralEconomy.manager.PlaceholderManager;
import dev.vulpine.spectralEconomy.manager.StorageManager;
import dev.vulpine.spectralEconomy.util.logger.Level;
import dev.vulpine.spectralEconomy.util.logger.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpectralEconomy extends JavaPlugin {

    AccountManager accountManager;
    StorageManager storageManager;

    @Override
    public void onEnable() {

        Logger.initialize(Level.INFO);

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

            Logger.system(line);

        }

        Logger.system("§7    [!] Starting Plugin...");

        Logger.system("§7    [!] Registering Commands...");

        getCommand("spectraleconomy").setExecutor(new MainCommand(this));
        getCommand("spectraleconomy").setTabCompleter(new MainCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("pay").setTabCompleter(new PayCommand(this));

        Logger.system("§7    [!] Registering Listeners...");

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        Logger.system("§7    [!] Registering Placeholders...");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {

            Logger.system("§7    [!] PlaceholderAPI found! Hooking...");

            new PlaceholderManager(this).register();

        }

        Logger.system("§7    [!] Initializing Configuration...");

        saveDefaultConfig();

        Logger.system("§7    [!] Initializing AccountManager...");

        accountManager = new AccountManager(this);

        Logger.system("§7    [!] Initializing StorageManager...");

        storageManager = new StorageManager(this,
                StorageMethod.fromString(getConfig().getString("storage.method", "H2")),
                getConfig().getString("storage.mysql.host", "localhost"),
                getConfig().getString("storage.mysql.port", "3306"),
                getConfig().getString("storage.mysql.database", "vcrates"),
                getConfig().getString("storage.mysql.username", "vcrates"),
                getConfig().getString("storage.mysql.password", ""));

        Logger.system("§a    [!] Plugin Started!");

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
