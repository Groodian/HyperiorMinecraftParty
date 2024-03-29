package de.groodian.minecraftparty.listener;

import de.groodian.minecraftparty.gamestate.minigame.BreakoutState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BreakoutListener implements Listener {

    private final Main plugin;

    public BreakoutListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof BreakoutState))
            return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerBreak(BlockBreakEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof BreakoutState state))
            return;

        if (state.isStarted()) {
            if (plugin.getPlayers().contains(e.getPlayer())) {
                int xBlock = (int) e.getBlock().getLocation().getX();
                int yBlock = (int) e.getBlock().getLocation().getY();
                int zBlock = (int) e.getBlock().getLocation().getZ();

                for (Location location : plugin.getLocationManager().BREAKOUT_PLAYERS) {
                    if (xBlock == location.getBlockX() && zBlock == location.getBlockZ() && yBlock <= location.getBlockY() &&
                        yBlock >= location.getBlockY() - (MainConfig.getInt("Breakout.depth") + 1)) {
                        e.getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        e.setCancelled(true);

    }

}
