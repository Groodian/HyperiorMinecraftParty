package de.groodian.minecraftparty.countdown;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.util.Map;
import org.bukkit.Bukkit;

public class EndingCountdown extends Countdown {

    private final Main plugin;
    private int seconds = 20;

    public EndingCountdown(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (seconds) {
                case 10, 5, 3, 2 -> {
                    plugin.getServer()
                            .broadcast(Main.PREFIX.append(
                                    Messages.getWithReplace("server-restart-in", Map.of("%seconds%", String.valueOf(seconds)))));
                }
                case 1 -> {
                    plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("server-restart-in-last-second")));
                }
                case 0 -> {
                    plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("server-restarting")));
                    stop();
                }
                default -> {
                }
            }

            seconds--;
        }, 0, 20);
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().cancelTask(taskID);
        plugin.getGameStateManager().stopCurrentGameState();
    }

}
