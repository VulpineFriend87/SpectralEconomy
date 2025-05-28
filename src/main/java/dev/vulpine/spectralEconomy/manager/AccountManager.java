package dev.vulpine.spectralEconomy.manager;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
import dev.vulpine.spectralEconomy.util.logger.Logger;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountManager {

    private static SpectralEconomy plugin;

    private final List<Account> accounts;

    public AccountManager(SpectralEconomy plugin) {

        AccountManager.plugin = plugin;

        accounts = new ArrayList<>();

    }

    public void loadAccount(UUID owner, boolean createIfNotFound) {
        Logger.info("&7[!] Loading account for " + owner.toString() + ".", "AccountManager");

        for (Account account : accounts) {

            if (account.getOwner().equals(owner)) {

                Logger.info("&7[+] Account for " + owner + " already loaded.", "AccountManager");
                return;

            }
        }

        String query = "SELECT balance FROM accounts WHERE owner = '" + owner.toString() + "';";
        plugin.getStorageManager().executeQuery(query).thenAccept(resultSet -> {

            try {

                if (resultSet != null && resultSet.next()) {

                    BigDecimal balance = resultSet.getBigDecimal("balance");

                    accounts.add(new Account(owner, balance));

                    Logger.info("&a[+] Loaded account for " + owner.toString() + ".", "AccountManager");

                } else {

                    if (resultSet == null) {

                        Logger.info("&4[-] Result set is null for account: " + owner.toString() + ".", "AccountManager");

                    }

                    if (!createIfNotFound) return;
                    Logger.info("&7[!] Account for " + owner.toString() + " not found. Creating new account.", "AccountManager");
                    Bukkit.getScheduler().runTask(plugin, () -> createAccount(owner, true));

                }

            } catch (SQLException e) {

                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cError loading account. Please try again later."));
                e.printStackTrace();

            } finally {

                try {

                    plugin.getStorageManager().closeResources(resultSet, resultSet.getStatement(), resultSet.getStatement().getConnection());

                } catch (SQLException e) {

                    Logger.error("Error while closing resources for " + owner + ": " + e.getMessage(), "ProfileManager");

                }

            }
        });
    }

    public void unloadAccount(UUID owner, boolean kickPlayer) {
        Logger.info("&7[!] Unloading account for " + owner.toString() + ".", "AccountManager");
        accounts.removeIf(account -> account.getOwner().equals(owner));

        if (kickPlayer) {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cYour account was unloaded."));
        }

        Logger.info("&a[+] Unloaded account for " + owner.toString() + ".", "AccountManager");
    }

    public void createAccount(UUID owner, boolean load) {
        Logger.info("&7[!] Creating account for " + owner.toString() + ".", "AccountManager");
        String query = "INSERT INTO accounts (owner, balance) VALUES ('" + owner.toString() + "', " + plugin.getConfig().getDouble("economy.starting_balance") + ");";

        plugin.getStorageManager().executeUpdate(query).thenRun(() -> {
            Logger.info("&a[+] Created account for " + owner.toString() + ".", "AccountManager");
            if (load) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getAccountManager().loadAccount(owner, false), 20);
            }
        }).exceptionally(e -> {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cError creating account. Please try again later."));
            e.printStackTrace();
            return null;
        });
    }

    public void deleteAccount(UUID owner) {
        Logger.info("&7[!] Removing account for " + owner.toString() + ".", "AccountManager");
        String query = "DELETE FROM accounts WHERE owner = '" + owner.toString() + "';";

        plugin.getStorageManager().executeUpdate(query).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cYour account was removed."));
            Logger.info("&a[+] Removed account for " + owner.toString() + ".", "AccountManager");
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public static void updateAccount(Account account) {

        Logger.info("&7[!] Updating account for " + account.getOwner().toString() + ".", "AccountManager");
        String query = "UPDATE accounts SET balance = " + account.getBalance() + " WHERE owner = '" + account.getOwner().toString() + "';";

        plugin.getStorageManager().executeUpdate(query).thenRun(() -> {
            Logger.info("&a[+] Updated account for " + account.getOwner().toString() + ".", "AccountManager");
        }).exceptionally(e -> {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(account.getOwner()).kickPlayer("§cError updating account. Please try again later."));
            Logger.info("&c[-] Error updating account for " + account.getOwner().toString() + ".", "AccountManager");
            e.printStackTrace();
            return null;
        });

    }

    public Account getAccount(UUID owner) {
        for (Account account : accounts) {
            if (account.getOwner().equals(owner)) {
                return account;
            }
        }
        return null;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}