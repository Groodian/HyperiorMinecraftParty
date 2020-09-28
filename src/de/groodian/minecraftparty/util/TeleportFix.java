package de.groodian.minecraftparty.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import de.groodian.minecraftparty.main.Main;

public class TeleportFix implements Listener {

	private Main plugin;

	private final int TELEPORT_FIX_DELAY = 15;

	public TeleportFix(Main plugin) {
		this.plugin = plugin;
	}

	public void doFor(Player player) {
		if (plugin.getPlayers().contains(player)) {

			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {

					for (Player all : Bukkit.getOnlinePlayers()) {
						all.hidePlayer(player);
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {

							for (Player all : Bukkit.getOnlinePlayers()) {
								all.showPlayer(player);
							}
						}
					}, 1);
				}
			}, TELEPORT_FIX_DELAY);
		}

	}

}
