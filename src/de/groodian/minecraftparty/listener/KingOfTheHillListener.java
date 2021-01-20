package de.groodian.minecraftparty.listener;

import de.groodian.minecraftparty.gamestate.minigame.KingOfTheHillState;
import de.groodian.minecraftparty.main.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class KingOfTheHillListener implements Listener {

    private Main plugin;

    public KingOfTheHillListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof KingOfTheHillState))
            return;

        if (!(e.getEntity() instanceof Player))
            return;

        if (((KingOfTheHillState) plugin.getGameStateManager().getCurrentGameState()).isStarted()) {
            Player player = (Player) e.getEntity();
            if (plugin.getPlayers().contains(player)) {
                return;
            }

        }

        e.setCancelled(true);
    }

    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof KingOfTheHillState))
            return;

        if (!(e.getEntity() instanceof Player))
            return;

        if (((KingOfTheHillState) plugin.getGameStateManager().getCurrentGameState()).isStarted()) {
            Player player = (Player) e.getEntity();
            if (plugin.getPlayers().contains(player)) {
                if (e.getDamager() instanceof Player) {
                    Player damager = (Player) e.getDamager();
                    if (plugin.getPlayers().contains(damager)) {
                        player.setHealth(20);
                        return;
                    }
                }
            }

        }

        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerRespawn(PlayerRespawnEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof KingOfTheHillState))
            return;

        KingOfTheHillState state = (KingOfTheHillState) plugin.getGameStateManager().getCurrentGameState();
        Location location = plugin.getLocationManager().getRespawnLocation(plugin.getLocationManager().KINGOFTHEHILL);
        e.setRespawnLocation(location);

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().teleport(location);
                e.getPlayer().setFireTicks(0);
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
}
