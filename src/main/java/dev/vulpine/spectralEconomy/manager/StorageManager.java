package dev.vulpine.spectralEconomy.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.vulpine.spectralEconomy.SpectralEconomy;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StorageManager {

    private static HikariDataSource dataSource;
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(5);
    private final boolean isMySQL;

    public StorageManager(SpectralEconomy plugin) {
        FileConfiguration config = plugin.getConfig();
        String storageMethod = config.getString("storage.method");

        if ("mysql".equalsIgnoreCase(storageMethod)) {
            setupMySQL(config);
            isMySQL = true;
        } else {
            setupH2(plugin);
            isMySQL = false;
        }

        createAccountsTable();
    }

    private void setupMySQL(FileConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();
        String databaseName = config.getString("storage.mysql.database");
        String jdbcUrl = "jdbc:mysql://" + config.getString("storage.mysql.host") + ":" +
                config.getString("storage.mysql.port") + "/" + databaseName + "?useSSL=false";

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getString("storage.mysql.user"));
        hikariConfig.setPassword(config.getString("storage.mysql.password"));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(30000);

        dataSource = new HikariDataSource(hikariConfig);

        databaseExecutor.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("Successfully connected to MySQL.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error connecting to MySQL: " + e.getMessage());
            }
        });
    }

    private void setupH2(SpectralEconomy plugin) {
        HikariConfig hikariConfig = new HikariConfig();
        File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String dbFilePath = new File(dataFolder, "database").getAbsolutePath();
        String jdbcUrl = "jdbc:h2:file:" + dbFilePath + ";AUTO_SERVER=TRUE;TRACE_LEVEL_FILE=0";

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setDriverClassName("org.h2.Driver");
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(30000);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.addDataSourceProperty("queryTimeout", "30");

        dataSource = new HikariDataSource(hikariConfig);
    }

    private void createAccountsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "owner VARCHAR(255) NOT NULL," +
                "balance DOUBLE DEFAULT 0" +
                ");";

        executeUpdate(createTableSQL);
    }

    public static CompletableFuture<Void> executeUpdate(String sql) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error executing update: " + e.getMessage());
            }
        }, databaseExecutor).orTimeout(10, TimeUnit.SECONDS);
    }

    public static CompletableFuture<ResultSet> executeQuery(String sql) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                return statement.executeQuery(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }, databaseExecutor).orTimeout(10, TimeUnit.SECONDS);
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
        databaseExecutor.shutdown();
        try {
            if (!databaseExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                databaseExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            databaseExecutor.shutdownNow();
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
