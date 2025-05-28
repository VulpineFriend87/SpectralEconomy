package dev.vulpine.spectralEconomy.util.logger;

import dev.vulpine.spectralEconomy.util.Colorize;
import org.bukkit.Bukkit;

public class Logger {

    private static Level level = Level.INFO;
    private static final String prefix = "&7[&3Spectral&bEconomy&7] &r";

    public static void initialize(Level loggingLevel) {

        level = loggingLevel;

    }

    private static String buildPathString(String... path) {

        StringBuilder pathString = new StringBuilder(prefix);

        for (String p : path) {

            pathString.append("&7[").append(p).append("&7] &r");

        }

        return pathString.toString();

    }

    public static void info(String message, String... path) {

        if (level != Level.INFO) return;

        String pathString = buildPathString(path);
        Bukkit.getConsoleSender().sendMessage(Colorize.color(pathString + "&7" + message));

    }

    public static void error(String message, String... path) {

        if (level != Level.ERROR && level != Level.WARN && level != Level.INFO) return;

        String pathString = buildPathString(path);
        Bukkit.getConsoleSender().sendMessage(Colorize.color(pathString + "&c" + message));

    }

    public static void warn(String message, String... path) {

        if (level != Level.WARN && level != Level.INFO) return;

        String pathString = buildPathString(path);
        Bukkit.getConsoleSender().sendMessage(Colorize.color(pathString + "&e" + message));

    }

    public static void system(String message, String... path) {

        String pathString = buildPathString(path);
        Bukkit.getConsoleSender().sendMessage(Colorize.color(pathString + "&f" + message));

    }

    public static String getPrefix() {
        return prefix;
    }

}
