package de.groodian.minecraftparty.util;

import de.groodian.minecraftparty.main.Main;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TeleportManager {

    private final Main plugin;
    private final Runnable onFinish;
    private final List<TeleportData> teleportDataList;
    private BukkitTask teleportTask;
    private int teleportCounter;

    public TeleportManager(Main plugin, Runnable onFinish) {
        this.plugin = plugin;
        this.onFinish = onFinish;
        this.teleportDataList = new ArrayList<>();
    }

    public void startTeleporting() {
        teleportTask = new BukkitRunnable() {
            @Override
            public void run() {

                if (teleportCounter < teleportDataList.size()) {
                    TeleportData teleportData = teleportDataList.get(teleportCounter);
                    Player player = teleportData.player;
                    player.teleport(teleportData.location);

                    if (plugin.getPlayers().contains(player)) {
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.setFireTicks(0);
                        player.setHealth(20);
                        teleportFix(player);
                    }

                    if (teleportData.runnable != null) {
                        teleportData.runnable.run();
                    }

                    teleportCounter++;
                } else {
                    teleportTask.cancel();
                    onFinish.run();
                }

            }

        }.runTaskTimer(plugin, 0, 5);

    }

    private void teleportFix(Player player) {
        if (plugin.getPlayers().contains(player)) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                for (Player all : Bukkit.getOnlinePlayers()) {
                    all.hidePlayer(plugin, player);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                    for (Player all : Bukkit.getOnlinePlayers()) {
                        all.showPlayer(plugin, player);
                    }

                }, 2);

            }, 15);
        }

    }

    public void addTeleport(Player player, Location location, Runnable runnable) {
        teleportDataList.add(new TeleportData(player, location, runnable));
    }

    private static class TeleportData {

        private final Player player;
        private final Location location;
        private final Runnable runnable;

        public TeleportData(Player player, Location location, Runnable runnable) {
            this.player = player;
            this.location = location;
            this.runnable = runnable;
        }

    }

}
