package me.angellime.stafffaketime;

import litebans.api.Entry;
import litebans.api.Events;
import me.angellime.stafffaketime.Util.DataBaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class StaffFakeTime extends JavaPlugin {

    private Connection connection;
    private DataBaseManager dataBaseManager;
    Plugin plugin;


    @Override
    public void onEnable() {

        plugin = this;

        dataBaseManager = new DataBaseManager();
        registerEvents();


    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().severe("Ошибка при закрытии соединения с базой данных: " + e.getMessage());
            }
        }
    }

    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        Player player1 = Bukkit.getPlayer(entry.getExecutorName());
                        UUID uuid = UUID.fromString(entry.getUuid());
                        OfflinePlayer player2 = Bukkit.getOfflinePlayer(uuid);
                        String sender = entry.getExecutorName();

                        String type = entry.getType();
                        String reason = entry.getReason();

                        dataBaseManager.addAntiChet(sender, player2.getName(), type, reason);

                        if (player2 == null || !player2.hasPlayedBefore()) return;
                        if (player1 == null) return;

                        dataBaseManager.addReason(player1.getName(), player2.getName(), type, reason);
                    } catch (Exception e) {
                        getLogger().severe("Ошибка при обработке события: " + e.getMessage());
                    }
                });
            }
        });
    }
}
