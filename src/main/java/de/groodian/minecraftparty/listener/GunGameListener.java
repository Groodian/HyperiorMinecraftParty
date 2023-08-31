package de.groodian.minecraftparty.listener;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestate.minigame.GunGameState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class GunGameListener implements Listener {

    private final Main plugin;

    public GunGameListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState))
            return;
        if (e.getAction() == Action.LEFT_CLICK_AIR)
            return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOW) {
                return;
            }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void handleBowShot(EntityShootBowEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState gunGameState))
            return;

        if (!(e.getEntity() instanceof Player player))
            return;

        if (gunGameState.isStarted()) {
            if (plugin.getPlayers().contains(player)) {
                return;
            }

        }

        e.setCancelled(true);
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState gunGameState))
            return;

        if (!(e.getEntity() instanceof Player player))
            return;

        if (gunGameState.isStarted()) {
            if (plugin.getPlayers().contains(player)) {
                return;
            }

        }

        e.setCancelled(true);
    }

    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState gunGameState))
            return;

        if (!(e.getEntity() instanceof Player player))
            return;

        if (gunGameState.isStarted()) {
            if (plugin.getPlayers().contains(player)) {
                if (e.getDamager() instanceof Player damager) {
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
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState state))
            return;

        Player killed = e.getEntity();
        Player killer = e.getEntity().getKiller();

        if (killer != null) {
            e.deathMessage(Main.PREFIX.append(Messages.getWithReplace("GunGame.death-message-killed", Map.of(
                    "%killed%", killed.getName(),
                    "%killer%", killer.getName()))));
            state.getRanking().put(killer, state.getRanking().get(killer) + 1);
            state.getRanking().put(killed, (state.getRanking().get(killed) == 0) ? 0 : state.getRanking().get(killed) - 1);
            killer.getInventory().clear();
            new HSound(Sound.ENTITY_PLAYER_LEVELUP).playFor(killer);
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
                            plugin.getServer()
                                    .broadcast(Main.PREFIX.append(
                                            Messages.getWithReplace("GunGame.almost-won", Map.of("%player%", killer.getName()))));
                        }
                    }
                }
            }.runTaskLater(plugin, 2);

        } else {
            e.deathMessage(Main.PREFIX.append(Messages.getWithReplace("GunGame.death-message-died", Map.of("%killed%", killed.getName()))));
            state.getRanking().put(killed, (state.getRanking().get(killed) == 0) ? 0 : state.getRanking().get(killed) - 1);
        }

        e.getDrops().clear();
        killed.spigot().respawn();
    }

    @EventHandler
    public void handlePlayerRespawn(PlayerRespawnEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState state))
            return;

        Location location = plugin.getLocationManager().getRespawnLocation(plugin.getLocationManager().GUNGAME);
        e.setRespawnLocation(location);

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().teleport(location);
            }
        }.runTaskLater(plugin, 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (state.isStarted()) {
                    state.giveWeapon(e.getPlayer());
                }
            }
        }.runTaskLater(plugin, 20);

    }

    @EventHandler
    public void handlePlayerMove(PlayerMoveEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState gunGameState))
            return;

        if (gunGameState.isStarted()) {
            if (e.getTo().getBlock().isLiquid()) {
                if (plugin.getPlayers().contains(e.getPlayer())) {
                    e.getPlayer().damage(100000);
                }
            }
        }

    }

}
