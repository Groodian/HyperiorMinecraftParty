package de.groodian.minecraftparty.util;

import de.groodian.minecraftparty.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TeleportManager {

    private Main plugin;
    private Runnable onFinish;
    private List<TeleportData> teleportDataList;
    private BukkitTask teleportTask;
    private int teleportCounter;

    public TeleportManager(Main plugin, Runnable onFinish) {
        this.plugin = plugin;
        this.onFinish = onFinish;
        teleportDataList = new ArrayList<>();
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
                    all.hidePlayer(player);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                    for (Player all : Bukkit.getOnlinePlayers()) {
                        all.showPlayer(player);
                    }

                }, 2);

            }, 15);
        }

    }

    public void addTeleport(Player player, Location location, Runnable runnable) {
        teleportDataList.add(new TeleportData(player, location, runnable));
    }

    private static class TeleportData {

        private Player player;
        private Location location;
        private Runnable runnable;

        public TeleportData(Player player, Location location, Runnable runnable) {
            this.player = player;
            this.location = location;
            this.runnable = runnable;
        }

    }

}
