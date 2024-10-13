package dev.vulpine.spectralEconomy.command;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
import dev.vulpine.spectralEconomy.util.Colorize;
import dev.vulpine.spectralEconomy.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final SpectralEconomy plugin;

    public MainCommand(SpectralEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0 || !sender.hasPermission("spectraleconomy.admin")) {

            String[] ascii = {
                    "§3",
                    "§3    §7This server is running",
                    "§3",
                    "§3    §3Spectral§bEconomy",
                    "§3    §3Version §b" + plugin.getDescription().getVersion(),
                    "§3    §7By VulpineFriend87",
                    "§3"
            };

            for (String line : ascii) {

                sender.sendMessage(line);

            }

            if (sender.hasPermission("spectraleconomy.admin")) {

                sender.sendMessage(Colorize.color("§7    [!] Run /seco help for a list of commands.\n"));

            } else {

                sender.sendMessage(Colorize.color("§7    [!] You do not have permission to use any subcommand.\n"));

            }

            return true;

        } else if (sender.hasPermission("spectraleconomy.admin")) {

            if (args[0].equalsIgnoreCase("help")) {

                String[] help = {
                        "",
                        "§7[§3S§bE§7] §fCommands:",
                        "",
                        "§7- §b/seco help §7- §fShows this help message.",
                        "§7- §b/seco account §7- §fShows the account commands.",
                        "§7- §b/seco reload §7- §fReloads the configuration (for some changes to take effect you must restart the server).",
                        ""
                };

                for (String line : help) {

                    sender.sendMessage(line);

                }

            } else if (args[0].equalsIgnoreCase("reload")) {

                try {

                    long start = System.currentTimeMillis();
                    plugin.reloadConfig();
                    long end = System.currentTimeMillis();

                    sender.sendMessage(Colorize.color("§7[§3S§bE§7] §fConfiguration reloaded. (Took " + (end - start) + "ms)"));

                } catch (Exception e) {

                    sender.sendMessage(Colorize.color("§7[§3S§bE§7] §cError while reloading configuration. Please check the server logs."));

                }

            } else if (args[0].equalsIgnoreCase("account")) {

                if (args.length == 1) {

                    String[] help = {
                            "",
                            "§7[§3S§bE§7] §fAccount Commands:",
                            "",
                            "§7- §b/seco account <player> info §7- §fShows information about a player's account.",
                            "§7- §b/seco account <player> balance set <amount> §7- §fSets a player's account balance.",
                            "§7- §b/seco account <player> balance add <amount> §7- §fAdds an amount to a player's account balance.",
                            "§7- §b/seco account <player> balance remove <amount> §7- §fRemoves an amount from a player's account balance.",
                            "§7- §b/seco account <player> delete §7- §fDeletes a player's account.",
                            "§7- §b/seco account <player> unload §7- §fUnloads a player's account from memory (this will kick the player).",
                            ""
                    };

                    for (String line : help) {

                        sender.sendMessage(line);

                    }

                    return true;
                }

                if (args.length == 2) {

                    sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_arguments")));
                    return true;

                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUUID = target.getUniqueId();
                Account account = plugin.getAccountManager().getAccount(targetUUID);

                if (account == null) {

                    plugin.getAccountManager().loadAccount(targetUUID, false);

                    account = plugin.getAccountManager().getAccount(targetUUID);

                    if (account == null) {

                        sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.account_not_found")));
                        return true;

                    }

                }

                if (args[2].equalsIgnoreCase("info")) {

                    List<String> info = plugin.getConfig().getStringList("messages.account.info");
                    for (String line : info) {

                        String formattedMessage = line
                                .replace("%name%", target.getName())
                                .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                .replace("%balance%", Format.format(account.getBalance()));
                        sender.sendMessage(Colorize.color(formattedMessage));

                    }

                } else if (args[2].equalsIgnoreCase("delete")) {

                    plugin.getAccountManager().deleteAccount(targetUUID);
                    sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.delete")
                            .replace("%player%", target.getName())));

                } else if (args[2].equalsIgnoreCase("unload")) {

                    plugin.getAccountManager().unloadAccount(targetUUID, true);
                    sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.unload")
                            .replace("%player%", target.getName())));

                } else if (args[2].equalsIgnoreCase("balance")) {

                    if (args.length < 5) {

                        sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_arguments")));
                        return true;

                    }

                    try {

                        BigDecimal amount = new BigDecimal(args[4]);

                        if (args[3].equalsIgnoreCase("set")) {

                            account.setBalance(amount);
                            sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.balance.admin.set")
                                    .replace("%player%", target.getName())
                                    .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                    .replace("%amount%", args[4])));

                            if (target.isOnline()) {
                                target.getPlayer().sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.balance.target.set")
                                        .replace("%player%", target.getName())
                                        .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                        .replace("%amount%", args[4])));
                            }

                        } else if (args[3].equalsIgnoreCase("add")) {

                            account.addMoney(amount);
                            sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.balance.admin.add")
                                    .replace("%player%", target.getName())
                                    .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                    .replace("%amount%", args[4])));

                            if (target.isOnline()) {
                                target.getPlayer().sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.balance.target.add")
                                        .replace("%player%", target.getName())
                                        .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                        .replace("%amount%", args[4])));
                            }

                        } else if (args[3].equalsIgnoreCase("remove")) {

                            account.removeMoney(amount);
                            sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.balance.admin.remove")
                                    .replace("%player%", target.getName())
                                    .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                    .replace("%amount%", args[4])));

                            if (target.isOnline()) {
                                target.getPlayer().sendMessage(Colorize.color(plugin.getConfig().getString("messages.account.balance.target.remove")
                                        .replace("%player%", target.getName())
                                        .replace("%currency%", plugin.getConfig().getString("economy.currency"))
                                        .replace("%amount%", args[4])));
                            }

                        } else {

                            sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_arguments")));

                        }

                    } catch (NumberFormatException e) {

                        sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_amount")));

                    }

                } else {

                    sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_arguments")));

                }

                if (!target.isOnline()) {

                    account = plugin.getAccountManager().getAccount(targetUUID);

                    if (account != null) {

                        plugin.getAccountManager().unloadAccount(targetUUID, false);

                    }
                }

            } else {

                sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_arguments")));
            }

        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("spectraleconomy.admin")) {
            return completions;
        }

        if (args.length == 1) {

            completions.addAll(List.of("help", "reload", "account"));

        } else if (args.length == 2 && args[0].equalsIgnoreCase("account")) {

            String query = args[1].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(query))
                    .toList());

        } else if (args.length == 3 && args[0].equalsIgnoreCase("account")) {

            completions.addAll(List.of("info", "balance", "delete", "unload"));

        } else if (args.length == 4 && args[0].equalsIgnoreCase("account") && args[2].equalsIgnoreCase("balance")) {

            completions.addAll(List.of("set", "add", "remove"));

        }

        return completions;
    }


}
