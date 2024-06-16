package me.angellime.stafffaketime.Util;

import me.angellime.stafffaketime.StaffFakeTime;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CheckTask extends BukkitRunnable {

    private final StaffFakeTime plugin;
    private final Player target;
    private final Player checker;
    private long timeLeft;

    public CheckTask(StaffFakeTime plugin, Player target, Player checker, long timeLeft) {
        this.plugin = plugin;
        this.target = target;
        this.checker = checker;
        this.timeLeft = timeLeft;
    }

    @Override
    public void run() {
        UUID targetUUID = target.getUniqueId();

        // Проверяем, что время проверки истекло или игрок больше не на проверке
        if (timeLeft <= 0 || !plugin.getCheckingPlayers().containsKey(targetUUID)) {
            checker.sendMessage(mossage(plugin.getConfig().getString("message.time")));
            BossBar bossBar = plugin.getBossBars().remove(targetUUID);
            if (bossBar != null) {
                bossBar.removeAll();
            }
            cancel();
            return;
        }

        // Обновляем прогресс BossBar
        BossBar bossBar = plugin.getBossBars().get(targetUUID);
        if (bossBar != null) {
            bossBar.setProgress((double) timeLeft / plugin.getCheckDuration());
        }

        timeLeft--;
    }

    public String mossage(String string){

        return ChatColor.translateAlternateColorCodes('&', string);

    }
}
