package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.boards.Title;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.util.JumpAndRunLocations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JumpAndRunState extends MiniGame {

    private JumpAndRunLocations locations;

    private BukkitTask moveTask;
    private BukkitTask gameTask;

    private Map<Player, Integer> checkpointsReached;

    public JumpAndRunState(String name, Main plugin) {
        super(name, plugin);
        this.locations = plugin.getLocationManager().JUMPANDRUN_LOCATIONS.get(new Random().nextInt(plugin.getLocationManager().JUMPANDRUN_LOCATIONS.size()));
        super.timeRecords = true;
        super.lowerIsBetterRecords = true;
        super.lowerIsBetterGame = true;
        super.setRecords = false;
        super.mapName = locations.getName();
        this.moveTask = null;
        this.gameTask = null;
        this.checkpointsReached = new HashMap<>();
    }

    private void moveListener() {
        moveTask = new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : plugin.getPlayers()) {
                    if (player.getLocation().getX() != locations.getStart().getX() || player.getLocation().getZ() != locations.getStart().getZ()) {
                        player.teleport(new Location(locations.getStart().getWorld(), locations.getStart().getX(), player.getLocation().getY(), locations.getStart().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
                    }
                }
            }

        }.runTaskTimer(plugin, 4, 4);

    }

    @Override
    protected void prepare() {
    }

    @Override
    protected void beforeCountdownStart() {
        Bukkit.broadcastMessage(Main.PREFIX + Messages.get("JumpAndRun.map").replace("%map%", locations.getName()));

        moveListener();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(locations.getStart());
            plugin.getTeleportFix().doFor(player);
        }
        for (Player player : plugin.getPlayers()) {
            plugin.getPlayerHider().giveHideItem(player);
        }
    }

    @Override
    protected void startMiniGame() {
        moveTask.cancel();

        for (Player player : plugin.getPlayers()) {
            checkpointsReached.put(player, 0);
        }

        gameTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : plugin.getPlayers()) {

                    if (!winner.contains(player)) {
                        ranking.put(player, (int) player.getLocation().distance(locations.getWin()));
                    }

                    if (player.getLocation().getY() <= 40) {
                        int temp = checkpointsReached.get(player);
                        if (temp <= 0) {
                            player.teleport(locations.getStart());
                        } else if (temp <= locations.getCheckpoints().size()) {
                            player.teleport(locations.getCheckpoints().get(temp - 1));
                        } else {
                            player.teleport(locations.getWin());
                        }
                    }

                    int count = 0;
                    for (Location currentLocation : locations.getCheckpoints()) {
                        if (player.getLocation().distance(currentLocation) < 1 && checkpointsReached.get(player) == count) {
                            checkpointsReached.put(player, checkpointsReached.get(player) + 1);
                            player.sendMessage(Main.PREFIX + Messages.get("JumpAndRun.checkpoint-reached").replace("%checkpoint%", count + 1 + "").replace("%max-checkpoints%", locations.getCheckpoints().size() + ""));
                            new Title(10, 20, 10, "", Messages.get("JumpAndRun.checkpoint-reached").replace("%checkpoint%", count + 1 + "").replace("%max-checkpoints%", locations.getCheckpoints().size() + "")).sendTo(player);
                            new HSound(Sound.LEVEL_UP).playFor(player);
                            break;
                        }
                        count++;
                    }

                    if (player.getLocation().distance(locations.getWin()) < 1 && checkpointsReached.get(player) == locations.getCheckpoints().size()) {
                        if (addWinner(player)) {
                            checkpointsReached.put(player, locations.getCheckpoints().size() + 1);
                        }
                    }

                }

                if (plugin.getPlayers().size() == winner.size() + 1 || plugin.getPlayers().size() == winner.size() || winner.size() >= 5 || secondsGame <= 0 || plugin.getPlayers().size() == 1) {
                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();
                }

            }

        }.runTaskTimer(plugin, 0, 5);

    }

}
