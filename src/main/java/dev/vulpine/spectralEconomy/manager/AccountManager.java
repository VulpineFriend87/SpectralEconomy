package dev.vulpine.spectralEconomy.manager;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
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
        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[!] Loading account for " + owner.toString() + ".");

        for (Account account : accounts) {

            if (account.getOwner().equals(owner)) {

                Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[+] Account for " + owner + " already loaded.");
                return;

            }
        }

        String query = "SELECT balance FROM accounts WHERE owner = '" + owner.toString() + "';";
        StorageManager.executeQuery(query).thenAccept(resultSet -> {

            try {

                if (resultSet != null && resultSet.next()) {

                    BigDecimal balance = resultSet.getBigDecimal("balance");

                    accounts.add(new Account(owner, balance));

                    Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §a[+] Loaded account for " + owner.toString() + ".");

                } else {

                    if (resultSet == null) {

                        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §4[-] Result set is null for account: " + owner.toString() + ".");

                    }

                    if (!createIfNotFound) return;
                    Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[!] Account for " + owner.toString() + " not found. Creating new account.");
                    Bukkit.getScheduler().runTask(plugin, () -> createAccount(owner, true));

                }

            } catch (SQLException e) {

                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cError loading account. Please try again later."));
                e.printStackTrace();

            }
        });
    }

    public void unloadAccount(UUID owner, boolean kickPlayer) {
        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[!] Unloading account for " + owner.toString() + ".");
        accounts.removeIf(account -> account.getOwner().equals(owner));

        if (kickPlayer) {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cYour account was unloaded."));
        }

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §a[+] Unloaded account for " + owner.toString() + ".");
    }

    public void createAccount(UUID owner, boolean load) {
        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[!] Creating account for " + owner.toString() + ".");
        String query = "INSERT INTO accounts (owner, balance) VALUES ('" + owner.toString() + "', 0);";

        StorageManager.executeUpdate(query).thenRun(() -> {
            Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §a[+] Created account for " + owner.toString() + ".");
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
        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[!] Removing account for " + owner.toString() + ".");
        String query = "DELETE FROM accounts WHERE owner.toString() = '" + owner.toString() + "';";

        StorageManager.executeUpdate(query).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cYour account was removed."));
            Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §a[+] Removed account for " + owner.toString() + ".");
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public static void updateAccount(Account account) {

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §7[!] Updating account for " + account.getOwner().toString() + ".");
        String query = "UPDATE accounts SET balance = " + account.getBalance() + " WHERE owner = '" + account.getOwner().toString() + "';";

        StorageManager.executeUpdate(query).thenRun(() -> {
            Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §a[+] Updated account for " + account.getOwner().toString() + ".");
        }).exceptionally(e -> {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(account.getOwner()).kickPlayer("§cError updating account. Please try again later."));
            Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [AccountManager] §c[-] Error updating account for " + account.getOwner().toString() + ".");
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