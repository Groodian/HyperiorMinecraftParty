package de.groodian.minecraftparty.listener;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestate.minigame.ColorBattleState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColorBattleListener implements Listener {

    private Main plugin;

    private List<Player> delay;

    public ColorBattleListener(Main plugin) {
        this.plugin = plugin;
        this.delay = new ArrayList<>();
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof ColorBattleState))
            return;

        ColorBattleState state = (ColorBattleState) plugin.getGameStateManager().getCurrentGameState();

        if (state.isStarted()) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getPlayer().getItemInHand() != null) {
                    if (e.getPlayer().getItemInHand().getItemMeta() != null) {
                        if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null) {
                            if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(Messages.get("ColorBattle.color-gun-name"))) {
                                if (!delay.contains(e.getPlayer())) {
                                    e.getPlayer().launchProjectile(Snowball.class);
                                    new HSound(Sound.EXPLODE, 0.1f, 3.0f).playFor(e.getPlayer());
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
        }

    }

    @EventHandler
    public void handleProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof ColorBattleState))
            return;

        ColorBattleState state = (ColorBattleState) plugin.getGameStateManager().getCurrentGameState();

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
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof ColorBattleState))
            return;

        ColorBattleState state = (ColorBattleState) plugin.getGameStateManager().getCurrentGameState();

        if (state.isStarted()) {
            if (e.getEntity().getShooter() != null) {
                if (e.getEntity().getShooter() instanceof Player) {
                    Player player = (Player) e.getEntity().getShooter();
                    if (e.getEntity() instanceof Snowball) {
                        for (Block block : getNearbyBlocks(e.getEntity().getLocation())) {
                            if (block.getType() == Material.STAINED_CLAY) {

                                byte data = block.getData();
                                block.setData(state.getColors().get(player));
                                state.getRanking().put(player, state.getRanking().get(player) + 1);

                                if (data != 0) {
                                    for (Map.Entry<Player, Byte> current : state.getColors().entrySet()) {
                                        if (current.getValue() == data) {
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
