package cp.corona.database;

import cp.corona.crownpunishments.CrownPunishments;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class SoftBanDatabaseManager {
    private final CrownPunishments plugin;
    private final String dbURL;

    public SoftBanDatabaseManager(CrownPunishments plugin) {
        this.plugin = plugin;
        String dbType = plugin.getConfigManager().getDatabaseType();
        String dbName = plugin.getConfigManager().getDatabaseName();
        String dbAddress = plugin.getConfigManager().getDatabaseAddress();
        String dbPort = plugin.getConfigManager().getDatabasePort();
        String dbUsername = plugin.getConfigManager().getDatabaseUsername();
        String dbPassword = plugin.getConfigManager().getDatabasePassword();

        if ("mysql".equalsIgnoreCase(dbType)) {
            this.dbURL = String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false", dbAddress, dbPort, dbName);
        } else {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();
            File dbFile = new File(dataFolder, dbName + ".db");
            this.dbURL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        }
        initializeDatabase();
        startExpiryCheckTask();
    }

    private Connection getConnection() throws SQLException {
        String dbType = plugin.getConfigManager().getDatabaseType();
        if ("mysql".equalsIgnoreCase(dbType)) {
            return DriverManager.getConnection(dbURL,
                    plugin.getConfigManager().getDatabaseUsername(),
                    plugin.getConfigManager().getDatabasePassword());
        } else {
            return DriverManager.getConnection(dbURL);
        }
    }

    private void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS softbans (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "endTime BIGINT NOT NULL," +
                    "reason TEXT)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize database!", e);
        }
    }

    private void startExpiryCheckTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                removeExpiredSoftBans();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error checking for expired soft bans.", e);
            }
        }, 0L, 20L * 60 * 5);
    }

    private void removeExpiredSoftBans() throws SQLException {
        long currentTime = System.currentTimeMillis();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM softbans WHERE endTime <= ? AND endTime != ?")) {
            ps.setLong(1, currentTime);
            ps.setLong(2, Long.MAX_VALUE);
            ps.executeUpdate();
        }
    }

    public void softBanPlayer(UUID uuid, long endTime, String reason) {
        long currentEndTime = getSoftBanEndTime(uuid);
        long finalEndTime = endTime;
        String logMessage;

        // Nueva lógica para permanente
        if (endTime == Long.MAX_VALUE) {
            finalEndTime = Long.MAX_VALUE;
            logMessage = currentEndTime > System.currentTimeMillis() ?
                    "Updating to PERMANENT softban" :
                    "Adding new PERMANENT softban";
        } else if (currentEndTime > System.currentTimeMillis() && currentEndTime != Long.MAX_VALUE) {
            finalEndTime = currentEndTime + (endTime - System.currentTimeMillis());
            logMessage = "Extending existing softban";
        } else {
            logMessage = "Adding new softban";
        }

        plugin.getLogger().log(Level.INFO,
                logMessage + " - UUID: " + uuid +
                        " | EndTime: " + new Date(finalEndTime) +
                        " | Reason: " + reason);

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT OR REPLACE INTO softbans (uuid, endTime, reason) VALUES (?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, finalEndTime);
            ps.setString(3, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Database operation failed!", e);
        }
    }

    public void unSoftBanPlayer(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM softbans WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not un-soft ban player!", e);
        }
    }

    public boolean isSoftBanned(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT endTime FROM softbans WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long endTime = rs.getLong("endTime");
                return endTime == Long.MAX_VALUE || endTime > System.currentTimeMillis();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Database error!", e);
        }
        return false;
    }

    public String getSoftBanReason(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT reason FROM softbans WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("reason");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Database error!", e);
        }
        return null;
    }

    public long getSoftBanEndTime(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT endTime FROM softbans WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("endTime");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Database error!", e);
        }
        return 0;
    }
}