package dev.vulpine.spectralEconomy.command;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
import dev.vulpine.spectralEconomy.util.Colorize;
import dev.vulpine.spectralEconomy.util.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final SpectralEconomy plugin;

    public BalanceCommand(SpectralEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        Account account = plugin.getAccountManager().getAccount(((Player) sender).getUniqueId());

        if (account != null) {

            sender.sendMessage(Colorize.color(plugin.getConfig().getString("messages.balance")
                    .replace("%balance%", Format.format(account.getBalance()))
                    .replace("%currency%", plugin.getConfig().getString("economy.currency"))
            ));

        }

        return true;

    }

}
