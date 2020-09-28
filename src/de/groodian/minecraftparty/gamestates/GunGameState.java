package de.groodian.minecraftparty.gamestates;

import java.util.ArrayList;
import java.util.Collections;
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

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;

public class GunGameState extends MiniGame {

	private List<Location> locations;
	private List<ItemStack> items;

	private BukkitTask gameTask;

	public GunGameState(String name, Main plugin) {
		super(name, plugin);
		super.timeRecords = true;
		super.lowerIsBetterRecords = true;
		super.setRecords = false;
		this.locations = plugin.getLocationManager().GUNGAME;
		this.items = MainConfig.getItemWithNameList("GunGame.items");
		this.gameTask = null;
	}

	public Location getRandomLocation() {

		Collections.shuffle(locations);

		for (Location respawnLoc : locations) {

			List<Double> distance = new ArrayList<>();
			for (Player player : plugin.getPlayers()) {
				distance.add(respawnLoc.distance(player.getLocation()));
			}

			boolean isDistance = true;
			for (Double current : distance) {
				if (current < 20) {
					isDistance = false;
				}
			}

			if (isDistance) {
				return respawnLoc;
			}
		}

		return locations.get(new Random().nextInt(locations.size()));
	}

	public void giveWeapon(Player player) {
		if (started) {

			int temp = ranking.get(player);

			if (temp >= items.size()) {
				player.getInventory().setItem(0, items.get(items.size() - 1));
			} else {
				player.getInventory().setItem(0, items.get(temp));
			}

			if (player.getInventory().getItem(0).getType() == Material.BOW) {
				player.getInventory().setItem(8, new ItemBuilder(Material.ARROW).setAmount(MainConfig.getInt("GunGame.arrow-amount")).setName(Messages.get("GunGame.arrow")).build());
			}

		}

	}

	@Override
	protected void prepare() {
	}

	@Override
	protected void beforeCountdownStart() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.teleport(getRandomLocation());
			plugin.getTeleportFix().doFor(player);
		}
	}

	@Override
	protected void startMiniGame() {

		for (Player player : plugin.getPlayers()) {
			ranking.put(player, 0);
			giveWeapon(player);
		}

		gameTask = new BukkitRunnable() {

			@Override
			public void run() {
				if (plugin.getPlayers().size() == 1 || secondsGame <= 0 || ranking.containsValue(items.size())) {

					for (Player player : plugin.getPlayers()) {
						if (ranking.get(player) == 7) {
							plugin.getRecord().setRecord(player, "gungame", System.currentTimeMillis() - startTime, false);
						}
					}

					gameTask.cancel();
					plugin.getGameStateManager().setRandomGameState();
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 5);
	}

	public boolean isStarted() {
		return started;
	}

	public Map<Player, Integer> getRanking() {
		return ranking;
	}

	public List<ItemStack> getItems() {
		return items;
	}
}
