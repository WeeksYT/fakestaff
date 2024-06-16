package me.angellime.stafffaketime.Command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckTabCompleter implements TabCompleter {

    private List<String> banReasons;

    public CheckTabCompleter(List<String> banReasons) {
        this.banReasons = banReasons;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Если первый аргумент, предоставляем список ников игроков
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            completions = completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

            // Если второй аргумент, предоставляем команды "start", "confirm", "ban"
        } else if (args.length == 2) {
            completions.add("start");
            completions.add("confirm");
            completions.add("ban");
            completions.add("addtime");
            completions = completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());

            // Если третий аргумент и команда "ban", предоставляем список причин
        } else if (args.length == 3 && args[1].equalsIgnoreCase("ban")) {
            completions.addAll(banReasons);
            completions = completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }

}
