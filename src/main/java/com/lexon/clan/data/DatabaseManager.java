package com.lexon.clan.data;

import com.lexon.clan.LexonClan;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final LexonClan plugin;
    private final String type;
    private HikariDataSource dataSource;

    
    public DatabaseManager(LexonClan plugin, String type) {
        this.plugin = plugin;
        this.type = type.toLowerCase();

        if (this.type.equals("mysql")) {
            initializeMySQL();
        }
    }

    
    private void initializeMySQL() {
        try {
            ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("database.mysql");
            if (mysqlConfig == null) {
                plugin.getLogger().severe("MySQL yapılandırması bulunamadı!");
                return;
            }

            String host = mysqlConfig.getString("host", "localhost");
            int port = mysqlConfig.getInt("port", 3306);
            String database = mysqlConfig.getString("database", "lexonclan");
            String username = mysqlConfig.getString("username", "root");
            String password = mysqlConfig.getString("password", "");

            ConfigurationSection poolConfig = mysqlConfig.getConfigurationSection("pool");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            if (poolConfig != null) {
                config.setMaximumPoolSize(poolConfig.getInt("maximum-pool-size", 10));
                config.setMinimumIdle(poolConfig.getInt("minimum-idle", 5));
                config.setConnectionTimeout(poolConfig.getLong("connection-timeout", 30000));
                config.setIdleTimeout(poolConfig.getLong("idle-timeout", 600000));
                config.setMaxLifetime(poolConfig.getLong("max-lifetime", 1800000));
            } else {
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(5);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
            }

            config.setPoolName("LexonClan-Pool");

            dataSource = new HikariDataSource(config);
            createTables();

            plugin.getLogger().info("✓ MySQL bağlantısı başarılı!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL bağlantısı kurulamadı!", e);
        }
    }

    
    private void createTables() {
        String clansTable = """
                CREATE TABLE IF NOT EXISTS lexonclan_clans (
                    id VARCHAR(8) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    tag VARCHAR(4) NOT NULL,
                    leader_uuid VARCHAR(36) NOT NULL,
                    premium BOOLEAN DEFAULT FALSE,
                    is_private BOOLEAN DEFAULT TRUE,
                    created_at BIGINT NOT NULL,
                    member_limit INT DEFAULT 10,
                    storage LONGTEXT,
                    UNIQUE KEY idx_name (name),
                    UNIQUE KEY idx_tag (tag)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        String membersTable = """
                CREATE TABLE IF NOT EXISTS lexonclan_members (
                    uuid VARCHAR(36) NOT NULL,
                    clan_id VARCHAR(8) NOT NULL,
                    role_id VARCHAR(32) DEFAULT 'member',
                    joined_at BIGINT NOT NULL,
                    last_online BIGINT NOT NULL,
                    PRIMARY KEY (uuid),
                    FOREIGN KEY (clan_id) REFERENCES lexonclan_clans(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        String rolesTable = """
                CREATE TABLE IF NOT EXISTS lexonclan_roles (
                    id VARCHAR(32) NOT NULL,
                    clan_id VARCHAR(8) NOT NULL,
                    display_name VARCHAR(64) NOT NULL,
                    priority INT DEFAULT 0,
                    permissions TEXT,
                    PRIMARY KEY (id, clan_id),
                    FOREIGN KEY (clan_id) REFERENCES lexonclan_clans(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        String invitesTable = """
                CREATE TABLE IF NOT EXISTS lexonclan_invites (
                    uuid VARCHAR(36) NOT NULL,
                    clan_id VARCHAR(8) NOT NULL,
                    expire_time BIGINT NOT NULL,
                    PRIMARY KEY (uuid, clan_id),
                    FOREIGN KEY (clan_id) REFERENCES lexonclan_clans(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        try (Connection conn = getConnection()) {
            conn.prepareStatement(clansTable).executeUpdate();
            conn.prepareStatement(membersTable).executeUpdate();
            conn.prepareStatement(rolesTable).executeUpdate();
            conn.prepareStatement(invitesTable).executeUpdate();
            plugin.getLogger().info("✓ Veritabanı tabloları oluşturuldu/kontrol edildi");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Tablolar oluşturulurken hata!", e);
        }
    }

    
    public Connection getConnection() {
        if (dataSource == null) {
            return null;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Veritabanı bağlantısı alınamadı!", e);
            return null;
        }
    }

    
    public boolean isMySQL() {
        return type.equals("mysql") && dataSource != null;
    }

    
    public String getType() {
        return type;
    }

    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("✓ MySQL bağlantısı kapatıldı");
        }
    }
}
