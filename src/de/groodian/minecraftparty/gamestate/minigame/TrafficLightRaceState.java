package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.boards.Title;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TrafficLightRaceState extends MiniGame {

    private Location start;
    private Location win;

    private BukkitTask moveStartTask;
    private BukkitTask moveTask;
    private BukkitTask gameTask;

    private boolean delayIsRunning;
    private boolean noMoving;

    private Map<Player, Location> location;
    private Map<Player, Boolean> pushingBack;

    public TrafficLightRaceState(String name, Main plugin) {
        super(name, plugin);
        super.timeRecords = true;
        super.lowerIsBetterRecords = true;
        super.lowerIsBetterGame = true;
        super.setRecords = false;
        this.start = plugin.getLocationManager().TRAFFICLIGHTRACE_START;
        this.win = plugin.getLocationManager().TRAFFICLIGHTRACE_WIN;
        this.moveStartTask = null;
        this.moveTask = null;
        this.gameTask = null;
        this.delayIsRunning = false;
        this.noMoving = false;
        this.location = new HashMap<>();
        this.pushingBack = new HashMap<>();
    }

    private void moveStartListener() {
        moveStartTask = new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : plugin.getPlayers()) {
                    if (player.getLocation().getX() <= start.getX() - 0.5D) {
                        player.teleport(new Location(start.getWorld(), start.getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
                    }
                }

            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void moveListener() {
        for (Player p : plugin.getPlayers()) {
            pushingBack.put(p, false);
        }

        moveTask = new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {

                for (Player p : plugin.getPlayers()) {
                    if (pushingBack.containsKey(p) && pushingBack.get(p) && p.isOnGround()) {
                        p.teleport(location.get(p));
                        pushingBack.put(p, false);
                    }

                    // If the distance between the two points is greater than 0.05
                    if (noMoving && !winner.contains(p) && (Math.abs(Math.abs(location.get(p).getX()) - Math.abs(p.getLocation().getX())) > 0.05) && !pushingBack.get(p)) {
                        p.setVelocity(new Vector(0.9D, 0.3D, 0.0D));
                        pushingBack.put(p, true);
                        new HSound(Sound.ENDERDRAGON_WINGS).playFor(p);
                    }

                    location.put(p, p.getLocation());
                }

            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    protected void prepare() {
    }

    @Override
    protected void beforeCountdownStart() {
        moveStartListener();

        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportManager.addTeleport(player, start, null);
        }
    }

    @Override
    protected void startMiniGame() {
        moveStartTask.cancel();
        moveListener();

        gameTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : plugin.getPlayers()) {

                    if (!winner.contains(player)) {
                        ranking.put(player, (int) player.getLocation().distance(win));
                    }

                    if ((int) player.getLocation().getX() <= (int) win.getX() && !winner.contains(player)) {
                        addWinner(player);
                    }

                }

                if (plugin.getPlayers().size() == winner.size() + 1 || plugin.getPlayers().size() == winner.size() || winner.size() >= 5 || secondsGame <= 0 || plugin.getPlayers().size() == 1) {
                    moveTask.cancel();
                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();
                }

                if (!delayIsRunning) {
                    delay(20 + (new Random()).nextInt(40));
                }

            }

        }.runTaskTimer(plugin, 0, 1);

    }

    private void delay(int delay) {
        delayIsRunning = true;
        noMoving = false;

        new HSound(Sound.NOTE_STICKS).play();
        new Title(5, 20, 5, "", Messages.get("TrafficLightRace.run")).send();
        for (Player p : plugin.getPlayers()) {
            for (int i = 0; i < 9; i++) {
                p.getInventory().setItem(i, (new ItemBuilder(Material.WOOL, (short) 5)).setName(Messages.get("TrafficLightRace.run")).build());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                if (started) {

                    new HSound(Sound.NOTE_STICKS).play();
                    new Title(5, 20, 5, "", Messages.get("TrafficLightRace.attention")).send();
                    for (Player p : plugin.getPlayers()) {
                        for (int i = 0; i < 9; i++) {
                            p.getInventory().setItem(i, (new ItemBuilder(Material.WOOL, (short) 1)).setName(Messages.get("TrafficLightRace.attention")).build());
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            if (started) {

                                new HSound(Sound.NOTE_STICKS).play();
                                new Title(5, 20, 5, "", Messages.get("TrafficLightRace.stop")).send();
                                for (Player p : plugin.getPlayers()) {
                                    for (int i = 0; i < 9; i++) {
                                        p.getInventory().setItem(i, (new ItemBuilder(Material.WOOL, (short) 14)).setName(Messages.get("TrafficLightRace.stop")).build());
                                    }
                                }

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {

                                        noMoving = true;

                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {

                                                delayIsRunning = false;

                                            }
                                        }.runTaskLater(plugin, 70);

                                    }
                                }.runTaskLater(plugin, 5);

                            }

                        }
                    }.runTaskLater(plugin, 30);

                }

            }
        }.runTaskLater(plugin, delay);

    }

}