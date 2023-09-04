package de.groodian.minecraftparty.listener;

import de.groodian.hyperiorcore.boards.HScoreboard;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.user.User;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.countdown.LobbyCountdown;
import de.groodian.minecraftparty.gamestate.LobbyState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.stats.Top10;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

public class LobbyListener implements Listener {

    private final Main plugin;

    public LobbyListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePlayerLogin(PlayerLoginEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState))
            return;

        if (plugin.getPlayers().size() >= Main.MAX_PLAYERS) {
            Player player = e.getPlayer();
            User user = HyperiorCore.getPaper().getUserManager().get(player.getUniqueId());
            if (user.has("minecraftparty.premiumjoin")) {
                for (Player currentPlayer : plugin.getPlayers()) {
                    User currentUser = HyperiorCore.getPaper().getUserManager().get(currentPlayer.getUniqueId());
                    if (!currentUser.has("minecraftparty.premiumjoin")) {
                        currentPlayer.sendMessage(Main.PREFIX.append(Messages.get("premium-player-kicked-you")));
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(b);
                        try {
                            out.writeUTF("Connect");
                            out.writeUTF(MainConfig.getString("fallback-server"));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        currentPlayer.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                        e.allow();
                        return;
                    }
                }
                e.disallow(PlayerLoginEvent.Result.KICK_FULL, Main.PREFIX.append(Messages.get("all-player-has-premium")));
            } else {
                e.disallow(PlayerLoginEvent.Result.KICK_FULL, Main.PREFIX.append(Messages.get("you-need-premium")));
            }
        } else {
            e.allow();
        }
    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState lobbyState))
            return;
        Player player = e.getPlayer();

        long currentTime = System.currentTimeMillis();
        if (currentTime - plugin.getLastJoin() > Main.TOP10_RELOAD) {
            plugin.setLastJoin(currentTime);
            Top10 top10 = new Top10(plugin);
            top10.set();
        }

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
        ItemStack statsItem = new ItemBuilder(Material.PLAYER_HEAD).setName(Messages.get("stats-item-name"))
                .setSkullOwner(player.getUniqueId())
                .addCustomData("interact", PersistentDataType.STRING, "Stats")
                .build();
        player.getInventory().setItem(0, statsItem);
        e.joinMessage(Main.PREFIX.append(Messages.getWithReplaceComp("join-message",
                Map.of("%player%", player.displayName(),
                        "%current-players%", Component.text(Math.min(plugin.getPlayers().size(), Main.MAX_PLAYERS)),
                        "%max-players%", Component.text(Main.MAX_PLAYERS)))));
        player.teleport(plugin.getLocationManager().LOBBY);

        LobbyCountdown countdown = lobbyState.getCountdown();
        if (plugin.getPlayers().size() >= Main.MIN_PLAYERS) {
            if (!countdown.isRunning()) {
                countdown.stopIdle();
                countdown.start();
            }
        }

        HScoreboard sb = HyperiorCore.getPaper().getScoreboard();
        if (MainConfig.getBoolean("Scoreboard.animation")) {
            sb.registerScoreboard(player, Messages.get("Scoreboard.title"), 5, MainConfig.getInt("Scoreboard.delay"),
                    MainConfig.getInt("Scoreboard.delay-between-animation"));
        } else {
            sb.registerScoreboard(player, Messages.get("Scoreboard.title"), 5);
        }

        lobbyState.updateScoreboard();

        plugin.getClient().sendUpdate();
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState lobbyState))
            return;
        Player player = e.getPlayer();
        plugin.getPlayers().remove(player);
        e.quitMessage(Main.PREFIX.append(Messages.getWithReplaceComp("quit-message",
                Map.of("%player%", player.displayName(),
                        "%current-players%", Component.text(plugin.getPlayers().size()),
                        "%max-players%", Component.text(Main.MAX_PLAYERS)))));

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
