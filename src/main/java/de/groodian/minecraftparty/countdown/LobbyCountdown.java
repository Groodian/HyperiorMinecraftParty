package de.groodian.minecraftparty.countdown;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestate.LobbyState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LobbyCountdown extends Countdown {

    private static final int IDLE_TIME = 15, COUNTDOWN_TIME = 60;

    private final Main plugin;
    private final LobbyState lobbyState;
    private int seconds;
    private int idleID;
    private boolean isIdling;
    private boolean isRunning;

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
                case 60, 30, 20, 10, 5, 3, 2 -> {
                    plugin.getServer().broadcast(Main.PREFIX.append(Messages.getWithReplace("game-start-in", Map.of("%seconds%",
                            String.valueOf(seconds)))));
                    new HSound(Sound.BLOCK_NOTE_BLOCK_HAT).play();
                }
                case 1 -> {
                    plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("game-start-in-last-second")));
                    new HSound(Sound.BLOCK_NOTE_BLOCK_HAT).play();
                }
                case 0 -> {
                    plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("game-started")));
                    new HSound(Sound.ENTITY_PLAYER_LEVELUP).play();
                    for (Player player : plugin.getPlayers()) {
                        player.setExp(0);
                    }
                    plugin.getStats().gamePlayed();
                    plugin.setStartTime(System.currentTimeMillis());
                    plugin.getGameStateManager().setRandomGameState();
                }
                default -> {
                }
            }

            seconds--;
        }, 0, 20);
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
                plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("game-start-one-player-needed")));
            } else {
                plugin.getServer()
                        .broadcast(Main.PREFIX.append(Messages.getWithReplace("game-start-player-needed", Map.of("%players-needed%",
                                String.valueOf(playersNeed)))));
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
