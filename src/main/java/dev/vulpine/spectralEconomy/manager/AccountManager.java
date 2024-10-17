package dev.vulpine.spectralEconomy.manager;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AccountManager {

    private static SpectralEconomy plugin;

    private final List<Account> accounts;

    public AccountManager(SpectralEconomy plugin) {

        AccountManager.plugin = plugin;

        accounts = new ArrayList<>();

    }

    public void loadAccount(UUID owner, boolean createIfNotFound) {

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[!] Loading account for " + owner + ".");

        for (Account account : accounts) {
            if (account.getOwner().equals(owner)) {
                Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[+] Account for " + owner + " already loaded.");
                return;
            }
        }

        String query = "SELECT balance FROM accounts WHERE owner = '" + owner + "';";

        StorageManager.executeQuery(query).thenAccept(resultSet -> {
            try {

                if (resultSet != null && resultSet.next()) {

                    BigDecimal balance = resultSet.getBigDecimal("balance");

                    accounts.add(new Account(owner, balance));

                    Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §a[+] Loaded account for " + owner + ".");

                } else {

                    if (!createIfNotFound) {

                        return;

                    }

                    Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[!] Account for " + owner + " not found. Creating new account.");

                    createAccount(owner);
                    loadAccount(owner, false);

                }

            } catch (SQLException e) {

                Bukkit.getPlayer(owner).kickPlayer("§cError loading account. Please try again later.");

                e.printStackTrace();

            }
        });

    }

    public void unloadAccount(UUID owner, boolean kickPlayer) {

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[!] Unloading account for " + owner + ".");

        accounts.removeIf(account -> account.getOwner().equals(owner));

        if (kickPlayer && Bukkit.getPlayer(owner) != null) {

            Bukkit.getPlayer(owner).kickPlayer("§cYour account was unloaded.");

        }

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §a[+] Unloaded account for " + owner + ".");

    }

    public void createAccount(UUID owner) {

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[!] Creating account for " + owner + ".");

        String query = "INSERT INTO accounts (owner, balance) VALUES (?, ?);";

        CompletableFuture.runAsync(() -> {
            try (Connection connection = plugin.getStorageManager().getDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, owner.toString());
                statement.setBigDecimal(2, BigDecimal.ZERO);

                statement.executeUpdate();

                Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §a[+] Created account for " + owner + ".");

            } catch (SQLException e) {

                Bukkit.getPlayer(owner).kickPlayer("§cError creating account. Please try again later.");

                e.printStackTrace();

            }
        });

    }

    public void deleteAccount(UUID owner) {

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[!] Removing account for " + owner + ".");

        String query = "DELETE FROM accounts WHERE owner = ?;";

        CompletableFuture.runAsync(() -> {
            try (Connection connection = plugin.getStorageManager().getDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, owner.toString());

                statement.executeUpdate();

                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPlayer(owner).kickPlayer("§cYour account was removed."));

                Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §a[+] Removed account for " + owner + ".");

            } catch (SQLException e) {

                e.printStackTrace();

            }
        });


    }

    public static void updateAccount(Account account) {

        Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §7[!] Updating account for " + account.getOwner() + ".");

        String query = "UPDATE accounts SET balance = ? WHERE owner = ?;";

        CompletableFuture.runAsync(() -> {
            try (Connection connection = plugin.getStorageManager().getDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setBigDecimal(1, account.getBalance());
                statement.setString(2, account.getOwner().toString());

                statement.executeUpdate();

                Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §a[+] Updated account for " + account.getOwner() + ".");

            } catch (SQLException e) {

                Bukkit.getPlayer(account.getOwner()).kickPlayer("§cError updating account. Please try again later.");

                Bukkit.getConsoleSender().sendMessage("[SpectralEconomy] [StorageManager] §c[-] Error updating account for " + account.getOwner() + ".");

                e.printStackTrace();

            }
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