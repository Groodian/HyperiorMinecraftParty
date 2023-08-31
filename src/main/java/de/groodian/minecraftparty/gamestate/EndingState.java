package de.groodian.minecraftparty.gamestate;

import de.groodian.hyperiorcore.boards.HTitle;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.countdown.EndingCountdown;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.util.TeleportManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EndingState implements GameState {

    private final Main plugin;
    private final EndingCountdown endingCountdown;
    private final TeleportManager teleportManager;

    private BukkitTask soundTask = null;
    private int soundCounter = 0;

    public EndingState(Main plugin) {
        this.plugin = plugin;
        this.endingCountdown = new EndingCountdown(plugin);
        this.teleportManager = new TeleportManager(plugin, this::afterAllTeleported);
    }

    @Override
    public void start() {
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§aENDING STATE STARTED!");

        // must be done before the delay, because if a player disconnects while the
        // delay he gets no play time and game ended, but he need to get it because the
        // game is ended. He gets not removed from the stars map. (look in MainListener)
        plugin.getStats().gameEnded();

        new BukkitRunnable() {
            @Override
            public void run() {

                plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("the-winner-are")));
                new HSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    teleportManager.addTeleport(player, plugin.getLocationManager().LOBBY,
                            () -> HyperiorCore.getPaper().getScoreboard().unregisterScoreboard(player));
                }

                teleportManager.startTeleporting();

            }
        }.runTaskLater(plugin, 35);

    }

    private void afterAllTeleported() {
        //HyperiorCosmetic.enable();

        playSound();

        // SORT PLAYERS START
        Map<Player, Integer> sorted = plugin.getStars()
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        Map<Player, Integer> forDatabase = new HashMap<>();

        List<List<Player>> winner = new ArrayList<>();
        List<Player> playersAtTheSamePlace = new ArrayList<>();

        int temp = sorted.entrySet().iterator().next().getValue();
        int place = 1;

        for (Map.Entry<Player, Integer> current : sorted.entrySet()) {
            if (!(current.getValue() == temp)) {
                temp = current.getValue();
                winner.add(playersAtTheSamePlace);
                playersAtTheSamePlace = new ArrayList<>();
                place++;
            }
            playersAtTheSamePlace.add(current.getKey());
            forDatabase.put(current.getKey(), place);
        }
        winner.add(playersAtTheSamePlace);
        // SORT PLAYERS END

        // TITLE START
        if (winner.size() > 0) {
            List<Player> firstPlace = winner.get(0);
            StringBuilder title = new StringBuilder();

            for (Player player : firstPlace) {
                if (title.length() == 0) {
                    title.append("§a").append(player.getName());
                } else {
                    title.append("§7, §a").append(player.getName());
                }
            }

            if (firstPlace.size() == 1) {
                new HTitle(Duration.ofMillis(1000), Duration.ofMillis(3000), Duration.ofMillis(1000),
                        LegacyComponentSerializer.legacySection().deserialize(title.toString()),
                        Messages.get("one-winner-subtitle")).send();
            } else {
                new HTitle(Duration.ofMillis(1000), Duration.ofMillis(3000), Duration.ofMillis(1000),
                        LegacyComponentSerializer.legacySection().deserialize(title.toString()),
                        Messages.get("multiple-winner-subtitle")).send();
            }
        }
        // TITLE END

        // MESSAGE START
        StringBuilder preOutput = new StringBuilder();

        place = 1;

        for (List<Player> current : winner) {
            StringBuilder row = new StringBuilder();
            for (Player player : current) {
                if (row.length() == 0) {
                    row.append("§a§l").append(place).append("§a# §7§l>> ").append(getColor(place)).append(player.getName());
                } else {
                    row.append("§7, ").append(getColor(place)).append(player.getName());
                }
            }
            row.append(" §7- §e").append(plugin.getStars().get(current.get(0))).append(Messages.get("points"));
            preOutput.append(row).append("\n");
            place++;
        }

        plugin.getServer()
                .broadcast(LegacyComponentSerializer.legacySection()
                        .deserialize(
                                "§7§m--------------------------------§r\n \n" + preOutput + "\n \n§7§m--------------------------------§r"));
        // MESSAGE END

        plugin.getStats().gameFinished(forDatabase);
        plugin.getStats().finish();

        endingCountdown.start();
    }

    private void playSound() {
        new HSound(Sound.ENTITY_ENDER_DRAGON_FLAP).play();

        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                new HSound(Sound.ENTITY_PLAYER_LEVELUP).play();
                soundCounter++;
                if (soundCounter > 3) {
                    soundTask.cancel();
                }
            }
        }.runTaskTimer(plugin, 4, 2);
    }

    @Override
    public void stop() {
        plugin.stopServer();
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cENDING STATE STOPPED!");
    }

    private String getColor(int place) {
        return switch (place) {
            case 1 -> "§6";
            case 2 -> "§7";
            default -> "§c";
        };
    }

}
