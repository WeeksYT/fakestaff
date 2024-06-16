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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CheckCommand implements CommandExecutor {

    private final StaffFakeTime plugin;
    private final DataBaseManager dataBaseManager;
    public static boolean checkInProgress = false;// Flag to indicate if a check is in progress

    public CheckCommand(StaffFakeTime plugin, DataBaseManager dataBaseManager) {
        this.plugin = plugin;
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (sender instanceof Player) {
            Player checker = (Player) sender;
            if (!checker.hasPermission("check.rvise")) {
                sender.sendMessage("Нету прав");
                return true;
            }

            if (target.hasPermission("check.no-check")) {
                sender.sendMessage(mossage(plugin.getConfig().getString("message.no-check")));
                return true;
            }
        }

        String action = args[1].toLowerCase();
        if ("start".equals(action)) {
            if (checkInProgress) {
                sender.sendMessage(mossage(plugin.getConfig().getString("message.no-two")));
                return true;
            }
            if (sender instanceof Player) {
                Player checker = (Player) sender;
                startCheck(checker, target);
            } else {
                sender.sendMessage("Only players can start a check.");
            }
        } else if ("confirm".equals(action)) {
            confirmCheck(target);
        } else if ("ban".equals(action)) {
            if (args.length < 3) {
                return false;
            }
            if (sender instanceof Player) {
                Player checker = (Player) sender;
                banPlayer(checker, target, args);
            } else {
                sender.sendMessage("Only players can ban a player.");
            }
        } else if ("addtime".equals(action)) {
            if (args.length < 3) {
                return false;
            }
            if (sender instanceof Player) {
                Player checker = (Player) sender;
                addTime(checker, target, args[2]);
            } else {
                sender.sendMessage("Only players can add time.");
            }
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
        target.sendMessage(mossage("&7[&cCheck&7] &c&lЭто проверка на читы, &eу Вас есть 7 минут, чтобы скинуть Ваш ID Анидеска &c&lAnyDesk &f(anydesk,com) &eи пройти проверку. В случае отказа/выхода/игнора - блокировка аккаунта"));
        target.sendTitle(ChatColor.RED + "Проверка", "Вы на проверке, смотрите в чат", 10, 70, 20);
        checker.sendMessage(mossage(plugin.getConfig().getString("message.proverka").replace("%player%", target.getName())));

        BossBar bossBar = Bukkit.createBossBar("Время до конца проверки", BarColor.RED, BarStyle.SOLID);
        bossBar.addPlayer(target);
        bossBar.addPlayer(checker);
        plugin.getBossBars().put(targetId, bossBar);

        dataBaseManager.addReason(checker.getName(), target.getName(), "Проверка", "Убедиться в использовании чита");

        CheckTask task = new CheckTask(plugin, target, checker, plugin.getCheckDuration());
        plugin.getCheckTasks().put(targetId, task); // Store the task for future reference
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void confirmCheck(Player target) {
        UUID targetId = target.getUniqueId();
        if (plugin.getCheckingPlayers().remove(targetId) != null) {
            CheckTask task = plugin.getCheckTasks().get(targetId);
            if (task != null && !task.isAlreadyBanned()) {
                task.setAlreadyBanned(true);
            }
            removeBossBar(targetId);
            target.teleport(target.getWorld().getSpawnLocation());
            target.sendMessage(mossage(plugin.getConfig().getString("message.succeful")));
            checkInProgress = false;
        }
    }

    private void banPlayer(Player checker, Player target, String[] args) {
        UUID targetId = target.getUniqueId();
        if (plugin.getCheckingPlayers().remove(targetId) != null) {
            plugin.getBossBars().remove(targetId).removeAll();
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            String duration = plugin.getConfig().getString("ban-durations." + reason, plugin.getConfig().getString("default-ban-duration", "10d"));
            String command = String.format("banip %s %s %s", target.getName(), duration, reason);
            Bukkit.dispatchCommand(checker, command); // Execute the ban command from the console
            CheckTask task = plugin.getCheckTasks().get(targetId);
            if (task != null && !task.isAlreadyBanned()) {
                task.setAlreadyBanned(true);
            }
            checkInProgress = false;
        }
    }

    private void addTime(Player checker, Player target, String timeString) {
        long additionalTime = parseTime(timeString);
        if (additionalTime <= 0) {
            checker.sendMessage(ChatColor.RED + "Некорректное время.");
            return;
        }

        UUID targetId = target.getUniqueId();
        CheckTask task = plugin.getCheckTasks().get(targetId);
        if (task != null) {
            long newTime = task.addTime(additionalTime);
            checker.sendMessage(ChatColor.GREEN + "Время проверки увеличено на " + timeString + ". Новое время: " + newTime + " секунд.");
        } else {
            checker.sendMessage(ChatColor.RED + "Этот игрок не находится на проверке.");
        }
    }

    private long parseTime(String timeString) {
        try {
            char unit = timeString.charAt(timeString.length() - 1);
            long time = Long.parseLong(timeString.substring(0, timeString.length() - 1));

            switch (unit) {
                case 's':
                    return time;
                case 'm':
                    return time * 60;
                case 'h':
                    return time * 3600;
                case 'd':
                    return time * 86400;
                default:
                    return -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void removeBossBar(UUID targetId) {
        BossBar bossBar = plugin.getBossBars().remove(targetId);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public String mossage(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
