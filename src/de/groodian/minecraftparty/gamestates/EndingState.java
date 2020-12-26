package de.groodian.minecraftparty.gamestates;

import de.groodian.cosmetics.HyperiorCosmetic;
import de.groodian.hyperiorcore.boards.Title;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.countdowns.EndingCountdown;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EndingState implements GameState {

    private EndingCountdown endingCountdown;
    private Main plugin;

    private int taskID = 0;
    private int count = 0;

    public EndingState(Main plugin) {
        endingCountdown = new EndingCountdown(plugin);
        this.plugin = plugin;
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

                Bukkit.broadcastMessage(Main.PREFIX + Messages.get("the-winner-are"));
                new HSound(Sound.FIREWORK_LAUNCH).play();

            }
        }.runTaskLater(plugin, 35);

        new BukkitRunnable() {
            @Override
            public void run() {

                HyperiorCosmetic.enable();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.teleport(plugin.getLocationManager().LOBBY);
                    HyperiorCore.getSB().unregisterScoreboard(player);
                }

                new HSound(Sound.ENDERDRAGON_WINGS).play();

                // SORT PLAYERS START
                Map<Player, Integer> sorted = plugin.getStars().entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

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
                        new Title(20, 60, 20, title.toString(), Messages.get("one-winner-subtitle")).send();
                    } else {
                        new Title(20, 60, 20, title.toString(), Messages.get("multiple-winner-subtitle")).send();
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

                Bukkit.broadcastMessage("§7§m--------------------------------§r\n \n" + preOutput + "\n \n§7§m--------------------------------§r");
                // MESSAGE END

                plugin.getStats().gameFinished(forDatabase);
                plugin.getStats().finish();

                taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    new HSound(Sound.LEVEL_UP).play();
                    count++;
                    if (count >= 2) {
                        Bukkit.getScheduler().cancelTask(taskID);
                    }
                }, 10, 2);

                endingCountdown.start();

            }
        }.runTaskLater(plugin, 70);

    }

    @Override
    public void stop() {
        for (Player all : Bukkit.getOnlinePlayers()) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("Connect");
                out.writeUTF(MainConfig.getString("fallback-server"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            all.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> Bukkit.shutdown(), 40);
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cENDING STATE STOPPED!");
    }

    private String getColor(int place) {
        switch (place) {
            case 1:
                return "§6";
            case 2:
                return "§7";
            default:
                return "§c";
        }
    }

}
