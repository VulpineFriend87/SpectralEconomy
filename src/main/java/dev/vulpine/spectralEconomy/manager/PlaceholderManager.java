package dev.vulpine.spectralEconomy.manager;

import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.Account;
import dev.vulpine.spectralEconomy.util.Format;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class PlaceholderManager extends PlaceholderExpansion {

    private final SpectralEconomy plugin;

    public PlaceholderManager(SpectralEconomy plugin) {

        this.plugin = plugin;

    }

    @Override
    public @NotNull String getIdentifier() {

        return "seco";

    }

    @Override
    public @NotNull String getAuthor() {

        return plugin.getDescription().getAuthors().toString();

    }

    @Override
    public @NotNull String getVersion() {

        return plugin.getDescription().getVersion();

    }

    @Override
    public boolean persist() {

        return true;

    }

    @Override
    public boolean canRegister() {

        return true;

    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {

        if (player == null) {

            return "";

        }

        Account account = plugin.getAccountManager().getAccount(player.getUniqueId());

        if (account == null) {

            return "No account";

        }

        BigDecimal balance = account.getBalance();

        switch (identifier) {
            case "balance":

                return String.valueOf(balance);

            case "balance_formatted":

                return plugin.getConfig().getString("placeholder_format")
                        .replace("%balance%", String.valueOf(balance))
                        .replace("%currency%", plugin.getConfig().getString("economy.currency"));

            default:

                return null;

        }

    }

}
