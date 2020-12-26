package de.groodian.minecraftparty.stats;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.MySQL;
import de.groodian.hyperiorcore.util.MySQLConnection;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Stats {

    private Main plugin;
    private MySQL minecraftPartyMySQL;
    private Map<Player, Integer> points;

    public Stats(Main plugin) {
        this.plugin = plugin;
        this.minecraftPartyMySQL = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL();
        this.points = new HashMap<>();
    }

    /**
     * This method can be executed sync
     */
    public void playTime(final Player player) {
        add(player, "playtime", plugin.getPlayTime());
        addPoints(player, (int) plugin.getPlayTime() / 3000); // 20 points per minute
    }

    /**
     * This method can be executed sync
     */
    public void miniGamePlayed() {
        for (Player player : plugin.getPlayers()) {
            add(player, "minigamesplayed", 1);
            addPoints(player, 15);
        }
    }

    /**
     * This method can be executed sync
     */
    public void gamePlayed() {
        for (Player player : plugin.getPlayers()) {
            add(player, "gamesplayed", 1);
            addPoints(player, 30);
        }
    }

    /**
     * This method can be executed sync
     */
    public void gameEnded() {
        for (Player player : plugin.getPlayers()) {
            add(player, "gamesended", 1);
            add(player, "playtime", plugin.getPlayTime());
            addPoints(player, ((int) plugin.getPlayTime() / 3000) + 150); // 20 points per minute + 150 for game ended
        }
    }

    /**
     * This method can be executed sync
     */
    public void gameFinished(Map<Player, Integer> winners) {
        for (Map.Entry<Player, Integer> winner : winners.entrySet()) {
            Player player = winner.getKey();
            switch (winner.getValue()) {
                case 1:
                    add(player, "gamesfirst", 1);
                    addPoints(player, 500);
                    break;
                case 2:
                    add(player, "gamessecond", 1);
                    addPoints(player, 400);
                    break;
                case 3:
                    add(player, "gamesthird", 1);
                    addPoints(player, 350);
                    break;
                case 4:
                    add(player, "gamesfourth", 1);
                    addPoints(player, 300);
                    break;
                case 5:
                    add(player, "gamesfifth", 1);
                    addPoints(player, 250);
                    break;
            }
        }
    }

    /**
     * This method can be executed sync
     */
    public void miniGameFinished(Map<Player, Integer> winners) {
        for (Map.Entry<Player, Integer> winner : winners.entrySet()) {
            Player player = winner.getKey();
            switch (winner.getValue()) {
                case 1:
                    add(player, "minigamesfirst", 1);
                    addPoints(player, 50);
                    break;
                case 2:
                    add(player, "minigamessecond", 1);
                    addPoints(player, 40);
                    break;
                case 3:
                    add(player, "minigamesthird", 1);
                    addPoints(player, 35);
                    break;
                case 4:
                    add(player, "minigamesfourth", 1);
                    addPoints(player, 30);
                    break;
                case 5:
                    add(player, "minigamesfifth", 1);
                    addPoints(player, 25);
                    break;
            }
        }
    }

    /**
     * This method should be executed async
     */
    public long getPlayTime(String uuid) {
        return get(uuid, "playtime");
    }

    /**
     * This method should be executed async
     */
    public long getPoints(String uuid) {
        return get(uuid, "points");
    }

    /**
     * This method should be executed async
     */
    public int getRank(String uuid) {
        return getRank(uuid, "points");
    }

    /**
     * This method should be executed async
     */
    public long getMiniGamesPlayed(String uuid) {
        return get(uuid, "minigamesplayed");
    }

    /**
     * This method should be executed async
     */
    public long getGamesPlayed(String uuid) {
        return get(uuid, "gamesplayed");
    }

    /**
     * This method should be executed async
     */
    public long getGamesEnded(String uuid) {
        return get(uuid, "gamesended");
    }

    /**
     * This method should be executed async
     */
    public long getGamesFirst(String uuid) {
        return get(uuid, "gamesfirst");
    }

    /**
     * This method should be executed async
     */
    public long getGamesSecond(String uuid) {
        return get(uuid, "gamessecond");
    }

    /**
     * This method should be executed async
     */
    public long getGamesThird(String uuid) {
        return get(uuid, "gamesthird");
    }

    /**
     * This method should be executed async
     */
    public long getGamesFourth(String uuid) {
        return get(uuid, "gamesfourth");
    }

    /**
     * This method should be executed async
     */
    public long getGamesFifth(String uuid) {
        return get(uuid, "gamesfifth");
    }

    /**
     * This method should be executed async
     */
    public long getMiniGamesFirst(String uuid) {
        return get(uuid, "minigamesfirst");
    }

    /**
     * This method should be executed async
     */
    public long getMiniGamesSecond(String uuid) {
        return get(uuid, "minigamessecond");
    }

    /**
     * This method should be executed async
     */
    public long getMiniGamesThird(String uuid) {
        return get(uuid, "minigamesthird");
    }

    /**
     * This method should be executed async
     */
    public long getMiniGamesFourth(String uuid) {
        return get(uuid, "minigamesfourth");
    }

    /**
     * This method should be executed async
     */
    public long getMiniGamesFifth(String uuid) {
        return get(uuid, "minigamesfifth");
    }

    /**
     * This method can be executed sync
     */
    public void finish() {
        for (Map.Entry<Player, Integer> current : points.entrySet()) {
            current.getKey().sendMessage(Messages.get("points-summary").replace("%points%", current.getValue() + ""));
            HyperiorCore.getCoinSystem().addCoins(current.getKey(), current.getValue(), true);
            HyperiorCore.getLevel().updateLevel(current.getKey());
        }
    }

    /**
     * This method should be executed async
     */
    public boolean isUserExists(String uuid) {
        uuid = uuid.replaceAll("-", "");
        try {
            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps = connection.getConnection().prepareStatement("SELECT playername FROM stats WHERE UUID = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            boolean userExists = rs.next();
            ps.close();
            connection.finish();
            return userExists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addPoints(Player player, int pointsToAdd) {
        if (points.containsKey(player)) {
            points.put(player, points.get(player) + pointsToAdd);
        } else {
            points.put(player, pointsToAdd);
        }
        add(player, "points", pointsToAdd);
    }

    private void add(final Player player, final String statistic, final long amount) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String uuid = player.getUniqueId().toString().replaceAll("-", "");

                    // Check if the user exists with the same database connection because,
                    // if two threads add data at the same time the user get twice inserted (like above add points and something else at the same time)
                    MySQLConnection connection01 = minecraftPartyMySQL.getMySQLConnection();
                    PreparedStatement ps01 = connection01.getConnection().prepareStatement("SELECT playername FROM stats WHERE UUID = ?");
                    ps01.setString(1, uuid);
                    ResultSet rs = ps01.executeQuery();
                    boolean userExists = rs.next();
                    ps01.close();

                    if (!userExists) {
                        PreparedStatement ps02 = connection01.getConnection().prepareStatement("INSERT INTO stats (UUID, playername, points, playtime, minigamesplayed, gamesplayed, gamesended, gamesfirst, gamessecond, gamesthird, gamesfourth, gamesfifth, minigamesfirst, minigamessecond, minigamesthird, minigamesfourth, minigamesfifth) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        ps02.setString(1, uuid);
                        ps02.setString(2, player.getName());
                        ps02.setLong(3, 0);
                        ps02.setLong(4, 0);
                        ps02.setLong(5, 0);
                        ps02.setLong(6, 0);
                        ps02.setLong(7, 0);
                        ps02.setLong(8, 0);
                        ps02.setLong(9, 0);
                        ps02.setLong(10, 0);
                        ps02.setLong(11, 0);
                        ps02.setLong(12, 0);
                        ps02.setLong(13, 0);
                        ps02.setLong(14, 0);
                        ps02.setLong(15, 0);
                        ps02.setLong(16, 0);
                        ps02.setLong(17, 0);
                        ps02.executeUpdate();
                        ps02.close();
                    }

                    // Finish connection to get data in the next step
                    connection01.finish();

                    long databaseValue = get(uuid, statistic);
                    MySQLConnection connection02 = minecraftPartyMySQL.getMySQLConnection();
                    PreparedStatement ps = connection02.getConnection().prepareStatement("UPDATE stats SET " + statistic + " = ?, playername = ? WHERE UUID = ?");
                    ps.setLong(1, (amount + databaseValue));
                    ps.setString(2, player.getName());
                    ps.setString(3, uuid);
                    ps.executeUpdate();
                    ps.close();
                    connection02.finish();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private long get(String uuid, String statistic) {
        uuid = uuid.replaceAll("-", "");
        try {
            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps = connection.getConnection().prepareStatement("SELECT " + statistic + " FROM stats WHERE UUID = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            long returnLong = 0;
            if (rs.next()) {
                returnLong = rs.getLong(statistic);
            }
            ps.close();
            connection.finish();
            return returnLong;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getRank(String uuid, String statistic) {
        uuid = uuid.replaceAll("-", "");
        try {
            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps = connection.getConnection().prepareStatement("SELECT * FROM stats ORDER BY " + statistic + " DESC");
            ResultSet rs = ps.executeQuery();
            int rank = 0;
            while (rs.next()) {
                rank++;
                if (rs.getString("UUID").equals(uuid)) {
                    break;
                }
            }
            ps.close();
            connection.finish();
            return rank;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
