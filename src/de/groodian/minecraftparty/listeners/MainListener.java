package de.groodian.minecraftparty.listeners;

import de.groodian.hyperiorcore.boards.Tablist;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.minecraftparty.commands.StatsGUI;
import de.groodian.minecraftparty.gamestates.BreakoutState;
import de.groodian.minecraftparty.gamestates.EndingState;
import de.groodian.minecraftparty.gamestates.GunGameState;
import de.groodian.minecraftparty.gamestates.LobbyState;
import de.groodian.minecraftparty.gamestates.MasterBuildersState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MainListener implements Listener {

    private Main plugin;

    public MainListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState)
            return;
        Player player = e.getPlayer();

        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof EndingState)) {
            if (plugin.getPlayers().contains(player)) {
                plugin.getStats().playTime(player);
            }
        }

        if (plugin.getPlayers().contains(player)) {
            plugin.getPlayers().remove(player);
            e.setQuitMessage(Main.PREFIX + Messages.get("quit-message").replace("%player%", player.getDisplayName()).replace("%current-players%", plugin.getPlayers().size() + "").replace("%max-players%", Main.MAX_PLAYERS + ""));
        } else {
            e.setQuitMessage(null);
        }

        plugin.getToRemove().add(player);
        plugin.getClient().sendUpdate();

        if (plugin.getPlayers().size() == 0) {
            plugin.stopServer();
        }

    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState)
            return;
        e.setJoinMessage(null);
        final Player player = e.getPlayer();

        new Task(plugin) {
            @Override
            public void executeAsync() {
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
                cache.add(rank);
                cache.add(points);
                cache.add(wins);
            }

            @Override
            public void executeSyncOnFinish() {
                new Tablist(Messages.get("tablist-header").replace("%server-number%", Bukkit.getServerName() + ""), Messages.get("tablist-footer").replace("%rank%", (String) cache.get(0)).replace("%points%", (String) cache.get(1)).replace("%wins%", (String) cache.get(2))).sendTo(player);
            }
        };

        HyperiorCore.getPrefix().addSpectator(player);
        HyperiorCore.getPrefix().setPrefix(player);
        HyperiorCore.getPrefix().setListName(player);
        player.setExp(0);
        player.setLevel(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));

        for (Player current : plugin.getPlayers()) {
            current.hidePlayer(player);
        }

        for (Player current : plugin.getPlayers()) {
            player.teleport(current);
            break;
        }

        plugin.getGameOverview().giveItem(player);

        if (MainConfig.getBoolean("Scoreboard.animation")) {
            HyperiorCore.getSB().registerScoreboard(player, Messages.get("Scoreboard.title"), 15, MainConfig.getInt("Scoreboard.delay"), MainConfig.getInt("Scoreboard.delay-between-animation"));
        } else {
            HyperiorCore.getSB().registerScoreboard(player, Messages.get("Scoreboard.title"), 15);
        }

        plugin.getClient().sendUpdate();
    }

    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        Player player = e.getEntity();
        PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
        ((CraftPlayer) player).getHandle().playerConnection.a(packet);
    }

    @EventHandler
    public void handleCreatureSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handleBedEnter(PlayerBedEnterEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerBuild(BlockPlaceEvent e) {
        if (plugin.getBuild().contains(e.getPlayer()))
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerBreak(BlockBreakEvent e) {
        if (plugin.getBuild().contains(e.getPlayer()))
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof BreakoutState)
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerDropItem(PlayerDropItemEvent e) {
        if (plugin.getBuild().contains(e.getPlayer()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerItemPickup(PlayerPickupItemEvent e) {
        if (plugin.getBuild().contains(e.getPlayer()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) e.getWhoClicked();

        if (!plugin.getPlayers().contains(player)) {
            if (e.getClickedInventory() != null) {
                if (e.getClickedInventory().getTitle() != null) {
                    if (e.getClickedInventory().getTitle().equals(Messages.get("player-overview-inventory-name"))) {
                        e.setCancelled(true);
                        if (e.getCurrentItem().getItemMeta() != null) {
                            SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
                            Player target = Bukkit.getPlayerExact(meta.getOwner());
                            if (target != null) {
                                player.teleport(target);
                            }
                        }
                    }
                }
            }
        }

        if (plugin.getBuild().contains(player))
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handleBowShot(EntityShootBowEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        if (e.getEntity() instanceof Player) {
            if (plugin.getBuild().contains(e.getEntity()))
                return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getItemInHand() != null) {
                if (e.getPlayer().getItemInHand().getItemMeta() != null) {
                    if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null) {
                        if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(Messages.get("stats-item-name"))) {
                            new StatsGUI(plugin).open(e.getPlayer(), e.getPlayer().getName());
                        } else if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(Messages.get("player-overview-item-name"))) {
                            plugin.getGameOverview().open(e.getPlayer());
                        }
                    }
                }
            }
        }
        if (plugin.getBuild().contains(e.getPlayer()))
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof BreakoutState)
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof MasterBuildersState)
            return;
        e.setCancelled(true);
    }

}
