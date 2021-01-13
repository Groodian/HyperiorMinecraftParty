package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.hyperiorcore.util.UUIDFetcher;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.util.JumpAndRunLocations;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class StatsGUI {

    private Main plugin;
    private UUIDFetcher uuidFetcher;

    public StatsGUI(Main plugin) {
        this.plugin = plugin;
        uuidFetcher = new UUIDFetcher();
    }

    public void open(final Player player, final String statsName) {
        player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.this-take-a-moment"));

        final Player target = Bukkit.getPlayer(statsName);

        new Task(plugin) {
            @Override
            public void executeAsync() {
                String uuid;
                String name;

                if (target == null) {
                    UUIDFetcher.Result result = uuidFetcher.getNameAndUUIDFromName(statsName);
                    if (result == null) {
                        cache.add(Messages.get("Commands.stats.this-player-does-not-exist"));
                        return;
                    } else {
                        uuid = result.getUUID();
                        name = result.getName();
                    }
                } else {
                    uuid = target.getUniqueId().toString();
                    name = target.getName();
                }

                if (!plugin.getStats().isUserExists(uuid)) {
                    if (player.getName().equalsIgnoreCase(statsName)) {
                        cache.add(Messages.get("Commands.stats.you-never-played"));
                    } else {
                        cache.add(Messages.get("Commands.stats.this-player-never-played"));
                    }
                    return;
                }

                cache.add(name);

                List<String> globalLores = Messages.getStringList("Commands.stats.inventory.items.global.lores");
                List<String> globalLoresReplaced = new ArrayList<>();
                for (String string : globalLores) {
                    string = string.replace("%place%", ((plugin.getStats().getRank(uuid)) == -1 ? "-" : plugin.getStats().getRank(uuid)) + "");
                    string = string.replace("%points%", plugin.getStats().getPoints(uuid) + "");
                    string = string.replace("%play-time%", convertPlayTime(plugin.getStats().getPlayTime(uuid)) + "");
                    string = string.replace("%games-played%", plugin.getStats().getGamesPlayed(uuid) + "");
                    string = string.replace("%games-played-till-end%", plugin.getStats().getGamesEnded(uuid) + "");
                    string = string.replace("%times-first%", plugin.getStats().getGamesFirst(uuid) + "");
                    string = string.replace("%times-second%", plugin.getStats().getGamesSecond(uuid) + "");
                    string = string.replace("%times-third%", plugin.getStats().getGamesThird(uuid) + "");
                    string = string.replace("%times-fourth%", plugin.getStats().getGamesFourth(uuid) + "");
                    string = string.replace("%times-fifth%", plugin.getStats().getGamesFifth(uuid) + "");
                    globalLoresReplaced.add(string);
                }
                cache.add(globalLoresReplaced);

                List<String> minigamesLores = Messages.getStringList("Commands.stats.inventory.items.minigames.lores");
                List<String> minigamesLoresReplaced = new ArrayList<>();
                for (String string : minigamesLores) {
                    string = string.replace("%minigames-played%", plugin.getStats().getMiniGamesPlayed(uuid) + "");
                    string = string.replace("%times-first%", plugin.getStats().getMiniGamesFirst(uuid) + "");
                    string = string.replace("%times-second%", plugin.getStats().getMiniGamesSecond(uuid) + "");
                    string = string.replace("%times-third%", plugin.getStats().getMiniGamesThird(uuid) + "");
                    string = string.replace("%times-fourth%", plugin.getStats().getMiniGamesFourth(uuid) + "");
                    string = string.replace("%times-fifth%", plugin.getStats().getMiniGamesFifth(uuid) + "");
                    minigamesLoresReplaced.add(string);
                }
                cache.add(minigamesLoresReplaced);

                List<String> woolblockLores = Messages.getStringList("Commands.stats.inventory.items.woolblock.lores");
                List<String> woolblockLoresReplaced = new ArrayList<>();
                for (String string : woolblockLores) {
                    string = string.replace("%rounds%", convert(plugin.getRecord().getRecord(uuid, "woolblock")) + "");
                    woolblockLoresReplaced.add(string);
                }
                cache.add(woolblockLoresReplaced);

                List<String> trafficlightraceLores = Messages.getStringList("Commands.stats.inventory.items.trafficlightrace.lores");
                List<String> trafficlightraceLoresReplaced = new ArrayList<>();
                for (String string : trafficlightraceLores) {
                    string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(uuid, "trafficlightrace")) + "");
                    trafficlightraceLoresReplaced.add(string);
                }
                cache.add(trafficlightraceLoresReplaced);

                List<String> gungameLores = Messages.getStringList("Commands.stats.inventory.items.gungame.lores");
                List<String> gungameLoresReplaced = new ArrayList<>();
                for (String string : gungameLores) {
                    string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(uuid, "gungame")) + "");
                    gungameLoresReplaced.add(string);
                }
                cache.add(gungameLoresReplaced);

                List<String> jumpandrunLores = Messages.getStringList("Commands.stats.inventory.items.jumpandrun.lores");
                List<String> jumpandrunLoresReplaced = new ArrayList<>();
                for (String string : jumpandrunLores) {
                    if (string.contains("%all-maps%") && string.contains("%all-times%")) {
                        for (JumpAndRunLocations jumpAndRun : plugin.getLocationManager().JUMPANDRUN_LOCATIONS) {
                            String temp = string.replace("%all-times%", convertTime(plugin.getRecord().getRecord(uuid, "jumpandrun" + jumpAndRun.getName())) + "").replace("%all-maps%", jumpAndRun.getName());
                            jumpandrunLoresReplaced.add(temp);
                        }
                        continue;
                    }
                    jumpandrunLoresReplaced.add(string);
                }
                cache.add(jumpandrunLoresReplaced);

                List<String> hotgroundLores = Messages.getStringList("Commands.stats.inventory.items.hotground.lores");
                List<String> hotgroundLoresReplaced = new ArrayList<>();
                for (String string : hotgroundLores) {
                    string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(uuid, "hotground")) + "");
                    hotgroundLoresReplaced.add(string);
                }
                cache.add(hotgroundLoresReplaced);

                List<String> colorbattleLores = Messages.getStringList("Commands.stats.inventory.items.colorbattle.lores");
                List<String> colorbattleLoresReplaced = new ArrayList<>();
                for (String string : colorbattleLores) {
                    string = string.replace("%blocks%", convert(plugin.getRecord().getRecord(uuid, "colorbattle")) + "");
                    colorbattleLoresReplaced.add(string);
                }
                cache.add(colorbattleLoresReplaced);

                List<String> breakoutLores = Messages.getStringList("Commands.stats.inventory.items.breakout.lores");
                List<String> breakoutLoresReplaced = new ArrayList<>();
                for (String string : breakoutLores) {
                    string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(uuid, "breakout")) + "");
                    breakoutLoresReplaced.add(string);
                }
                cache.add(breakoutLoresReplaced);

                List<String> masterbuildersLores = Messages.getStringList("Commands.stats.inventory.items.masterbuilders.lores");
                List<String> masterbuildersReplaced = new ArrayList<>();
                for (String string : masterbuildersLores) {
                    string = string.replace("%points%", convert(plugin.getRecord().getRecord(uuid, "masterbuilders")) + "");
                    masterbuildersReplaced.add(string);
                }
                cache.add(masterbuildersReplaced);
            }

            @Override
            public void executeSyncOnFinish() {
                if (cache.size() == 1) {
                    player.sendMessage(Main.PREFIX + cache.get(0));
                    return;
                }

                Inventory inventory = Bukkit.createInventory(null, 45, Messages.get("Commands.stats.inventory.name").replace("%player%", (String) cache.get(0)));

                new BukkitRunnable() {
                    public void run() {
                        inventory.setItem(0, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
                        inventory.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
                        inventory.setItem(36, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
                        inventory.setItem(44, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
                    }
                }.runTaskLater(plugin, 5);

                new BukkitRunnable() {
                    public void run() {
                        inventory.setItem(1, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(7, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(37, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(43, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                    }
                }.runTaskLater(plugin, 10);

                new BukkitRunnable() {
                    public void run() {
                        inventory.setItem(2, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(6, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(38, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(42, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(9, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(17, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(27, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                        inventory.setItem(35, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
                    }
                }.runTaskLater(plugin, 15);

                new BukkitRunnable() {
                    public void run() {
                        inventory.setItem(12, new ItemBuilder(Material.BOOK_AND_QUILL).setName(Messages.get("Commands.stats.inventory.items.global.name")).setLore(((List<String>) cache.get(1)).toArray(new String[0])).build());
                        inventory.setItem(14, new ItemBuilder(Material.BOOK).setName(Messages.get("Commands.stats.inventory.items.minigames.name")).setLore(((List<String>) cache.get(2)).toArray(new String[0])).build());
                        inventory.setItem(29, new ItemBuilder(Material.WOOL).setName(Messages.get("Commands.stats.inventory.items.woolblock.name")).setLore(((List<String>) cache.get(3)).toArray(new String[0])).build());
                        inventory.setItem(30, new ItemBuilder(Material.WOOL, (short) 5).setName(Messages.get("Commands.stats.inventory.items.trafficlightrace.name")).setLore(((List<String>) cache.get(4)).toArray(new String[0])).build());
                        inventory.setItem(31, new ItemBuilder(Material.WOOD_AXE).setName(Messages.get("Commands.stats.inventory.items.gungame.name")).setLore(((List<String>) cache.get(5)).toArray(new String[0])).build());
                        inventory.setItem(32, new ItemBuilder(Material.FEATHER).setName(Messages.get("Commands.stats.inventory.items.jumpandrun.name")).setLore(((List<String>) cache.get(6)).toArray(new String[0])).build());
                        inventory.setItem(33, new ItemBuilder(Material.NETHERRACK).setName(Messages.get("Commands.stats.inventory.items.hotground.name")).setLore(((List<String>) cache.get(7)).toArray(new String[0])).build());
                        inventory.setItem(39, new ItemBuilder(Material.BOW).setName(Messages.get("Commands.stats.inventory.items.colorbattle.name")).setLore(((List<String>) cache.get(8)).toArray(new String[0])).build());
                        inventory.setItem(40, new ItemBuilder(Material.IRON_FENCE).setName(Messages.get("Commands.stats.inventory.items.breakout.name")).setLore(((List<String>) cache.get(9)).toArray(new String[0])).build());
                        inventory.setItem(41, new ItemBuilder(Material.BRICK).setName(Messages.get("Commands.stats.inventory.items.masterbuilders.name")).setLore(((List<String>) cache.get(10)).toArray(new String[0])).build());
                    }
                }.runTaskLater(plugin, 20);


                player.openInventory(inventory);
                new HSound(Sound.CHEST_OPEN).playFor(player);

            }
        };

    }

    private String convert(int data) {
        if (data == -1)
            return "-";
        else
            return "" + data;
    }

    private String convertTime(int ms) {
        if (ms == -1) {
            return "-";
        }
        if (ms % 1000 < 100) {
            if (ms % 100 < 10) {
                return ms / 1000 + ",00" + (ms % 1000) + "s";
            }
            return ms / 1000 + ",0" + (ms % 1000) + "s";
        }
        return ms / 1000 + "," + (ms % 1000) + "s";
    }

    private String convertPlayTime(long ms) {
        if (ms == -1) {
            return "-";
        }
        if (ms < 3600000L) {
            if (ms / 1000L / 60L == 1L) {
                return ms / 1000L / 60L + Messages.get("Commands.stats.inventory.minute");
            }
            return ms / 1000L / 60L + Messages.get("Commands.stats.inventory.minutes");
        }
        if (ms / 1000L / 60L / 60L == 1L) {
            return ms / 1000L / 60L / 60L + Messages.get("Commands.stats.inventory.hour");
        }
        return ms / 1000L / 60L / 60L + Messages.get("Commands.stats.inventory.hours");
    }

}
