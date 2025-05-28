package dev.vulpine.spectralEconomy.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.vulpine.spectralEconomy.SpectralEconomy;
import dev.vulpine.spectralEconomy.instance.StorageMethod;
import dev.vulpine.spectralEconomy.util.logger.Logger;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class StorageManager {

    private final SpectralEconomy plugin;

    private static HikariDataSource dataSource;

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public StorageManager(SpectralEconomy plugin, StorageMethod method, String host, String port, String database, String username, String password) {
        this.plugin = plugin;

        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        if (method == null) {
            method = StorageMethod.H2;
        }

        setup(method);

        createAccountsTable();
    }

    public void setup(StorageMethod method) {

        HikariConfig config = new HikariConfig();

        if (method == StorageMethod.H2) {

            String databasePath = plugin.getDataFolder().getAbsolutePath();
            config.setJdbcUrl("jdbc:h2:file:" + databasePath + "/database;MODE=MYSQL;AUTO_RECONNECT=TRUE");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(60000);
            config.setLeakDetectionThreshold(3000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);

        } else if (method == StorageMethod.MYSQL) {

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setLeakDetectionThreshold(3000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);

        }

        config.setAutoCommit(true);
        config.setValidationTimeout(3000);
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);

    }

    private void createAccountsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "owner VARCHAR(255) NOT NULL," +
                "balance DOUBLE DEFAULT 0" +
                ");";

        executeUpdate(createTableSQL);
    }

    public CompletableFuture<ResultSet> executeQuery(String query, Object... params) {

        return CompletableFuture.supplyAsync(() -> {

            Connection connection;
            PreparedStatement statement;
            ResultSet rs;

            try {

                connection = dataSource.getConnection();
                statement = connection.prepareStatement(query);

                for (int i = 0; i < params.length; i++) {

                    statement.setObject(i + 1, params[i]);

                }

                rs = statement.executeQuery();

                return rs;

            } catch (SQLException e) {

                throw new RuntimeException("Error while executing query: ", e);

            }

        });

    }

    public CompletableFuture<Integer> executeUpdate(String query, Object... params) {

        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < params.length; i++) {

                    statement.setObject(i + 1, params[i]);

                }

                return statement.executeUpdate();

            } catch (SQLException e) {

                throw new RuntimeException("Error while executing query: ", e);

            }

        });

    }

    public void closeResources(AutoCloseable... resources) {

        for (AutoCloseable resource : resources) {

            if (resource != null) {

                try {

                    resource.close();

                } catch (Exception e) {

                    Logger.error("Error while closing resource: " + resource, "StorageManager");

                }

            }
        }

    }

    public void close() {

        if (dataSource != null) {

            dataSource.close();

        }

    }
}