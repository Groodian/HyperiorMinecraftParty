package de.groodian.minecraftparty.gamestates;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.minecraftparty.countdowns.LobbyCountdown;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyState implements GameState {

    private Main plugin;

    private LobbyCountdown countdown;

    public LobbyState(Main plugin) {
        this.plugin = plugin;
        countdown = new LobbyCountdown(plugin, this);
    }

    @Override
    public void start() {
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§aLOBBY STATE STARTED!");
        countdown.startIdle();
    }

    @Override
    public void stop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            plugin.getGameOverview().giveItem(player);
            HyperiorCore.getSB().unregisterScoreboard(player);
        }
        countdown.stop();

        de.groodian.cosmetics.main.Main.setIsEnabled(false);

        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cLOBBY STATE STOPPED!");
    }

    public LobbyCountdown getCountdown() {
        return countdown;
    }

    public void updateScoreboard() {
        for (Player player : plugin.getPlayers()) {
            HyperiorCore.getSB().updateLine(0, player, "");
            HyperiorCore.getSB().updateLine(1, player, Messages.get("Scoreboard.lobby.players").replace("%players%", plugin.getPlayers().size() + "").replace("%max-players%", Main.MAX_PLAYERS + ""));
            HyperiorCore.getSB().updateLine(2, player, "");
            if (countdown.isIdling()) {
                int playersNeed = (Main.MIN_PLAYERS - plugin.getPlayers().size());
                if (playersNeed == 1) {
                    HyperiorCore.getSB().updateLine(3, player, Messages.get("Scoreboard.lobby.waiting-for-player-line-1").replace("%players-needed%", playersNeed + ""));
                } else {
                    HyperiorCore.getSB().updateLine(3, player, Messages.get("Scoreboard.lobby.waiting-for-players-line-1").replace("%players-needed%", playersNeed + ""));
                }
                HyperiorCore.getSB().updateLine(4, player, Messages.get("Scoreboard.lobby.waiting-for-players-line-2").replace("%players-needed%", playersNeed + ""));
            } else {
                HyperiorCore.getSB().updateLine(3, player, Messages.get("Scoreboard.lobby.starting-in-line-1").replace("%seconds%", countdown.getSeconds() + ""));
                HyperiorCore.getSB().updateLine(4, player, Messages.get("Scoreboard.lobby.starting-in-line-2").replace("%seconds%", countdown.getSeconds() + ""));
            }
        }
    }

}
