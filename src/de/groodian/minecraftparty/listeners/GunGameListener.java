package de.groodian.minecraftparty.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestates.GunGameState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;

public class GunGameListener implements Listener {

	private Main plugin;

	public GunGameListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void handleInteract(PlayerInteractEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;
		if (e.getAction() == Action.LEFT_CLICK_AIR)
			return;
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.getPlayer().getItemInHand().getType() == Material.BOW) {
				return;
			}
		}
		e.setCancelled(true);
	}

	@EventHandler
	public void handleBowShot(EntityShootBowEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;

		if (!(e.getEntity() instanceof Player))
			return;

		if (((GunGameState) plugin.getGameStateManager().getCurrentGameState()).isStarted()) {
			Player player = (Player) e.getEntity();
			if (plugin.getPlayers().contains(player)) {
				return;
			}

		}

		e.setCancelled(true);
	}

	@EventHandler
	public void handleEntityDamage(EntityDamageEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;

		if (!(e.getEntity() instanceof Player))
			return;

		if (((GunGameState) plugin.getGameStateManager().getCurrentGameState()).isStarted()) {
			Player player = (Player) e.getEntity();
			if (plugin.getPlayers().contains(player)) {
				return;
			}

		}

		e.setCancelled(true);
	}

	@EventHandler
	public void handleEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;

		if (!(e.getEntity() instanceof Player))
			return;

		if (((GunGameState) plugin.getGameStateManager().getCurrentGameState()).isStarted()) {
			Player player = (Player) e.getEntity();
			if (plugin.getPlayers().contains(player)) {
				if (e.getDamager() instanceof Player) {
					Player damager = (Player) e.getDamager();
					if (plugin.getPlayers().contains(damager)) {
						return;
					}
				} else {
					return;
				}
			}

		}

		e.setCancelled(true);
	}

	@EventHandler
	public void handlePlayerDeath(PlayerDeathEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;

		Player killed = e.getEntity();
		Player killer = e.getEntity().getKiller();
		GunGameState state = (GunGameState) plugin.getGameStateManager().getCurrentGameState();

		if (killer != null) {
			e.setDeathMessage(Main.PREFIX + Messages.get("GunGame.death-message-killed").replace("%killed%", killed.getName()).replace("%killer%", killer.getName()));
			state.getRanking().put(killer, state.getRanking().get(killer) + 1);
			state.getRanking().put(killed, (state.getRanking().get(killed) == 0) ? 0 : state.getRanking().get(killed) - 1);
			killer.getInventory().clear();
			new HSound(Sound.LEVEL_UP).playFor(killer);
			state.giveWeapon(killer);
			double health = killer.getHealth() + MainConfig.getInt("GunGame.hp-get-after-kill");
			if (health > 20.0)
				health = 20.0;
			killer.setHealth(health);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					if (state.isStarted()) {
						if (state.getRanking().get(killer) == state.getItems().size() - 1) {
							Bukkit.broadcastMessage(Main.PREFIX + Messages.get("GunGame.almost-won").replace("%player%", killer.getName()));
						}
					}
				}
			}.runTaskLaterAsynchronously(plugin, 2);
			
		} else {
			e.setDeathMessage(Main.PREFIX + Messages.get("GunGame.death-message-died").replace("%killed%", killed.getName()));
			state.getRanking().put(killed, (state.getRanking().get(killed) == 0) ? 0 : state.getRanking().get(killed) - 1);
		}

		e.getDrops().clear();
		PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
		((CraftPlayer) killed).getHandle().playerConnection.a(packet);
	}

	@EventHandler
	public void handlePlayerRespawn(PlayerRespawnEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;

		GunGameState state = (GunGameState) plugin.getGameStateManager().getCurrentGameState();
		Location location = state.getRandomLocation();
		e.setRespawnLocation(location);

		new BukkitRunnable() {
			@Override
			public void run() {
				e.getPlayer().teleport(location);
			}
		}.runTaskLaterAsynchronously(plugin, 10);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (state.isStarted()) {
					state.giveWeapon(e.getPlayer());
				}
			}
		}.runTaskLaterAsynchronously(plugin, 20);

	}

	@EventHandler
	public void handlePlayerMove(PlayerMoveEvent e) {
		if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
			return;

		if (((GunGameState) plugin.getGameStateManager().getCurrentGameState()).isStarted()) {
			if (e.getTo().getBlock().isLiquid()) {
				if (plugin.getPlayers().contains(e.getPlayer())) {
					e.getPlayer().damage(100000);
				}
			}
		}

	}

}
