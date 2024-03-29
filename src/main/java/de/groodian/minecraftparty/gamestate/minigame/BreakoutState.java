package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BreakoutState extends MiniGame {

    private final List<Location> players;
    private final Location spectator;
    private final Map<Player, Location> playersLocation;

    private final int depth;
    private final List<Material> materials;
    private final List<ItemStack> tools;

    private BukkitTask gameTask;

    public BreakoutState(String name, Main plugin) {
        super(name, plugin);
        super.timeRecords = true;
        super.lowerIsBetterGame = true;
        super.setRecords = false;
        this.players = plugin.getLocationManager().BREAKOUT_PLAYERS;
        this.spectator = plugin.getLocationManager().BREAKOUT_SPECTATOR;
        this.playersLocation = new HashMap<>();
        this.depth = MainConfig.getInt("Breakout.depth");
        this.materials = MainConfig.getMaterialList("Breakout.materials");
        this.tools = MainConfig.getItemWithNameList("Breakout.tools");
        this.gameTask = null;
    }

    @Override
    protected void prepare() {
        Random random = new Random();
        for (int i = 0; i < depth + 1; i++) {
            Material material = materials.get(random.nextInt(materials.size()));
            for (Location location : players) {
                location.clone().add(0, -i - 1, 0).getBlock().setType(material);
            }
        }
    }

    @Override
    protected void beforeCountdownStart() {
        int locationCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getPlayers().contains(player)) {
                Location loc = players.get(locationCount);
                teleportManager.addTeleport(player, loc.clone().add(0, 2, 0), () -> {
                    HyperiorCore.getPaper().getGlowingBlock().send(player, loc.clone().add(0, -1, 0), 400);
                });
                playersLocation.put(player, loc);
                locationCount++;
            } else {
                teleportManager.addTeleport(player, spectator, null);
            }
        }
    }

    @Override
    protected void startMiniGame() {
        for (Player player : plugin.getPlayers()) {
            player.getInventory().clear();
            for (ItemStack item : tools) {
                player.getInventory().addItem(item);
            }
        }

        gameTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : plugin.getPlayers()) {

                    int distance = (int) (player.getLocation().getY() - (playersLocation.get(player).getY() - (depth + 1)));

                    if (!winner.contains(player)) {
                        ranking.put(player, distance);
                    }

                    if (distance <= 0 && !winner.contains(player)) {
                        addWinner(player);
                    }
                }

                if (plugin.getPlayers().size() == winner.size() + 1 || plugin.getPlayers().size() == winner.size() || winner.size() >= 5 ||
                    secondsGame <= 0 || plugin.getPlayers().size() == 1) {
                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();
                }

            }
        }.runTaskTimer(plugin, 0, 5);
    }

    public boolean isStarted() {
        return started;
    }

}
