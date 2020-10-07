package de.groodian.minecraftparty.listeners;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import de.groodian.hyperiorcore.boards.Tablist;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.countdowns.LobbyCountdown;
import de.groodian.minecraftparty.gamestates.LobbyState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;

public class LobbyListener implements Listener {

	private Main plugin;

	public LobbyListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void handlePlayerLogin(PlayerLoginEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState))
			return;
		if (plugin.getPlayers().size() >= Main.MAX_PLAYERS) {
			Player player = e.getPlayer();
			if (HyperiorCore.getRanks().has(player.getUniqueId(), "minecraftparty.premiumjoin")) {
				for (Player current : plugin.getPlayers()) {
					if (!HyperiorCore.getRanks().has(player.getUniqueId(), "minecraftparty.premiumjoin")) {
						current.sendMessage(Main.PREFIX + Messages.get("premium-player-kicked-you"));
						ByteArrayOutputStream b = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(b);
						try {
							out.writeUTF("Connect");
							out.writeUTF(MainConfig.getString("fallback-server"));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						current.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
						e.allow();
						return;
					}
				}
				e.disallow(PlayerLoginEvent.Result.KICK_FULL, Main.PREFIX + Messages.get("all-player-has-premium"));
			} else {
				e.disallow(PlayerLoginEvent.Result.KICK_FULL, Main.PREFIX + Messages.get("you-need-premium"));
			}
		} else {
			e.allow();
		}
	}

	@EventHandler
	public void handlePlayerJoin(PlayerJoinEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState))
			return;
		Player player = e.getPlayer();
		String rank = plugin.getStats().getRank(player.getUniqueId().toString().replaceAll("-", "")) + "";
		String points = plugin.getStats().getPoints(player.getUniqueId().toString().replaceAll("-", "")) + "";
		String wins = plugin.getStats().getGamesFirst(player.getUniqueId().toString().replaceAll("-", "")) + "";
		if (rank.equals("-1")) {
			rank = "-";
		}
		if (points.equals("-1")) {
			points = "-";
		}
		if (wins.equals("-1")) {
			wins = "-";
		}
		new Tablist(Messages.get("tablist-header").replace("%server-number%", Bukkit.getServerName() + ""), Messages.get("tablist-footer").replace("%rank%", rank).replace("%points%", points).replace("%wins%", wins)).sendTo(player);
		plugin.getPlayers().add(player);
		player.setExp(0);
		player.setLevel(0);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setGameMode(GameMode.SURVIVAL);
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(player.getName());
		meta.setDisplayName(Messages.get("stats-item-name"));
		skull.setItemMeta(meta);
		player.getInventory().setItem(0, skull);
		player.getInventory().setItem(7, new ItemBuilder(Material.FIREWORK_CHARGE).setName("�cKein Gadget ausgew�hlt!").build());
		player.getInventory().setItem(8, new ItemBuilder(Material.CHEST).setName("�6�lTrails �7(Rechtsklick)").build());
		e.setJoinMessage(Main.PREFIX + Messages.get("join-message").replace("%player%", player.getDisplayName()).replace("%current-players%", ((plugin.getPlayers().size() > Main.MAX_PLAYERS) ? Main.MAX_PLAYERS : plugin.getPlayers().size()) + "").replace("%max-players%", Main.MAX_PLAYERS + ""));
		player.teleport(plugin.getLocationManager().LOBBY);

		LobbyState lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
		LobbyCountdown countdown = lobbyState.getCountdown();
		if (plugin.getPlayers().size() >= Main.MIN_PLAYERS) {
			if (!countdown.isRunning()) {
				countdown.stopIdle();
				countdown.start();
			}
		}

		if (MainConfig.getBoolean("Scoreboard.animation")) {
			HyperiorCore.getSB().registerScoreboard(player, Messages.get("Scoreboard.title"), 5, MainConfig.getInt("Scoreboard.delay"), MainConfig.getInt("Scoreboard.delay-between-animation"));
		} else {
			HyperiorCore.getSB().registerScoreboard(player, Messages.get("Scoreboard.title"), 5);
		}

		lobbyState.updateScoreboard();

		plugin.getClient().sendUpdate();
	}

	@EventHandler
	public void handlePlayerQuit(PlayerQuitEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState))
			return;
		Player player = e.getPlayer();
		plugin.getPlayers().remove(player);
		e.setQuitMessage(Main.PREFIX + Messages.get("quit-message").replace("%player%", player.getDisplayName()).replace("%current-players%", plugin.getPlayers().size() + "").replace("%max-players%", Main.MAX_PLAYERS + ""));

		LobbyState lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
		LobbyCountdown countdown = lobbyState.getCountdown();
		if (plugin.getPlayers().size() < Main.MIN_PLAYERS) {
			if (countdown.isRunning()) {
				countdown.stop();
				countdown.startIdle();
			}
		}
		
		lobbyState.updateScoreboard();

		plugin.getClient().sendUpdate();
	}

}
