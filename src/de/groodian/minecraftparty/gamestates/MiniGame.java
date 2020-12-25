package de.groodian.minecraftparty.gamestates;

import de.groodian.hyperiorcore.boards.HScoreboard;
import de.groodian.hyperiorcore.boards.Title;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // private stuff
    private Map<Player, Integer> records;
    private BukkitTask countdownTask;
    private int secondsCountdown;
    private BukkitTask scoreboardGameTask;
    private HScoreboard sb;

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
        this.records = new HashMap<>();
        this.countdownTask = null;
        this.secondsCountdown = 6;
        this.scoreboardGameTask = null;
        this.sb = HyperiorCore.getSB();
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

                Bukkit.broadcastMessage(Main.PREFIX + Messages.get("next-minigame"));
                new HSound(Sound.FIREWORK_LAUNCH).play();
                if (MainConfig.getBoolean(name + ".reset")) {
                    prepare();
                }

            }
        }.runTaskLater(plugin, 35);

        new BukkitRunnable() {
            @Override
            public void run() {

                Bukkit.broadcastMessage(Messages.get("Minigame.game-info-header"));
                Bukkit.broadcastMessage(Messages.get(name + ".game-info").replace("%name%", Messages.get(name + ".name").toUpperCase()));
                Bukkit.broadcastMessage(Messages.get("Minigame.game-info-fooder"));
                new HSound(Sound.FIREWORK_TWINKLE2).play();

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
                        Bukkit.broadcastMessage(Main.PREFIX + Messages.get("Minigame.teleport-player"));
                        beforeCountdownStart();
                        if (firstMiniGame) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (MainConfig.getBoolean("Scoreboard.animation")) {
                                    sb.registerScoreboard(player, Messages.get("Scoreboard.title"), 15, MainConfig.getInt("Scoreboard.delay"), MainConfig.getInt("Scoreboard.delay-between-animation"));
                                } else {
                                    sb.registerScoreboard(player, Messages.get("Scoreboard.title"), 15);
                                }
                            }
                            firstMiniGame = false;
                        }
                        scoreboardRecords();
                        countdown();
                    }
                };

            }
        }.runTaskLater(plugin, 105);

    }

    private void loadRecords() {
        if (mapName == null) {
            for (Player all : plugin.getPlayers()) {
                records.put(all, plugin.getRecord().getRecord(all.getUniqueId().toString().replaceAll("-", ""), name));
            }
        } else {
            for (Player all : plugin.getPlayers()) {
                records.put(all, plugin.getRecord().getRecord(all.getUniqueId().toString().replaceAll("-", ""), name + mapName));
            }
        }
    }

    private void scoreboardRecords() {
        for (Player all : Bukkit.getOnlinePlayers()) {

            int scoreboardRow = 5;
            int place = 0;

            sb.updateLine(0, all, "");
            sb.updateLine(1, all, Messages.get("Scoreboard.minigame"));
            sb.updateLine(2, all, Messages.get("Scoreboard.minigame-name").replace("%name%", Messages.get(name + ".name")));
            sb.updateLine(3, all, "");
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

                    sb.updateLine(scoreboardRow++, all, "§a" + place + "# §8(§7" + playerValue + "§8) " + getPlayerColor(all, current.getKey()) + current.getKey().getName());

                }

            }

            if (scoreboardRow == 5) {
                sb.updateLine(scoreboardRow++, all, Messages.get("Scoreboard.no-records"));
            }

            while (scoreboardRow <= 11) {
                sb.updateLine(scoreboardRow++, all, "");
            }

            if (timeScoreboardGame) {
                sb.updateLine(12, all, "");
                sb.updateLine(13, all, Messages.get("Scoreboard.ending-in"));
                sb.updateLine(14, all, Messages.get("Scoreboard.time").replace("%seconds%", (secondsGame - 1) + ""));
            } else {
                sb.updateLine(12, all, "");
                sb.updateLine(13, all, Messages.get("Scoreboard.players-remaining"));
                sb.updateLine(14, all, Messages.get("Scoreboard.players").replace("%player%", (plugin.getPlayers().size() - diePlayers.size()) + "").replace("%max-players%", plugin.getPlayers().size() + ""));
            }

        }
    }

    private void countdown() {
        countdownTask = new BukkitRunnable() {

            @Override
            public void run() {
                switch (secondsCountdown) {
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                        new Title(0, 30, 0, Messages.get("Minigame.start-title").replace("%name%", Messages.get(name + ".name")), Messages.get("Minigame.start-subtitle").replaceAll("%seconds%", "" + secondsCountdown))
                                .send();
                        new HSound(Sound.NOTE_STICKS).play();
                        break;
                    case 1:
                        new Title(0, 30, 0, Messages.get("Minigame.start-title-last-second").replace("%name%", Messages.get(name + ".name")), Messages.get("Minigame.start-subtitle-last-second")).send();
                        new HSound(Sound.NOTE_STICKS).play();
                        break;
                    case 0:
                        new Title(0, 20, 10, Messages.get("Minigame.started-title"), Messages.get("Minigame.started-subtitle")).send();
                        plugin.getStats().miniGamePlayed();
                        new HSound(Sound.LEVEL_UP).play();
                        startTime = System.currentTimeMillis();
                        started = true;
                        startMiniGame();
                        scoreboardGame();
                        countdownTask.cancel();
                        break;
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

            sb.updateLine(0, all, "");
            sb.updateLine(1, all, Messages.get("Scoreboard.minigame"));
            sb.updateLine(2, all, Messages.get("Scoreboard.minigame-name").replace("%name%", Messages.get(name + ".name")));
            sb.updateLine(3, all, "");
            sb.updateLine(4, all, Messages.get("Scoreboard.ranking"));

            for (Player current : winner) {
                place++;

                if (scoreboardRow <= playersUpToScoreboardRow) {
                    sb.updateLine(scoreboardRow++, all, "§a" + place + "# " + getPlayerColor(current, all) + current.getName());
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
                    sb.updateLine(scoreboardRow++, all, "§a" + place + "# §8(§7" + current.getValue() + "§8) " + getPlayerColor(current.getKey(), all) + current.getKey().getName());
                } else {
                    break;
                }
            }

            if (playersUpToScoreboardRow == 9) {
                sb.updateLine(scoreboardRow++, all, "...");
                sb.updateLine(scoreboardRow++, all, "§a" + place + "# §8(§7" + ranking.get(all) + "§8) " + getPlayerColor(all, all) + all.getName());
            }

            while (scoreboardRow <= 11) {
                sb.updateLine(scoreboardRow++, all, "");
            }

            if (timeScoreboardGame) {
                sb.updateLine(12, all, "");
                sb.updateLine(13, all, Messages.get("Scoreboard.ending-in"));
                sb.updateLine(14, all, Messages.get("Scoreboard.time").replace("%seconds%", secondsGame + ""));
            } else {
                sb.updateLine(12, all, "");
                sb.updateLine(13, all, Messages.get("Scoreboard.players-remaining"));
                sb.updateLine(14, all, Messages.get("Scoreboard.players").replace("%player%", (plugin.getPlayers().size() - diePlayers.size()) + "").replace("%max-players%", plugin.getPlayers().size() + ""));
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
            plugin.getGameOverview().giveItem(player);
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
            preOutput += "§a§l" + place + "§a# §7§l>> " + getColor(place) + winner.get(place - 1).getName() + "§e +" + (6 - place) + Messages.get("points") + "\n";

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
                            plugin.getRecord().setRecord(current.getKey(), name, current.getValue(), !lowerIsBetterGame);
                        } else {
                            plugin.getRecord().setRecord(current.getKey(), name + mapName, current.getValue(), !lowerIsBetterGame);
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

        new HSound(Sound.LEVEL_UP).play();
        String outPut = "§7§m--------------------------------§r\n \n" + preOutput + "\n \n§7§m--------------------------------§r";
        Bukkit.broadcastMessage(outPut);

    }

    private String getColor(int place) {
        switch (place) {
            case 1:
                return "§6";
            case 2:
                return "§7";
            default:
                return "§c";
        }
    }

    private void addStars(Player player, int place) {
        int points = 6 - place;
        if (plugin.getStars().containsKey(player)) {
            plugin.getStars().put(player, plugin.getStars().get(player) + points);
        } else {
            plugin.getStars().put(player, points);
        }
    }

    protected void addWinner(Player player) {
        winner.add(player);
        ranking.remove(player);
        new HSound(Sound.LEVEL_UP).playFor(player);

        if (setRecordsInAddWinner) {
            if (mapName == null) {
                plugin.getRecord().setRecord(player, name, System.currentTimeMillis() - startTime, false);
            } else {
                plugin.getRecord().setRecord(player, name + mapName, System.currentTimeMillis() - startTime, false);
            }
        }

        Bukkit.broadcastMessage(Main.PREFIX + Messages.get(name + ".winner-message-" + winner.size()).replace("%player%", player.getName()));

    }

    protected void addDiePlayer(Player player) {
        diePlayers.add(player);
        Bukkit.broadcastMessage(Main.PREFIX + Messages.get(name + ".player-out").replace("%player%", player.getName()));
        new Title(10, 20, 20, "", Messages.get("Minigame.player-out-title")).sendTo(player);
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, boolean order) {
        Map<K, V> sorted;

        if (order) {
            sorted = map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        } else {
            sorted = map.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
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
