package me.angellime.stafffaketime;

import litebans.api.Entry;
import litebans.api.Events;
import me.angellime.stafffaketime.Command.CheckCommand;
import me.angellime.stafffaketime.Command.CheckTabCompleter;
import me.angellime.stafffaketime.Listenr.PlayerListener;
import me.angellime.stafffaketime.Util.DataBaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.bukkit.boss.BossBar;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class StaffFakeTime extends JavaPlugin {

    private Connection connection;
    private DataBaseManager dataBaseManager;
    private Plugin plugin;

    private FileConfiguration pluginConfig;
    private HashMap<UUID, UUID> checkingPlayers = new HashMap<>();
    private HashMap<UUID, BossBar> bossBars = new HashMap<>();
    private Location checkLocation;
    private long checkDuration;
    private List<String> banReasons;

    @Override
    public void onEnable() {
        plugin = this;

        // Сохранение конфигурационного файла, если он не существует
        saveDefaultConfig();

        // Загрузка конфигурации
        pluginConfig = getConfig();
        loadConfiguration();

        dataBaseManager = new DataBaseManager();

        // Регистрация команд и событий
        CommandExecutor checkCommandExecutor = new CheckCommand(this, dataBaseManager);
        getCommand("check").setExecutor(checkCommandExecutor);
        getCommand("check").setTabCompleter(new CheckTabCompleter(banReasons));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Инициализация менеджера базы данных

        registerLiteBansEvents();
    }

    @Override
    public void onDisable() {
        // Закрытие соединения с базой данных при выключении плагина
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().severe("Ошибка при закрытии соединения с базой данных: " + e.getMessage());
            }
        }
    }

    private void loadConfiguration() {
        // Проверка и загрузка конфигурации
        if (pluginConfig.contains("check-location.world") && pluginConfig.contains("check-location.x") &&
                pluginConfig.contains("check-location.y") && pluginConfig.contains("check-location.z")) {
            checkLocation = new Location(
                    Bukkit.getWorld(pluginConfig.getString("check-location.world")),
                    pluginConfig.getDouble("check-location.x"),
                    pluginConfig.getDouble("check-location.y"),
                    pluginConfig.getDouble("check-location.z")
            );
        } else {
            getLogger().severe("Конфигурация 'check-location' не найдена или некорректна!");
        }

        checkDuration = pluginConfig.getLong("check-duration", 300L);
        banReasons = pluginConfig.getStringList("ban-reasons");
    }

    private void registerLiteBansEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        String executorName = entry.getExecutorName();
                        String uuidString = entry.getUuid();
                        UUID uuid = UUID.fromString(uuidString);
                        Player player1 = Bukkit.getPlayer(executorName);
                        OfflinePlayer player2 = Bukkit.getOfflinePlayer(uuid);
                        String type = entry.getType();
                        String reason = entry.getReason();

                        if (executorName == null || uuidString == null || type == null || reason == null) {
                            getLogger().severe("Запись содержит null значения. Executor: " + executorName + ", UUID: " + uuidString + ", Type: " + type + ", Reason: " + reason);
                            return;
                        }

                        if (player2 == null || !player2.hasPlayedBefore()) {
                            getLogger().warning("Player2 is null или никогда не играл. UUID: " + uuidString);
                            return;
                        }

                        if (player1 == null) {
                            dataBaseManager.addAntiChet(executorName, player2.getName(), type, reason);
                        } else {
                            dataBaseManager.addReason(player1.getName(), player2.getName(), type, reason);
                        }

                    } catch (IllegalArgumentException e) {
                        getLogger().severe("Неверный UUID строка: " + entry.getUuid());
                    } catch (Exception e) {
                        getLogger().severe("Ошибка при обработке события: " + e.getMessage());
                    }
                });
            }
        });
    }

    public FileConfiguration getPluginConfig() {
        return pluginConfig;
    }

    public HashMap<UUID, UUID> getCheckingPlayers() {
        return checkingPlayers;
    }

    public HashMap<UUID, BossBar> getBossBars() {
        return bossBars;
    }

    public Location getCheckLocation() {
        return checkLocation;
    }

    public long getCheckDuration() {
        return checkDuration;
    }
}
