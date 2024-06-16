package me.angellime.stafffaketime.Util;

import me.angellime.stafffaketime.Command.CheckCommand;
import me.angellime.stafffaketime.StaffFakeTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckTask extends BukkitRunnable {

    private final StaffFakeTime plugin;
    private final Player target;
    private final Player checker;
    private long timeLeft;
    private long totalTime;
    private boolean alreadyBanned;

    public CheckTask(StaffFakeTime plugin, Player target, Player checker, long timeLeft) {
        this.plugin = plugin;
        this.target = target;
        this.checker = checker;
        this.timeLeft = timeLeft;
        this.totalTime = timeLeft;
        this.alreadyBanned = false;
    }

    @Override
    public void run() {
        if (timeLeft <= 0 || !plugin.getCheckingPlayers().containsKey(target.getUniqueId())) {
            if (!alreadyBanned) {
                checker.sendMessage(mossage(plugin.getConfig().getString("message.time")));
                BossBar bossBar = plugin.getBossBars().remove(target.getUniqueId());
                if (bossBar != null) {
                    bossBar.removeAll();
                }
                Bukkit.dispatchCommand(checker, "ban " + target.getName() + " 30d 2.4(Время вышло) -s");
                alreadyBanned = true;
            }

            plugin.getCheckingPlayers().remove(target.getUniqueId());
            CheckCommand.checkInProgress = false;
            cancel();
            return;
        }

        timeLeft--;
        double progress = (double) timeLeft / totalTime;
        progress = Math.max(0.0, Math.min(1.0, progress));

        BossBar bossBar = plugin.getBossBars().get(target.getUniqueId());
        if (bossBar != null) {
            bossBar.setProgress(progress);
        }
    }

    public long addTime(long additionalTime) {
        timeLeft += additionalTime;
        totalTime += additionalTime;
        return timeLeft;
    }

    public String mossage(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void setAlreadyBanned(boolean alreadyBanned) {
        this.alreadyBanned = alreadyBanned;
    }

    public boolean isAlreadyBanned() {
        return alreadyBanned;
    }
}
