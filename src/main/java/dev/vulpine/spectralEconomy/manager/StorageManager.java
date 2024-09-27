package dev.vulpine.spectralEconomy.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.vulpine.spectralEconomy.SpectralEconomy;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StorageManager {

    private static HikariDataSource dataSource;

    public StorageManager(SpectralEconomy plugin) {
        FileConfiguration config = plugin.getConfig();
        String storageMethod = config.getString("storage.method");

        if ("mysql".equalsIgnoreCase(storageMethod)) {
            setupMySQL(config);
        } else if ("sqlite".equalsIgnoreCase(storageMethod)) {
            setupSQLite(plugin);
        }

        createAccountsTable();
    }

    private void setupMySQL(FileConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();
        String databaseName = config.getString("storage.mysql.database");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("storage.mysql.host") + ":" +
                config.getString("storage.mysql.port") + "/" + databaseName);
        hikariConfig.setUsername(config.getString("storage.mysql.user"));
        hikariConfig.setPassword(config.getString("storage.mysql.password"));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        dataSource = new HikariDataSource(hikariConfig);
    }

    private void setupSQLite(SpectralEconomy plugin) {
        HikariConfig hikariConfig = new HikariConfig();
        File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String dbFile = dataFolder + "/database.db";
        File databaseFile = new File(dbFile);

        try {

            if (!databaseFile.exists()) {
                databaseFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile);
        dataSource = new HikariDataSource(hikariConfig);
    }


    private void createAccountsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "owner TEXT NOT NULL," +
                "balance REAL DEFAULT 0" +
                ");";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeUpdate(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String sql) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

}