package de.groodian.minecraftparty.stats;

import de.groodian.hyperiorcore.boards.HTitle;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.user.CoinSystem;
import de.groodian.hyperiorcore.user.MinecraftPartyStats;
import de.groodian.hyperiorcore.user.XP;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Stats {

    private final Main plugin;
    private final Map<Player, MinecraftPartyStats.Player> stats;
    private final Map<Player, MinecraftPartyStats.PlayerFinishedGame> currentGame;

    public Stats(Main plugin) {
        this.plugin = plugin;
        this.stats = Collections.synchronizedMap(new HashMap<>());
        this.currentGame = new HashMap<>();
    }

    public MinecraftPartyStats.Player login(Player player) {
        MinecraftPartyStats.Player statsPlayer = MinecraftPartyStats.loadPlayer(HyperiorCore.getPaper().getDatabaseManager(),
                player.getUniqueId());
        stats.put(player, statsPlayer);
        return statsPlayer;
    }

    public MinecraftPartyStats.Player get(Player player) {
        return stats.get(player);
    }

    public void record(Player player, String game, int record, boolean mustBeHigher) {
        MinecraftPartyStats.Player playerStats = stats.get(player);
        boolean updateRecord = false;
        if (playerStats != null) {
            boolean found = false;
            for (MinecraftPartyStats.Record currentRecord : playerStats.records()) {
                if (game.equals(currentRecord.name())) {
                    if (mustBeHigher) {
                        if (record > currentRecord.record()) {
                            updateRecord = true;
                        }
                    } else {
                        if (record < currentRecord.record()) {
                            updateRecord = true;
                        }
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                updateRecord = true;
            }

        } else {
            updateRecord = true;
        }

        if (updateRecord) {
            getStats(player).records.add(
                    new MinecraftPartyStats.PlayerFinishedGameRecord(game, record, OffsetDateTime.now(), mustBeHigher));
            player.sendMessage(Main.PREFIX.append(Messages.get("new-record-message")));
            new HSound(Sound.ENTITY_WITHER_DEATH).playFor(player);
            new HTitle(Duration.ofMillis(250), Duration.ofMillis(2000), Duration.ofMillis(250), Messages.get("new-record-title"),
                    Component.empty()).sendTo(player);
        }
    }

    public void playTime(Player player) {
        getStats(player).playtime = plugin.getPlayTime();
        addPoints(player, plugin.getPlayTime() / 3); // 20 points per minute
    }

    public void miniGamePlayed() {
        for (Player player : plugin.getPlayers()) {
            MinecraftPartyStats.PlayerFinishedGame playerFinishedGame = getStats(player);
            playerFinishedGame.miniGamesPlayed += 1;
            addPoints(player, 15);
        }
    }

    public void gamePlayed() {
        for (Player player : plugin.getPlayers()) {
            getStats(player).gameEnded = false;
            addPoints(player, 30);
        }
    }

    public void gameEnded() {
        for (Player player : plugin.getPlayers()) {
            getStats(player).gameEnded = true;
            getStats(player).playtime = plugin.getPlayTime();
            addPoints(player, (plugin.getPlayTime() / 3) + 150); // 20 points per minute + 150 for game ended
        }
    }

    public void gameFinished(Map<Player, Integer> winners) {
        for (Map.Entry<Player, Integer> winner : winners.entrySet()) {
            Player player = winner.getKey();
            getStats(player).winnerPlace = winner.getValue();
            switch (winner.getValue()) {
                case 1 -> addPoints(player, 500);
                case 2 -> addPoints(player, 400);
                case 3 -> addPoints(player, 350);
                case 4 -> addPoints(player, 300);
                case 5 -> addPoints(player, 250);
            }
        }
    }

    public void miniGameFinished(Map<Player, Integer> winners) {
        for (Map.Entry<Player, Integer> winner : winners.entrySet()) {
            Player player = winner.getKey();
            MinecraftPartyStats.PlayerFinishedGame playerFinishedGame = getStats(player);
            switch (winner.getValue()) {
                case 1 -> {
                    playerFinishedGame.miniGamesFirst += 1;
                    addPoints(player, 50);
                }
                case 2 -> {
                    playerFinishedGame.miniGamesSecond += 1;
                    addPoints(player, 40);
                }
                case 3 -> {
                    playerFinishedGame.miniGamesThird += 1;
                    addPoints(player, 35);
                }
                case 4 -> {
                    playerFinishedGame.miniGamesFourth += 1;
                    addPoints(player, 30);
                }
                case 5 -> {
                    playerFinishedGame.miniGamesFifth += 1;
                    addPoints(player, 25);
                }
            }
        }
    }

    public void finish() {
        for (Map.Entry<Player, MinecraftPartyStats.PlayerFinishedGame> current : currentGame.entrySet()) {
            HyperiorCore.getPaper().getDatabaseManager().transaction(
                    List.of(new MinecraftPartyStats(current.getValue()),
                            new CoinSystem.Add(true, current.getValue().points, current.getKey()),
                            new XP(current.getValue().points, current.getKey())),
                    success -> {
                        if (success) {
                            current.getKey().sendMessage(
                                    Messages.getWithReplace("points-summary",
                                            Map.of("%points%", String.valueOf(current.getValue().points))));
                        }
                    }
            );
        }
    }

    private MinecraftPartyStats.PlayerFinishedGame getStats(Player player) {
        if (currentGame.containsKey(player)) {
            return currentGame.get(player);
        } else {
            MinecraftPartyStats.PlayerFinishedGame stats = new MinecraftPartyStats.PlayerFinishedGame(player.getUniqueId());
            currentGame.put(player, stats);
            return stats;
        }
    }

    private void addPoints(Player player, int points) {
        MinecraftPartyStats.PlayerFinishedGame playerFinishedGame = getStats(player);
        playerFinishedGame.points += points;
    }

}
