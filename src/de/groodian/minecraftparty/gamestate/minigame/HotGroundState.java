package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotGroundState extends MiniGame {

    private Location location;

    private BukkitTask gameTask;

    private Map<Block, Integer> blocksToRemove;
    private List<Block> blocksToRemoveToRemove;

    public HotGroundState(String name, Main plugin) {
        super(name, plugin);
        super.timeRecords = true;
        super.setRecords = false;
        super.timeScoreboardGame = false;
        this.location = plugin.getLocationManager().HOTGROUND;
        this.gameTask = null;
        this.blocksToRemove = new HashMap<>();
        this.blocksToRemoveToRemove = new ArrayList<>();
    }

    @Override
    protected void prepare() {
        location = plugin.getLocationManager().HOTGROUND;
        int radius = MainConfig.getInt("HotGround.radius");
        Material material = Material.valueOf(MainConfig.getString("HotGround.material"));
        for (int i = -radius; i < radius; i++) {
            for (int j = -radius; j < radius; j++) {
                Location temp = location.clone();
                temp.add(i, 0, j);
                if (location.distance(temp) < radius) {
                    temp.getBlock().setType(material);
                    temp.add(0, -20, 0);
                    temp.getBlock().setType(material);
                    temp.add(0, -20, 0);
                    temp.getBlock().setType(material);
                }
            }
        }
    }

    @Override
    protected void beforeCountdownStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(location.clone().add(0, 3, 0));
            plugin.getTeleportFix().doFor(player);
        }
    }

    @Override
    protected void startMiniGame() {
        gameTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : plugin.getPlayers()) {
                    if (!diePlayers.contains(player)) {

                        ranking.put(player, (int) (System.currentTimeMillis() - startTime) / 1000);

                        if (!blocksToRemove.containsKey(player.getLocation().add(0, -1, 0).getBlock()))
                            blocksToRemove.put(player.getLocation().add(0, -1, 0).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(0, -1, -0.5).getBlock()))
                            blocksToRemove.put(player.getLocation().add(0, -1, -0.5).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(0, -1, 0.5).getBlock()))
                            blocksToRemove.put(player.getLocation().add(0, -1, 0.5).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(-0.5, -1, 0).getBlock()))
                            blocksToRemove.put(player.getLocation().add(-0.5, -1, 0).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(0.5, -1, 0).getBlock()))
                            blocksToRemove.put(player.getLocation().add(0.5, -1, 0).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(0.5, -1, -0.5).getBlock()))
                            blocksToRemove.put(player.getLocation().add(0.5, -1, -0.5).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(-0.5, -1, -0.5).getBlock()))
                            blocksToRemove.put(player.getLocation().add(-0.5, -1, -0.5).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(0.5, -1, 0.5).getBlock()))
                            blocksToRemove.put(player.getLocation().add(0.5, -1, 0.5).getBlock(), 4);

                        if (!blocksToRemove.containsKey(player.getLocation().add(-0.5, -1, 0.5).getBlock()))
                            blocksToRemove.put(player.getLocation().add(-0.5, -1, 0.5).getBlock(), 4);

                        if (player.getLocation().getY() < (location.getY() - 43)) {
                            addDiePlayer(player);
                            plugin.getRecord().setRecord(player, "hotground", System.currentTimeMillis() - startTime, true);
                            player.teleport(location);
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            for (Player current : Bukkit.getOnlinePlayers()) {
                                current.hidePlayer(player);
                            }
                        }
                    }
                }

                for (Map.Entry<Block, Integer> current : blocksToRemove.entrySet()) {
                    if (current.getValue() <= 0) {
                        current.getKey().setType(Material.AIR);
                        blocksToRemoveToRemove.add(current.getKey());
                    } else {
                        current.setValue(current.getValue() - 1);
                    }
                }

                for (Block current : blocksToRemoveToRemove) {
                    blocksToRemove.remove(current);
                }

                if (plugin.getPlayers().size() == (diePlayers.size() + 1) || plugin.getPlayers().size() == 1 || diePlayers.size() == plugin.getPlayers().size()) {
                    for (Player player : plugin.getPlayers()) {
                        if (!diePlayers.contains(player)) {
                            plugin.getRecord().setRecord(player, "hotground", (System.currentTimeMillis() - startTime) + 1000, true);
                            ranking.put(player, (int) ((System.currentTimeMillis() - startTime) + 1000) / 1000);
                        }
                    }

                    for (Player player : plugin.getPlayers()) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (Player current : plugin.getPlayers()) {
                            player.showPlayer(current);
                        }
                    }

                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();

                }

            }

        }.runTaskTimer(plugin, 0, 2);

    }

}
