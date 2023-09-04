package de.groodian.minecraftparty.gamestate;

import de.groodian.hyperiorcore.boards.HScoreboard;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcosmetics.HyperiorCosmetic;
import de.groodian.minecraftparty.countdown.LobbyCountdown;
import de.groodian.minecraftparty.gui.GameOverviewGUI;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyState implements GameState {

    private final Main plugin;
    private final LobbyCountdown countdown;
    private final HScoreboard sb;

    public LobbyState(Main plugin) {
        this.plugin = plugin;
        countdown = new LobbyCountdown(plugin, this);
        sb = HyperiorCore.getPaper().getScoreboard();
    }

    @Override
    public void start() {
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§aLOBBY STATE STARTED!");
        countdown.startIdle();
    }

    @Override
    public void stop() {
        HyperiorCosmetic.disable();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getOpenInventory().close();
            GameOverviewGUI.giveItem(player);
            sb.unregisterScoreboard(player);
        }
        countdown.stop();

        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cLOBBY STATE STOPPED!");
    }

    public LobbyCountdown getCountdown() {
        return countdown;
    }

    public void updateScoreboard() {
        for (Player player : plugin.getPlayers()) {
            sb.updateLine(0, player, Component.text(""));
            sb.updateLine(1, player, Messages.getWithReplace("Scoreboard.lobby.players",
                    Map.of("%players%", String.valueOf(plugin.getPlayers().size()), "%max-players%", String.valueOf(Main.MAX_PLAYERS))));
            sb.updateLine(2, player, Component.text(" "));
            if (countdown.isIdling()) {
                int playersNeed = (Main.MIN_PLAYERS - plugin.getPlayers().size());
                if (playersNeed == 1) {
                    sb.updateLine(3, player, Messages.getWithReplace("Scoreboard.lobby.waiting-for-player-line-1",
                            Map.of("%players-needed%", String.valueOf(playersNeed))));
                } else {
                    sb.updateLine(3, player, Messages.getWithReplace("Scoreboard.lobby.waiting-for-players-line-1",
                            Map.of("%players-needed%", String.valueOf(playersNeed))));
                }
                sb.updateLine(4, player, Messages.getWithReplace("Scoreboard.lobby.waiting-for-players-line-2",
                        Map.of("%players-needed%", String.valueOf(playersNeed))));
            } else {
                sb.updateLine(3, player, Messages.getWithReplace("Scoreboard.lobby.starting-in-line-1", Map.of("%seconds%",
                        String.valueOf(countdown.getSeconds()))));
                sb.updateLine(4, player, Messages.getWithReplace("Scoreboard.lobby.starting-in-line-2", Map.of("%seconds%",
                        String.valueOf(countdown.getSeconds()))));
            }
        }
    }

}
