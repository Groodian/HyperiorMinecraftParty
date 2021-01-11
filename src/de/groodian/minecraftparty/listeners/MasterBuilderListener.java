package de.groodian.minecraftparty.listeners;

import de.groodian.minecraftparty.gamestates.MasterBuildersState;
import de.groodian.minecraftparty.main.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MasterBuilderListener implements Listener {

    private Main plugin;

    public MasterBuilderListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePlayerInventoryClick(InventoryClickEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState))
            return;
        if (((MasterBuildersState) plugin.getGameStateManager().getCurrentGameState()).isInBuildMode())
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState))
            return;
        if (((MasterBuildersState) plugin.getGameStateManager().getCurrentGameState()).isInBuildMode())
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerBreak(BlockBreakEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState))
            return;
        e.setCancelled(handlePictureModifikation(e.getPlayer(), e.getBlock()));
    }

    @EventHandler
    public void handlePlayerPlace(BlockPlaceEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState))
            return;
        e.setCancelled(handlePictureModifikation(e.getPlayer(), e.getBlock()));
    }

    private boolean handlePictureModifikation(Player player, Block block) {
        MasterBuildersState state = (MasterBuildersState) plugin.getGameStateManager().getCurrentGameState();

        if (state.isInBuildMode()) {
            if (plugin.getPlayers().contains(player)) {
                if (state.isBlockInReplicaPicture(player, block)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            state.pictureModified(player);
                        }
                    }.runTaskLater(plugin, 1);
                    return false;
                }
            }
        }

        return true;
    }

}
