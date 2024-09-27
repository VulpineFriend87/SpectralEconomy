package dev.vulpine.spectralEconomy.command;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
import dev.vulpine.spectralEconomy.util.Colorize;
import dev.vulpine.spectralEconomy.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final SpectralEconomy plugin;

    public PayCommand(SpectralEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player payer)) {
            sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.only_players")));
            return true;
        }

        if (args.length != 2) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_arguments")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.player_not_found")));
            return true;
        }

        if (payer.equals(target)) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.pay_self")));
            return true;
        }

        Account payerAccount = plugin.getAccountManager().getAccount(payer.getUniqueId());
        Account targetAccount = plugin.getAccountManager().getAccount(target.getUniqueId());

        if (payerAccount == null) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.account_not_found")));
            return true;
        }

        if (targetAccount == null) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.account_not_found")));
            return true;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
        } catch (NumberFormatException e) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_amount")));
            return true;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.invalid_amount")));
            return true;
        }

        BigDecimal minAmount = new BigDecimal("0.01");
        if (amount.compareTo(minAmount) < 0) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.minimum_amount")));
            return true;
        }

        if (payerAccount.getBalance().compareTo(amount) < 0) {
            payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.errors.insufficient_funds")));
            return true;
        }

        payerAccount.removeMoney(amount);
        targetAccount.addMoney(amount);

        String currency = plugin.getConfig().getString("economy.currency");

        payer.sendMessage(Colorize.color(plugin.getConfig().getString("messages.pay.success")
                .replace("%amount%", Format.format(amount))
                .replace("%currency%", currency)
                .replace("%player%", target.getName())));

        target.sendMessage(Colorize.color(plugin.getConfig().getString("messages.pay.received")
                .replace("%amount%", Format.format(amount))
                .replace("%currency%", currency)
                .replace("%player%", payer.getName())));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialPlayerName = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialPlayerName))
                    .toList());
        }

        return completions;
    }
}
