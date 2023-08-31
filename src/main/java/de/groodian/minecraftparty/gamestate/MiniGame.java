package de.groodian.minecraftparty.gamestate;

import de.groodian.hyperiorcore.boards.HScoreboard;
import de.groodian.hyperiorcore.boards.HTitle;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.user.MinecraftPartyStats;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.minecraftparty.gui.GameOverviewGUI;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.util.TeleportManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class MiniGame implements GameState {

    private static boolean firstMiniGame = true;

    // settings
    protected boolean timeRecords;
    protected boolean timeScoreboardGame;
    protected boolean lowerIsBetterRecords;
    protected boolean lowerIsBetterGame;
    protected boolean setRecords;
    protected boolean setRecordsInAddWinner;

    // protected stuff
    protected String name;
    protected Main plugin;
    protected String mapName;
    protected int secondsGame;
    protected long startTime;
    protected Map<Player, Integer> ranking;
    protected List<Player> winner;
    protected List<Player> diePlayers;
    protected boolean started;
    protected TeleportManager teleportManager;

    // private stuff
    private Map<Player, Integer> records;
    private BukkitTask countdownTask;
    private int secondsCountdown;
    private BukkitTask scoreboardGameTask;
    private final HScoreboard sb;

    public MiniGame(String name, Main plugin) {
        // default settings
        this.timeRecords = false;
        this.timeScoreboardGame = true;
        this.lowerIsBetterRecords = false;
        this.lowerIsBetterGame = false;
        this.setRecords = true;
        this.setRecordsInAddWinner = true;

        this.name = name;
        this.plugin = plugin;
        this.mapName = null;
        this.secondsGame = (MainConfig.getInt(name + ".time") == 0) ? 0 : MainConfig.getInt(name + ".time") + 1;
        this.startTime = 0;
        this.ranking = new HashMap<>();
        this.winner = new ArrayList<>();
        this.diePlayers = new ArrayList<>();
        this.started = false;
        this.teleportManager = new TeleportManager(plugin, this::countdown);

        this.records = new HashMap<>();
        this.countdownTask = null;
        this.secondsCountdown = 6;
        this.scoreboardGameTask = null;
        this.sb = HyperiorCore.getPaper().getScoreboard();
    }

    protected abstract void prepare();

    protected abstract void beforeCountdownStart();

    protected abstract void startMiniGame();

    @Override
    public void start() {
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§a" + name.toUpperCase() + " STATE STARTED!");

        new BukkitRunnable() {
            @Override
            public void run() {

                plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("next-minigame")));
                new HSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play();
                if (MainConfig.getBoolean(name + ".reset")) {
                    prepare();
                }

            }
        }.runTaskLater(plugin, 35);

        new BukkitRunnable() {
            @Override
            public void run() {

                plugin.getServer().broadcast(Messages.get("Minigame.game-info-header"));
                plugin.getServer()
                        .broadcast(Messages.getWithReplace(name + ".game-info", Map.of("%name%",
                                PlainTextComponentSerializer.plainText().serialize(Messages.get(name + ".name")).toUpperCase())));
                plugin.getServer().broadcast(Messages.get("Minigame.game-info-fooder"));
                new HSound(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR).play();

            }
        }.runTaskLater(plugin, 70);

        new BukkitRunnable() {
            @Override
            public void run() {

                new Task(plugin) {
                    @Override
                    public void executeAsync() {
                        loadRecords();
                        records = sortMapByValue(records, lowerIsBetterRecords);
                    }

                    @Override
                    public void executeSyncOnFinish() {
                        plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("Minigame.teleport-player")));
                        beforeCountdownStart();
                        if (firstMiniGame) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (MainConfig.getBoolean("Scoreboard.animation")) {
                                    sb.registerScoreboard(player, Messages.get("Scoreboard.title"), 15,
                                            MainConfig.getInt("Scoreboard.delay"), MainConfig.getInt("Scoreboard.delay-between-animation"));
                                } else {
                                    sb.registerScoreboard(player, Messages.get("Scoreboard.title"), 15);
                                }
                            }
                            firstMiniGame = false;
                        }
                        scoreboardRecords();
                        teleportManager.startTeleporting();
                    }
                };

            }
        }.runTaskLater(plugin, 105);

    }

    private void loadRecords() {
        String recordName = name;
        if (mapName != null) {
            recordName += mapName;
        }

        for (Player all : plugin.getPlayers()) {
            int recordValue = -1;
            for (MinecraftPartyStats.Record record : plugin.getStats().get(all).records()) {
                if (record.name().equals(recordName)) {
                    recordValue = record.record();
                    break;
                }
            }
            records.put(all, recordValue);
        }
    }

    private void scoreboardRecords() {
        for (Player all : Bukkit.getOnlinePlayers()) {

            int scoreboardRow = 5;
            int place = 0;

            sb.updateLine(0, all, Component.text(""));
            sb.updateLine(1, all, Messages.get("Scoreboard.minigame"));
            sb.updateLine(2, all, Messages.getWithReplace("Scoreboard.minigame-name",
                    Map.of("%name%", PlainTextComponentSerializer.plainText().serialize(Messages.get(name + ".name")))));
            sb.updateLine(3, all, Component.text(""));
            sb.updateLine(4, all, Messages.get("Scoreboard.records"));

            for (Map.Entry<Player, Integer> current : records.entrySet()) {

                if (current.getValue() != -1 && scoreboardRow <= 11) {
                    place++;

                    String playerValue;
                    if (timeRecords) {
                        playerValue = formatTime(current.getValue());
                    } else {
                        playerValue = current.getValue().toString();
                    }

                    sb.updateLine(scoreboardRow++, all, LegacyComponentSerializer.legacySection()
                            .deserialize("§a" + place + "# §8(§7" + playerValue + "§8) " + getPlayerColor(all, current.getKey()) +
                                         current.getKey().getName()));

                }

            }

            if (scoreboardRow == 5) {
                sb.updateLine(scoreboardRow++, all, Messages.get("Scoreboard.no-records"));
            }

            while (scoreboardRow <= 11) {
                sb.updateLine(scoreboardRow++, all, Component.text(""));
            }

            if (timeScoreboardGame) {
                sb.updateLine(12, all, Component.text(""));
                sb.updateLine(13, all, Messages.get("Scoreboard.ending-in"));
                sb.updateLine(14, all, Messages.getWithReplace("Scoreboard.time", Map.of("%seconds%", String.valueOf(secondsGame - 1))));
            } else {
                sb.updateLine(12, all, Component.text(""));
                sb.updateLine(13, all, Messages.get("Scoreboard.players-remaining"));
                sb.updateLine(14, all, Messages.getWithReplace("Scoreboard.players",
                        Map.of("%player%", String.valueOf(plugin.getPlayers().size() - diePlayers.size()),
                                "%max-players%", String.valueOf(plugin.getPlayers().size()))));
            }

        }
    }

    private void countdown() {
        countdownTask = new BukkitRunnable() {

            @Override
            public void run() {
                switch (secondsCountdown) {
                    case 5, 4, 3, 2 -> {
                        new HTitle(Duration.ofMillis(0), Duration.ofMillis(1500), Duration.ofMillis(0),
                                Messages.getWithReplace("Minigame.start-title",
                                        Map.of("%name%", PlainTextComponentSerializer.plainText().serialize(Messages.get(name + ".name")))),
                                Messages.getWithReplace("Minigame.start-subtitle",
                                        Map.of("%seconds%", String.valueOf(secondsCountdown)))).send();
                        new HSound(Sound.BLOCK_NOTE_BLOCK_HAT).play();
                    }
                    case 1 -> {
                        new HTitle(Duration.ofMillis(0), Duration.ofMillis(1500), Duration.ofMillis(0),
                                Messages.getWithReplace("Minigame.start-title-last-second",
                                        Map.of("%name%", PlainTextComponentSerializer.plainText().serialize(Messages.get(name + ".name")))),
                                Messages.get("Minigame.start-subtitle-last-second")).send();
                        new HSound(Sound.BLOCK_NOTE_BLOCK_HAT).play();
                    }
                    case 0 -> {
                        new HTitle(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(500),
                                Messages.get("Minigame.started-title"), Messages.get("Minigame.started-subtitle")).send();
                        plugin.getStats().miniGamePlayed();
                        new HSound(Sound.ENTITY_PLAYER_LEVELUP).play();
                        startTime = System.currentTimeMillis();
                        started = true;
                        startMiniGame();
                        scoreboardGame();
                        countdownTask.cancel();
                    }
                }
                secondsCountdown--;
            }
        }.runTaskTimer(plugin, 0, 20);

    }

    private void scoreboardGame() {
        scoreboardGameTask = new BukkitRunnable() {

            @Override
            public void run() {

                secondsGame--;

                ranking = sortMapByValue(ranking, lowerIsBetterGame);
                updateScoreboardGame();

                if (secondsGame == 0) {
                    scoreboardGameTask.cancel();
                }

            }
        }.runTaskTimer(plugin, 5, 20);

    }

    private void updateScoreboardGame() {
        for (Player all : Bukkit.getOnlinePlayers()) {

            int scoreboardRow = 5;
            int place = 0;
            int previous = -1;
            int scoreboardPlace = getScoreboardPlace(all);
            int playersUpToScoreboardRow = scoreboardPlace > 7 ? 9 : 11;

            sb.updateLine(0, all, Component.text(""));
            sb.updateLine(1, all, Messages.get("Scoreboard.minigame"));
            sb.updateLine(2, all, Messages.getWithReplace("Scoreboard.minigame-name",
                    Map.of("%name%", PlainTextComponentSerializer.plainText().serialize(Messages.get(name + ".name")))));
            sb.updateLine(3, all, Component.text(""));
            sb.updateLine(4, all, Messages.get("Scoreboard.ranking"));

            for (Player current : winner) {
                place++;

                if (scoreboardRow <= playersUpToScoreboardRow) {
                    sb.updateLine(scoreboardRow++, all, LegacyComponentSerializer.legacySection()
                            .deserialize("§a" + place + "# " + getPlayerColor(current, all) + current.getName()));
                } else {
                    break;
                }
            }

            for (Map.Entry<Player, Integer> current : ranking.entrySet()) {
                if (previous != current.getValue()) {
                    place++;
                }
                previous = current.getValue();

                if (scoreboardRow <= playersUpToScoreboardRow) {
                    sb.updateLine(scoreboardRow++, all, LegacyComponentSerializer.legacySection()
                            .deserialize("§a" + place + "# §8(§7" + current.getValue() + "§8) " + getPlayerColor(current.getKey(), all) +
                                         current.getKey().getName()));
                } else {
                    break;
                }
            }

            if (playersUpToScoreboardRow == 9) {
                sb.updateLine(scoreboardRow++, all, Component.text("..."));
                sb.updateLine(scoreboardRow++, all, LegacyComponentSerializer.legacySection()
                        .deserialize("§a" + place + "# §8(§7" + ranking.get(all) + "§8) " + getPlayerColor(all, all) + all.getName()));
            }

            while (scoreboardRow <= 11) {
                sb.updateLine(scoreboardRow++, all, Component.text(""));
            }

            if (timeScoreboardGame) {
                sb.updateLine(12, all, Component.text(""));
                sb.updateLine(13, all, Messages.get("Scoreboard.ending-in"));
                sb.updateLine(14, all, Messages.getWithReplace("Scoreboard.time", Map.of("%seconds%", String.valueOf(secondsGame))));
            } else {
                sb.updateLine(12, all, Component.text(""));
                sb.updateLine(13, all, Messages.get("Scoreboard.players-remaining"));
                sb.updateLine(14, all, Messages.getWithReplace("Scoreboard.players", Map.of(
                        "%player%", String.valueOf(plugin.getPlayers().size() - diePlayers.size()),
                        "%max-players%", String.valueOf(plugin.getPlayers().size()))));
            }

        }
    }

    private String getPlayerColor(Player player0, Player player1) {
        if (player0 == player1) {
            return "§e";
        } else {
            return "§f";
        }
    }

    private int getScoreboardPlace(Player player) {
        int scoreboardPlace = 0;

        if (plugin.getPlayers().contains(player)) {
            if (winner.contains(player)) {
                for (Player current : winner) {
                    scoreboardPlace++;
                    if (current == player) {
                        break;
                    }
                }
            } else {
                scoreboardPlace = winner.size();
                for (Map.Entry<Player, Integer> current : ranking.entrySet()) {
                    scoreboardPlace++;
                    if (current.getKey() == player) {
                        break;
                    }
                }
            }
        }

        return scoreboardPlace;
    }

    @Override
    public void stop() {
        started = false;
        ranking = sortMapByValue(ranking, lowerIsBetterGame);
        winner();
        scoreboardGameTask.cancel();
        updateScoreboardGame();

        for (Player player : plugin.getPlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setFireTicks(0);
            player.setHealth(20);
            GameOverviewGUI.giveItem(player);
        }

        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§c" + name.toUpperCase() + " STATE STOPPED!");
    }

    private void winner() {

        int place = 1;
        int previous = -1;

        String preOutput = "";
        String row = "";

        Map<Player, Integer> forDatabase = new HashMap<>();

        for (Player player : winner) {
            preOutput += "§a§l" + place + "§a# §7§l>> " + getColor(place) + winner.get(place - 1).getName() + "§e +" + (6 - place) +
                         Messages.get("points") + "\n";

            addStars(player, place);
            forDatabase.put(player, place);

            place++;
            if (place == 6) {
                break;
            }

        }

        if (place <= 5) {

            if (!ranking.isEmpty()) {

                for (Map.Entry<Player, Integer> current : ranking.entrySet()) {

                    if (setRecords) {
                        if (mapName == null) {
                            plugin.getStats().record(current.getKey(), name, current.getValue(), !lowerIsBetterGame);
                        } else {
                            plugin.getStats().record(current.getKey(), name + mapName, current.getValue(), !lowerIsBetterGame);
                        }
                    }

                    if (previous != -1 && previous != current.getValue()) {
                        preOutput += "§a§l" + place + "§a# §7§l>> " + row + "§e +" + (6 - place) + Messages.get("points") + "\n";
                        row = "";

                        place++;
                        if (place == 6) {
                            break;
                        }

                    }

                    if (row.equals("")) {
                        row = getColor(place) + current.getKey().getName();
                    } else {
                        row += "§7, " + getColor(place) + current.getKey().getName();
                    }

                    addStars(current.getKey(), place);
                    forDatabase.put(current.getKey(), place);

                    previous = current.getValue();
                }

                if (place <= 5) {
                    preOutput += "§a§l" + place + "§a# §7§l>> " + row + "§e +" + (6 - place) + Messages.get("points") + "\n";
                }

            }

        }

        plugin.getStats().miniGameFinished(forDatabase);

        new HSound(Sound.ENTITY_PLAYER_LEVELUP).play();
        String outPut = "§7§m--------------------------------§r\n \n" + preOutput + "\n \n§7§m--------------------------------§r";
        plugin.getServer().broadcast(LegacyComponentSerializer.legacySection().deserialize(outPut));

    }

    private String getColor(int place) {
        return switch (place) {
            case 1 -> "§6";
            case 2 -> "§7";
            default -> "§c";
        };
    }

    private void addStars(Player player, int place) {
        int points = 6 - place;
        if (plugin.getStars().containsKey(player)) {
            plugin.getStars().put(player, plugin.getStars().get(player) + points);
        } else {
            plugin.getStars().put(player, points);
        }
    }

    protected boolean addWinner(Player player) {
        if (winner.size() < 5) {
            winner.add(player);
            ranking.remove(player);
            new HSound(Sound.ENTITY_PLAYER_LEVELUP).playFor(player);

            if (setRecordsInAddWinner) {
                if (mapName == null) {
                    plugin.getStats().record(player, name, (int) (System.currentTimeMillis() - startTime), false);
                } else {
                    plugin.getStats().record(player, name + mapName, (int) (System.currentTimeMillis() - startTime), false);
                }
            }

            plugin.getServer()
                    .broadcast(Main.PREFIX.append(
                            Messages.getWithReplace(name + ".winner-message-" + winner.size(), Map.of("%player%", player.getName()))));
            return true;
        } else {
            return false;
        }
    }

    protected void addDiePlayer(Player player) {
        diePlayers.add(player);
        plugin.getServer()
                .broadcast(Main.PREFIX.append(Messages.getWithReplace(name + ".player-out", Map.of("%player%", player.getName()))));
        new HTitle(Duration.ofMillis(500), Duration.ofMillis(1000), Duration.ofMillis(1000), Component.empty(),
                Messages.get("Minigame.player-out-title")).sendTo(player);
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, boolean order) {
        Map<K, V> sorted;

        if (order) {
            sorted = map.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        } else {
            sorted = map.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        }

        return sorted;
    }

    private String formatTime(int time) {
        if (time % 1000 < 100) {
            if (time % 100 < 10) {
                return time / 1000 + ",00" + time % 1000 + "s";
            } else {
                return time / 1000 + ",0" + time % 1000 + "s";
            }
        } else {
            return time / 1000 + "," + time % 1000 + "s";
        }
    }

}
