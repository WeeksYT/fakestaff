package me.angellime.stafffaketime.Listenr;

import me.angellime.stafffaketime.StaffFakeTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private StaffFakeTime plugin;

    public PlayerListener(StaffFakeTime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerId)) {
            UUID checkerId = plugin.getCheckingPlayers().get(playerId);
            Player checker = Bukkit.getPlayer(checkerId);
            if (checker != null) {
                event.setCancelled(true);
                checker.sendMessage(ChatColor.LIGHT_PURPLE + "[Check] " + event.getPlayer().getName() + ": " + event.getMessage());
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[Check] Вы: " + event.getMessage());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerId)) {
            Player checker = Bukkit.getPlayer(plugin.getCheckingPlayers().get(playerId));
            if (checker != null) {
                String quitMessage = plugin.getConfig().getString("message.leave");
                if (quitMessage != null) {
                    quitMessage = quitMessage.replace("%player%", event.getPlayer().getName());
                } else {
                    quitMessage = "Игрок " + event.getPlayer().getName() + " вышел во время проверки.";
                }
                checker.sendMessage(mossage(quitMessage));
            }
            plugin.getCheckingPlayers().remove(playerId);
            plugin.getBossBars().remove(playerId).removeAll();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (plugin.getCheckingPlayers().containsKey(playerUUID)) {
            // Проверяем, что игрок действительно передвигается
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



    public String mossage(String string){

        return ChatColor.translateAlternateColorCodes('&', string);

    }
}
