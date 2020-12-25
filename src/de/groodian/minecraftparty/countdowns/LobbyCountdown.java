package de.groodian.minecraftparty.countdowns;

import de.groodian.minecraftparty.gamestates.LobbyState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LobbyCountdown extends Countdown {

    private Main plugin;

    private LobbyState lobbyState;

    private int seconds;
    private int idleID;
    private boolean isIdling;
    private boolean isRunning;

    private static final int IDLE_TIME = 15, COUNTDOWN_TIME = 60;

    public LobbyCountdown(Main plugin, LobbyState lobbyState) {
        this.plugin = plugin;
        this.lobbyState = lobbyState;
        seconds = COUNTDOWN_TIME;
    }

    @Override
    public void start() {
        isRunning = true;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(seconds);
                player.setExp((1.0f / COUNTDOWN_TIME) * (COUNTDOWN_TIME - seconds));
            }
            lobbyState.updateScoreboard();
            switch (seconds) {
                case 60:
                case 30:
                case 20:
                case 10:
                case 5:
                case 3:
                case 2:
                    Bukkit.broadcastMessage(Main.PREFIX + Messages.get("game-start-in").replace("%seconds%", seconds + ""));
                    playSound(Sound.NOTE_STICKS);
                    break;
                case 1:
                    Bukkit.broadcastMessage(Main.PREFIX + Messages.get("game-start-in-last-second"));
                    playSound(Sound.NOTE_STICKS);
                    break;
                case 0:
                    Bukkit.broadcastMessage(Main.PREFIX + Messages.get("game-started"));
                    playSound(Sound.LEVEL_UP);
                    for (Player player : plugin.getPlayers()) {
                        player.setExp(0);
                    }
                    plugin.getStats().gamePlayed();
                    plugin.setStartTime(System.currentTimeMillis());
                    plugin.getGameStateManager().setRandomGameState();
                    break;
                default:
                    break;
            }
            seconds--;
        }, 0, 20);
    }

    private void playSound(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            Bukkit.getScheduler().cancelTask(taskID);
            isRunning = false;
            seconds = COUNTDOWN_TIME;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(0);
                player.setExp(0);
            }
        }
    }

    public void startIdle() {
        isIdling = true;
        idleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int playersNeed = (Main.MIN_PLAYERS - plugin.getPlayers().size());
            if (playersNeed == 1) {
                Bukkit.broadcastMessage(Main.PREFIX + Messages.get("game-start-one-player-needed"));
            } else {
                Bukkit.broadcastMessage(Main.PREFIX + Messages.get("game-start-player-needed").replace("%players-needed%", playersNeed + ""));
            }
        }, 0, IDLE_TIME * 20);
    }

    public void stopIdle() {
        if (isIdling) {
            Bukkit.getScheduler().cancelTask(idleID);
            isIdling = false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isIdling() {
        return isIdling;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

}
