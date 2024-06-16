package me.angellime.stafffaketime.Command;

import me.angellime.stafffaketime.StaffFakeTime;
import me.angellime.stafffaketime.Util.CheckTask;
import me.angellime.stafffaketime.Util.DataBaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CheckCommand implements CommandExecutor {

    private final StaffFakeTime plugin;
    private final DataBaseManager dataBaseManager;
    private boolean checkInProgress = false; // Flag to indicate if a check is in progress

    public CheckCommand(StaffFakeTime plugin, DataBaseManager dataBaseManager) {
        this.plugin = plugin;
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки");
            return true;
        }

        Player checker = (Player) sender;

        if (args.length < 2) {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            checker.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if(!checker.hasPermission("check.rvise")){
            sender.sendMessage("Нету прав");
            return true;
        }

        if(target.hasPermission("check.no-check")){
            sender.sendMessage(mossage(plugin.getConfig().getString("message.no-check")));
            return true;
        }

        String action = args[1].toLowerCase();
        if ("start".equals(action)) {
            if (checkInProgress) {
                checker.sendMessage(mossage(plugin.getConfig().getString("message.no-two")));
                return true;
            }
            startCheck(checker, target);
        } else if ("confirm".equals(action)) {
            confirmCheck(target);
        } else if ("ban".equals(action)) {
            if (args.length < 3) {
                return false;
            }
            banPlayer(checker, target, args[2]);
        } else {
            return false;
        }
        return true;
    }

    private void startCheck(Player checker, Player target) {
        UUID targetId = target.getUniqueId();

        plugin.getCheckingPlayers().put(targetId, checker.getUniqueId());
        checkInProgress = true;

        target.teleport(plugin.getCheckLocation());
        target.sendMessage(mossage("&7[&cCheck&7] c&lЭто проверка на читы, &eу Вас есть 7 минут, чтобы скинуть Ваш ID Анидеска &c&lAnyDesk &f(anydesk,com) &eи пройти проверку. В случае отказа/выхода/игнора - блокировка аккаунта"));
        target.sendTitle(ChatColor.RED + "Провірка", "Вы на проверке, смотрите в чат", 10, 70, 20);
        checker.sendMessage(mossage(plugin.getConfig().getString("message.proverka").replace("%player%", target.getName())));

        BossBar bossBar = Bukkit.createBossBar("Время до конца проверки", BarColor.RED, BarStyle.SOLID);
        bossBar.addPlayer(target);
        bossBar.addPlayer(checker);
        plugin.getBossBars().put(targetId, bossBar);

        dataBaseManager.addReason(checker.getName(), target.getName(), "Проверка", "Убидиться в использывании чита");

        new CheckTask(plugin, target, checker, plugin.getCheckDuration()).runTaskTimer(plugin, 0L, 20L);
    }

    private void confirmCheck(Player target) {
        UUID targetId = target.getUniqueId();
        if (plugin.getCheckingPlayers().remove(targetId) != null) {
            removeBossBar(targetId);
            target.teleport(target.getWorld().getSpawnLocation());
            target.sendMessage(mossage(plugin.getConfig().getString("message.succeful")));
            checkInProgress = false;
        }
    }

    private void banPlayer(Player checker, Player target, String reason) {
        UUID targetId = target.getUniqueId();
        if (plugin.getCheckingPlayers().remove(targetId) != null) {
            plugin.getBossBars().remove(targetId).removeAll();
            String duration = plugin.getConfig().getString("ban-durations." + reason, plugin.getConfig().getString("default-ban-duration", "10d"));
            String command = String.format("banip %s %s %s", target.getName(), duration, reason);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            checkInProgress = false;
        }
    }

    private void removeBossBar(UUID targetId) {
        BossBar bossBar = plugin.getBossBars().remove(targetId);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public String mossage(String string){

        return ChatColor.translateAlternateColorCodes('&', string);

    }
}
