package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.boards.HTitle;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.playerhider.PlayerHider;
import de.groodian.minecraftparty.util.JumpAndRunLocations;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class JumpAndRunState extends MiniGame {

    private final JumpAndRunLocations locations;
    private final PlayerHider playerHider;
    private final Map<Player, Integer> checkpointsReached;

    private BukkitTask moveTask;
    private BukkitTask gameTask;

    public JumpAndRunState(String name, Main plugin) {
        super(name, plugin);
        super.timeRecords = true;
        super.lowerIsBetterRecords = true;
        super.lowerIsBetterGame = true;
        super.setRecords = false;
        this.locations = plugin.getLocationManager().JUMPANDRUN_LOCATIONS.get(
                new Random().nextInt(plugin.getLocationManager().JUMPANDRUN_LOCATIONS.size()));
        super.mapName = locations.getName();
        this.playerHider = new PlayerHider(plugin);
        this.moveTask = null;
        this.gameTask = null;
        this.checkpointsReached = new HashMap<>();
    }

    private void moveListener() {
        moveTask = new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : plugin.getPlayers()) {
                    if (player.getLocation().getX() != locations.getStart().getX() ||
                        player.getLocation().getZ() != locations.getStart().getZ()) {
                        player.teleport(
                                new Location(locations.getStart().getWorld(), locations.getStart().getX(), player.getLocation().getY(),
                                        locations.getStart().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
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
        plugin.getServer().broadcast(Main.PREFIX.append(Messages.getWithReplace("JumpAndRun.map", Map.of("%map%", locations.getName()))));

        moveListener();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getPlayers().contains(player)) {
                teleportManager.addTeleport(player, locations.getStart(), () -> playerHider.giveHideItem(player));
            } else {
                teleportManager.addTeleport(player, locations.getStart(), null);
            }
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
                            Component msg = Messages.getWithReplace("JumpAndRun.checkpoint-reached", Map.of("%checkpoint%",
                                    String.valueOf(count + 1), "%max-checkpoints%", String.valueOf(locations.getCheckpoints().size())));
                            player.sendMessage(Main.PREFIX.append(msg));
                            new HTitle(Duration.ofMillis(500), Duration.ofMillis(1000), Duration.ofMillis(500), Component.empty(),
                                    msg).sendTo(player);
                            new HSound(Sound.ENTITY_PLAYER_LEVELUP).playFor(player);
                            break;
                        }
                        count++;
                    }

                    if (player.getLocation().distance(locations.getWin()) < 1 &&
                        checkpointsReached.get(player) == locations.getCheckpoints().size()) {
                        if (addWinner(player)) {
                            checkpointsReached.put(player, locations.getCheckpoints().size() + 1);
                        }
                    }

                }

                if (plugin.getPlayers().size() == winner.size() + 1 || plugin.getPlayers().size() == winner.size() || winner.size() >= 5 ||
                    secondsGame <= 0 || plugin.getPlayers().size() == 1) {
                    gameTask.cancel();
                    playerHider.remove();
                    plugin.getGameStateManager().setRandomGameState();
                }

            }

        }.runTaskTimer(plugin, 0, 5);

    }

}
