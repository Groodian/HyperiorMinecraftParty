package de.groodian.minecraftparty.countdowns;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;

public class EndingCountdown extends Countdown {

    private Main plugin;
    private int seconds = 20;

    public EndingCountdown(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (seconds) {
                case 10:
                case 5:
                case 3:
                case 2:
                    Bukkit.broadcastMessage(Main.PREFIX + Messages.get("server-restart-in").replace("%seconds%", seconds + ""));
                    break;
                case 1:
                    Bukkit.broadcastMessage(Main.PREFIX + Messages.get("server-restart-in-last-second"));
                    break;
                case 0:
                    Bukkit.broadcastMessage(Main.PREFIX + Messages.get("server-restarting"));
                    stop();
                    break;
                default:
                    break;
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
