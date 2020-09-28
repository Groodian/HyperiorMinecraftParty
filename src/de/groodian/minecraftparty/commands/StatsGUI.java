package de.groodian.minecraftparty.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.util.JumpAndRunLocations;

public class StatsGUI {

	private Main plugin;

	public StatsGUI(Main plugin) {
		this.plugin = plugin;
	}

	public void openGUI(Player player, final String statsUUID, String statsName) {
		Inventory inventory = Bukkit.createInventory(null, 45, Messages.get("Commands.stats.inventory.name").replace("%player%", statsName));

		new BukkitRunnable() {
			public void run() {
				inventory.setItem(0, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
				inventory.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
				inventory.setItem(36, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
				inventory.setItem(44, new ItemBuilder(Material.STAINED_GLASS_PANE).setName("§a").build());
			}
		}.runTaskLaterAsynchronously(plugin, 5);

		new BukkitRunnable() {
			public void run() {
				inventory.setItem(1, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
				inventory.setItem(7, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
				inventory.setItem(37, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
				inventory.setItem(43, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName("§a").build());
			}
		}.runTaskLaterAsynchronously(plugin, 10);

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
		}.runTaskLaterAsynchronously(plugin, 15);

		new BukkitRunnable() {
			public void run() {

				List<String> globalLores = Messages.getStringList("Commands.stats.inventory.items.global.lores");
				List<String> globalLoresReplaced = new ArrayList<>();
				for (String string : globalLores) {
					string = string.replace("%place%", ((plugin.getStats().getRank(statsUUID)) == -1 ? "-" : plugin.getStats().getRank(statsUUID)) + "");
					string = string.replace("%points%", plugin.getStats().getPoints(statsUUID) + "");
					string = string.replace("%play-time%", convertPlayTime(plugin.getStats().getPlayTime(statsUUID)) + "");
					string = string.replace("%games-played%", plugin.getStats().getGamesPlayed(statsUUID) + "");
					string = string.replace("%games-played-till-end%", plugin.getStats().getGamesEnded(statsUUID) + "");
					string = string.replace("%times-first%", plugin.getStats().getGamesFirst(statsUUID) + "");
					string = string.replace("%times-second%", plugin.getStats().getGamesSecond(statsUUID) + "");
					string = string.replace("%times-third%", plugin.getStats().getGamesThird(statsUUID) + "");
					string = string.replace("%times-fourth%", plugin.getStats().getGamesFourth(statsUUID) + "");
					string = string.replace("%times-fifth%", plugin.getStats().getGamesFifth(statsUUID) + "");
					globalLoresReplaced.add(string);
				}
				inventory.setItem(12, new ItemBuilder(Material.BOOK_AND_QUILL).setName(Messages.get("Commands.stats.inventory.items.global.name")).setLore(globalLoresReplaced.toArray(new String[0])).build());

				List<String> minigamesLores = Messages.getStringList("Commands.stats.inventory.items.minigames.lores");
				List<String> minigamesLoresReplaced = new ArrayList<>();
				for (String string : minigamesLores) {
					string = string.replace("%minigames-played%", plugin.getStats().getMiniGamesPlayed(statsUUID) + "");
					string = string.replace("%times-first%", plugin.getStats().getMiniGamesFirst(statsUUID) + "");
					string = string.replace("%times-second%", plugin.getStats().getMiniGamesSecond(statsUUID) + "");
					string = string.replace("%times-third%", plugin.getStats().getMiniGamesThird(statsUUID) + "");
					string = string.replace("%times-fourth%", plugin.getStats().getMiniGamesFourth(statsUUID) + "");
					string = string.replace("%times-fifth%", plugin.getStats().getMiniGamesFifth(statsUUID) + "");
					minigamesLoresReplaced.add(string);
				}
				inventory.setItem(14, new ItemBuilder(Material.BOOK).setName(Messages.get("Commands.stats.inventory.items.minigames.name")).setLore(minigamesLoresReplaced.toArray(new String[0])).build());

				List<String> woolblockLores = Messages.getStringList("Commands.stats.inventory.items.woolblock.lores");
				List<String> woolblockLoresReplaced = new ArrayList<>();
				for (String string : woolblockLores) {
					string = string.replace("%rounds%", convert(plugin.getRecord().getRecord(statsUUID, "woolblock")) + "");
					woolblockLoresReplaced.add(string);
				}
				inventory.setItem(29, new ItemBuilder(Material.WOOL).setName(Messages.get("Commands.stats.inventory.items.woolblock.name")).setLore(woolblockLoresReplaced.toArray(new String[0])).build());

				List<String> trafficlightraceLores = Messages.getStringList("Commands.stats.inventory.items.trafficlightrace.lores");
				List<String> trafficlightraceLoresReplaced = new ArrayList<>();
				for (String string : trafficlightraceLores) {
					string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(statsUUID, "trafficlightrace")) + "");
					trafficlightraceLoresReplaced.add(string);
				}
				inventory.setItem(30,
						new ItemBuilder(Material.WOOL, (short) 5).setName(Messages.get("Commands.stats.inventory.items.trafficlightrace.name")).setLore(trafficlightraceLoresReplaced.toArray(new String[0])).build());

				List<String> gungameLores = Messages.getStringList("Commands.stats.inventory.items.gungame.lores");
				List<String> gungameLoresReplaced = new ArrayList<>();
				for (String string : gungameLores) {
					string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(statsUUID, "gungame")) + "");
					gungameLoresReplaced.add(string);
				}
				inventory.setItem(31, new ItemBuilder(Material.WOOD_AXE).setName(Messages.get("Commands.stats.inventory.items.gungame.name")).setLore(gungameLoresReplaced.toArray(new String[0])).build());

				List<String> jumpandrunLores = Messages.getStringList("Commands.stats.inventory.items.jumpandrun.lores");
				List<String> jumpandrunLoresReplaced = new ArrayList<>();
				for (String string : jumpandrunLores) {
					if (string.contains("%all-maps%") && string.contains("%all-times%")) {
						for (JumpAndRunLocations jumpAndRun : plugin.getLocationManager().JUMPANDRUN_LOCATIONS) {
							String temp = string.replace("%all-times%", convertTime(plugin.getRecord().getRecord(statsUUID, "jumpandrun" + jumpAndRun.getName())) + "").replace("%all-maps%", jumpAndRun.getName());
							jumpandrunLoresReplaced.add(temp);
						}
						continue;
					}
					jumpandrunLoresReplaced.add(string);
				}
				inventory.setItem(32, new ItemBuilder(Material.FEATHER).setName(Messages.get("Commands.stats.inventory.items.jumpandrun.name")).setLore(jumpandrunLoresReplaced.toArray(new String[0])).build());

				List<String> hotgroundLores = Messages.getStringList("Commands.stats.inventory.items.hotground.lores");
				List<String> hotgroundLoresReplaced = new ArrayList<>();
				for (String string : hotgroundLores) {
					string = string.replace("%time%", convertTime(plugin.getRecord().getRecord(statsUUID, "hotground")) + "");
					hotgroundLoresReplaced.add(string);
				}
				inventory.setItem(33, new ItemBuilder(Material.NETHERRACK).setName(Messages.get("Commands.stats.inventory.items.hotground.name")).setLore(hotgroundLoresReplaced.toArray(new String[0])).build());

				List<String> colorbattleLores = Messages.getStringList("Commands.stats.inventory.items.colorbattle.lores");
				List<String> colorbattleLoresReplaced = new ArrayList<>();
				for (String string : colorbattleLores) {
					string = string.replace("%blocks%", convert(plugin.getRecord().getRecord(statsUUID, "colorbattle")) + "");
					colorbattleLoresReplaced.add(string);
				}
				inventory.setItem(40, new ItemBuilder(Material.BOW).setName(Messages.get("Commands.stats.inventory.items.colorbattle.name")).setLore(colorbattleLoresReplaced.toArray(new String[0])).build());

			}
		}.runTaskLaterAsynchronously(plugin, 20);

		player.openInventory(inventory);
		new HSound(Sound.CHEST_OPEN).playFor(player);

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
