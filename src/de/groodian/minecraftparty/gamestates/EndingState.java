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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EndingState implements GameState {

    private EndingCountdown endingCountdown;
    private Main plugin;

    private Map<Player, Integer> sorted = new HashMap<>();
    private Map<Player, Integer> first = new HashMap<>();
    private Map<Player, Integer> second = new HashMap<>();
    private Map<Player, Integer> third = new HashMap<>();
    private Map<Player, Integer> fourth = new HashMap<>();
    private Map<Player, Integer> fifth = new HashMap<>();

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
        for (Player player : plugin.getPlayers()) {
            plugin.getStats().gameEnded(player);
            plugin.getStats().playTime(player);
        }

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

                sorted = plugin.getStars().entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
                int temp = sorted.entrySet().iterator().next().getValue();
                int place = 1;
                String row = "";
                String output;
                String preoutput = "";

                for (Map.Entry<Player, Integer> current : sorted.entrySet()) {
                    if (!(current.getValue() == temp)) {
                        place++;
                        temp = current.getValue();
                    }
                    switch (place) {
                        case 1:
                            first.put(current.getKey(), current.getValue());
                            break;
                        case 2:
                            second.put(current.getKey(), current.getValue());
                            break;
                        case 3:
                            third.put(current.getKey(), current.getValue());
                            break;
                        case 4:
                            fourth.put(current.getKey(), current.getValue());
                            break;
                        case 5:
                            fifth.put(current.getKey(), current.getValue());
                            break;
                        default:
                            break;
                    }
                }

                for (Map.Entry<Player, Integer> current : first.entrySet()) {
                    if (row.equals("")) {
                        row = "§a" + current.getKey().getName();
                    } else {
                        row = row + "§7, §a" + current.getKey().getName();
                    }
                }

                if (first.size() == 1) {
                    new Title(20, 60, 20, row, Messages.get("one-winner-subtitle")).send();
                } else {
                    new Title(20, 60, 20, row, Messages.get("multiple-winner-subtitle")).send();
                }

                row = "";
                for (Map.Entry<Player, Integer> current : first.entrySet()) {
                    if (row.equals("")) {
                        row = "§a§l" + 1 + "§a# §7§l>> §6" + current.getKey().getName();
                    } else {
                        row = row + "§7, §6" + current.getKey().getName();
                    }
                    plugin.getStats().gameFirst(current.getKey());
                }
                if (row.equals("")) {
                    row = "§a§l" + 1 + "§a# §7§l>> §8-------";
                } else {
                    row += " §7- §e" + first.entrySet().iterator().next().getValue() + Messages.get("points");
                }
                preoutput += row + "\n";

                row = "";
                for (Map.Entry<Player, Integer> current : second.entrySet()) {
                    if (row.equals("")) {
                        row = "§a§l" + 2 + "§a# §7§l>> §7" + current.getKey().getName();
                    } else {
                        row = row + "§7, §7" + current.getKey().getName();
                    }
                    plugin.getStats().gameSecond(current.getKey());
                }
                if (row.equals("")) {
                    row = "§a§l" + 2 + "§a# §7§l>> §8-------";
                } else {
                    row += " §7- §e" + second.entrySet().iterator().next().getValue() + Messages.get("points");
                }
                preoutput += row + "\n";

                row = "";
                for (Map.Entry<Player, Integer> current : third.entrySet()) {
                    if (row.equals("")) {
                        row = "§a§l" + 3 + "§a# §7§l>> §c" + current.getKey().getName();
                    } else {
                        row = row + "§7, §c" + current.getKey().getName();
                    }
                    plugin.getStats().gameThird(current.getKey());
                }
                if (row.equals("")) {
                    row = "§a§l" + 3 + "§a# §7§l>> §8-------";
                } else {
                    row += " §7- §e" + third.entrySet().iterator().next().getValue() + Messages.get("points");
                }
                preoutput += row + "\n";

                row = "";
                for (Map.Entry<Player, Integer> current : fourth.entrySet()) {
                    if (row.equals("")) {
                        row = "§a§l" + 4 + "§a# §7§l>> §c" + current.getKey().getName();
                    } else {
                        row = row + "§7, §c" + current.getKey().getName();
                    }
                    plugin.getStats().gameFourth(current.getKey());
                }
                if (row.equals("")) {
                    row = "§a§l" + 4 + "§a# §7§l>> §8-------";
                } else {
                    row += " §7- §e" + fourth.entrySet().iterator().next().getValue() + Messages.get("points");
                }
                preoutput += row + "\n";

                row = "";
                for (Map.Entry<Player, Integer> current : fifth.entrySet()) {
                    if (row.equals("")) {
                        row = "§a§l" + 5 + "§a# §7§l>> §c" + current.getKey().getName();
                    } else {
                        row = row + "§7, §c" + current.getKey().getName();
                    }
                    plugin.getStats().gameFifth(current.getKey());
                }
                if (row.equals("")) {
                    row = "§a§l" + 5 + "§a# §7§l>> §8-------";
                } else {
                    row += " §7- §e" + fifth.entrySet().iterator().next().getValue() + Messages.get("points");
                }
                preoutput += row + "\n";

                output = "§7§m--------------------------------§r\n \n" + preoutput + "\n \n§7§m--------------------------------§r";
                Bukkit.broadcastMessage(output);

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

}
