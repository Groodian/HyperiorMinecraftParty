package de.groodian.minecraftparty.listener;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.gamestate.minigame.ColorBattleState;
import de.groodian.minecraftparty.main.Main;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ColorBattleListener implements Listener {

    private final Main plugin;

    private final List<Player> delay;

    public ColorBattleListener(Main plugin) {
        this.plugin = plugin;
        this.delay = new ArrayList<>();
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof ColorBattleState state))
            return;

        if (state.isStarted()) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
                String interact = ItemBuilder.getCustomData(itemInMainHand, "interact", PersistentDataType.STRING);
                if (interact != null) {
                    if (interact.equals("ColorBattleGun")) {
                        if (!delay.contains(e.getPlayer())) {
                            e.getPlayer().launchProjectile(Snowball.class);
                            new HSound(Sound.ENTITY_GENERIC_EXPLODE, 0.1f, 3.0f).playFor(e.getPlayer());
                            delay.add(e.getPlayer());
                            new BukkitRunnable() {
                                public void run() {
                                    delay.remove(e.getPlayer());
                                }
                            }.runTaskLater(plugin, 5L);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void handleProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof ColorBattleState state))
            return;

        if (state.isStarted()) {
            if (e.getEntity().getShooter() != null) {
                if (e.getEntity().getShooter() instanceof Player) {
                    if (e.getEntity() instanceof Snowball) {
                        state.getSnowballs().add((Snowball) e.getEntity());
                    }
                }
            }
        }

    }

    @EventHandler
    public void handleProjectileHit(ProjectileHitEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof ColorBattleState state))
            return;

        if (state.isStarted()) {
            if (e.getEntity().getShooter() != null) {
                if (e.getEntity().getShooter() instanceof Player player) {
                    if (e.getEntity() instanceof Snowball) {
                        for (Block block : getNearbyBlocks(e.getEntity().getLocation())) {
                            if (Tag.TERRACOTTA.isTagged(block.getType())) {

                                Material previousMaterial = block.getType();
                                block.setType(state.getColors().get(player));
                                state.getRanking().put(player, state.getRanking().get(player) + 1);

                                if (previousMaterial != Material.TERRACOTTA) {
                                    for (Map.Entry<Player, Material> current : state.getColors().entrySet()) {
                                        if (current.getValue() == previousMaterial) {
                                            Player temp = current.getKey();
                                            state.getRanking().put(temp, state.getRanking().get(temp) - 1);
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    private List<Block> getNearbyBlocks(Location loc) {
        List<Block> blocks = new ArrayList<>();

        blocks.add(loc.getBlock());

        blocks.add(loc.clone().add(1, 0, 0).getBlock());
        blocks.add(loc.clone().add(2, 0, 0).getBlock());
        blocks.add(loc.clone().add(-1, 0, 0).getBlock());
        blocks.add(loc.clone().add(-2, 0, 0).getBlock());

        blocks.add(loc.clone().add(0, 1, 0).getBlock());
        blocks.add(loc.clone().add(0, 2, 0).getBlock());
        blocks.add(loc.clone().add(0, -1, 0).getBlock());
        blocks.add(loc.clone().add(0, -2, 0).getBlock());

        blocks.add(loc.clone().add(0, 0, 1).getBlock());
        blocks.add(loc.clone().add(0, 0, 2).getBlock());
        blocks.add(loc.clone().add(0, 0, -1).getBlock());
        blocks.add(loc.clone().add(0, 0, -2).getBlock());

        blocks.add(loc.clone().add(1, 1, 0).getBlock());
        blocks.add(loc.clone().add(-1, 1, 0).getBlock());
        blocks.add(loc.clone().add(1, -1, 0).getBlock());
        blocks.add(loc.clone().add(-1, -1, 0).getBlock());

        blocks.add(loc.clone().add(0, 1, 1).getBlock());
        blocks.add(loc.clone().add(0, 1, -1).getBlock());
        blocks.add(loc.clone().add(0, -1, 1).getBlock());
        blocks.add(loc.clone().add(0, -1, -1).getBlock());

        blocks.add(loc.clone().add(1, 0, 1).getBlock());
        blocks.add(loc.clone().add(1, 0, -1).getBlock());
        blocks.add(loc.clone().add(-1, 0, 1).getBlock());
        blocks.add(loc.clone().add(-1, 0, -1).getBlock());

        return blocks;
    }

}
