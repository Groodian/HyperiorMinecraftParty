package de.groodian.minecraftparty.listener;

import de.groodian.hyperiorcore.boards.Tablist;
import de.groodian.hyperiorcore.guis.MinecraftPartyStatsGUI;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.user.MinecraftPartyStats;
import de.groodian.hyperiorcore.user.User;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.minecraftparty.gamestate.EndingState;
import de.groodian.minecraftparty.gamestate.LobbyState;
import de.groodian.minecraftparty.gamestate.minigame.BreakoutState;
import de.groodian.minecraftparty.gamestate.minigame.GunGameState;
import de.groodian.minecraftparty.gamestate.minigame.KingOfTheHillState;
import de.groodian.minecraftparty.gamestate.minigame.MasterBuildersState;
import de.groodian.minecraftparty.gui.GameOverviewGUI;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MainListener implements Listener {

    private final Main plugin;

    public MainListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleWorldLoadEvent(WorldLoadEvent e) {
        if (e.getWorld().getName().equals("world")) {
            plugin.afterWorldLoad();
        }
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
            e.quitMessage(Main.PREFIX.append(Messages.getWithReplaceComp("quit-message",
                    Map.of("%player%", player.displayName(),
                            "%current-players%", Component.text(plugin.getPlayers().size()),
                            "%max-players%", Component.text(Main.MAX_PLAYERS)))));
        } else {
            e.quitMessage(null);
        }

        plugin.getToRemove().add(player);
        plugin.getClient().sendUpdate();

        if (plugin.getPlayers().size() == 0) {
            plugin.stopServer();
        }

    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        new Task(plugin) {
            @Override
            public void executeAsync() {
                cache.add(plugin.getStats().login(player));
            }

            @Override
            public void executeSyncOnFinish() {
                String rank = "-";
                String points = "-";
                String wins = "-";
                if (cache.get(0) != null) {
                    MinecraftPartyStats.Player statsPlayer = (MinecraftPartyStats.Player) cache.get(0);
                    rank = String.valueOf(statsPlayer.rank());
                    points = String.valueOf(statsPlayer.points());
                    wins = String.valueOf(statsPlayer.gamesFirst());
                }

                new Tablist(Messages.getWithReplace("tablist-header", Map.of("%server-number%", String.valueOf(plugin.getGroupNumber()))),
                        Messages.getWithReplace("tablist-footer", Map.of(
                                "%rank%", rank,
                                "%points%", points,
                                "%wins%", wins))).sendTo(player);
            }
        };

        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState)
            return;
        e.joinMessage(null);

        HyperiorCore.getPaper().getPrefix().addSpectator(player);
        HyperiorCore.getPaper().getPrefix().setPrefix(player);
        HyperiorCore.getPaper().getPrefix().setListName(player);
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
            current.hidePlayer(plugin, player);
        }

        for (Player current : plugin.getPlayers()) {
            player.teleport(current);
            break;
        }

        GameOverviewGUI.giveItem(player);

        if (MainConfig.getBoolean("Scoreboard.animation")) {
            HyperiorCore.getPaper().getScoreboard()
                    .registerScoreboard(player, Messages.get("Scoreboard.title"), 15, MainConfig.getInt("Scoreboard.delay"),
                            MainConfig.getInt("Scoreboard.delay-between-animation"));
        } else {
            HyperiorCore.getPaper().getScoreboard().registerScoreboard(player, Messages.get("Scoreboard.title"), 15);
        }

        plugin.getClient().sendUpdate();
    }

    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        e.deathMessage(null);
        e.getDrops().clear();
        Player player = e.getEntity();
        player.spigot().respawn();
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
    public void handlePlayerItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;
        if (plugin.getBuild().contains(player))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player))
            return;
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
        if (e.getEntity() instanceof Player player) {
            if (plugin.getBuild().contains(player))
                return;
        }
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof GunGameState)
            return;
        if (plugin.getGameStateManager().getCurrentGameState() instanceof KingOfTheHillState)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
            String interact = ItemBuilder.getCustomData(itemInMainHand, "interact", PersistentDataType.STRING);
            if (interact != null) {
                if (interact.equals("Stats")) {
                    User user = HyperiorCore.getPaper().getUserManager().get(e.getPlayer().getUniqueId());
                    MinecraftPartyStats.Player stats = plugin.getStats().get(e.getPlayer());
                    if (user != null && stats != null) {
                        HyperiorCore.getPaper().getDefaultGUIManager().open(e.getPlayer(), new MinecraftPartyStatsGUI(user, stats));
                    } else {
                        e.getPlayer().sendMessage(Main.PREFIX.append(
                                Component.text("Du hast noch nie Minecraft Party gespielt.", NamedTextColor.RED)));
                    }
                } else if (interact.equals("Overview")) {
                    plugin.getGameOverviewGUIManager().open(e.getPlayer(), new GameOverviewGUI(plugin));
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
