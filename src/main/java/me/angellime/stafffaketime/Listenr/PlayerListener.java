package me.angellime.stafffaketime.Listenr;

import me.angellime.stafffaketime.Command.CheckCommand;
import me.angellime.stafffaketime.StaffFakeTime;
import me.angellime.stafffaketime.Util.CheckTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.UUID;

public class PlayerListener implements Listener {

    private StaffFakeTime plugin;

    public PlayerListener(StaffFakeTime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Player target = event.getPlayer();
        if (plugin.getCheckingPlayers().containsKey(playerId)) {
            UUID checkerId = plugin.getCheckingPlayers().get(playerId);
            Player checker = Bukkit.getPlayer(checkerId);
            if (checker != null) {
                event.setCancelled(true);

                checker.sendMessage(mossage(plugin.getConfig().getString("message.checker_message")
                        .replace("%player%", event.getPlayer().getName())
                        .replace("%message%", event.getMessage())));
                target.sendMessage(mossage(plugin.getConfig().getString("message.target_message")
                        .replace("%message%", event.getMessage())));
            }

            if(target != null ){
                event.setCancelled(true);

                target.sendMessage(mossage(plugin.getConfig().getString("message.checker_message")
                        .replace("%player%", event.getPlayer().getName())
                        .replace("%message%", event.getMessage())));

                checker.sendMessage(mossage(plugin.getConfig().getString("message.target_message")
                        .replace("%message%", event.getMessage())));

            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerId)) {
            CheckTask task = plugin.getCheckTasks().get(playerId);
            if (task != null && !task.isAlreadyBanned()) {
                Player checker = Bukkit.getPlayer(plugin.getCheckingPlayers().get(playerId));
                if (checker != null) {
                    String quitMessage = plugin.getConfig().getString("message.leave");
                    if (quitMessage != null) {
                        quitMessage = quitMessage.replace("%player%", event.getPlayer().getName());
                    } else {
                        quitMessage = "Игрок " + event.getPlayer().getName() + " вышел во время проверки.";
                    }
                    checker.sendMessage(mossage(quitMessage));
                    Bukkit.dispatchCommand(checker, "banip " + event.getPlayer().getName() + " 30d 2.4 (Лив) -s");
                    task.setAlreadyBanned(true);
                }
                CheckCommand.checkInProgress = false;

                plugin.getCheckingPlayers().remove(playerId);
                plugin.getBossBars().remove(playerId).removeAll();
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerUUID)) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
                player.sendMessage(mossage(plugin.getConfig().getString("message.no-command")));
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerUUID)) {
            event.setCancelled(true);
            player.sendMessage(mossage(plugin.getConfig().getString("message.no-command")));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerUUID)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerUUID)) {
            event.setCancelled(true);
        }
    }


    public String mossage(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
