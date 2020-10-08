package de.groodian.minecraftparty.stats;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.MySQL;
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
    public void playTime(Player player) {
        add(player, "playtime", plugin.getPlayTime());
        addPoints(player, (int) plugin.getPlayTime() / 3000); // 20 points per minute
    }

    /**
     * This method can be executed sync
     */
    public void miniGamePlayed(Player player) {
        add(player, "minigamesplayed", 1);
        addPoints(player, 15);
    }

    /**
     * This method can be executed sync
     */
    public void gamePlayed(Player player) {
        add(player, "gamesplayed", 1);
        addPoints(player, 30);
    }

    /**
     * This method can be executed sync
     */
    public void gameEnded(Player player) {
        add(player, "gamesended", 1);
        addPoints(player, 150);
    }

    /**
     * This method can be executed sync
     */
    public void gameFirst(Player player) {
        add(player, "gamesfirst", 1);
        addPoints(player, 500);
    }

    /**
     * This method can be executed sync
     */
    public void gameSecond(Player player) {
        add(player, "gamessecond", 1);
        addPoints(player, 400);
    }

    /**
     * This method can be executed sync
     */
    public void gameThird(Player player) {
        add(player, "gamesthird", 1);
        addPoints(player, 350);
    }

    /**
     * This method can be executed sync
     */
    public void gameFourth(Player player) {
        add(player, "gamesfourth", 1);
        addPoints(player, 300);
    }

    /**
     * This method can be executed sync
     */
    public void gameFifth(Player player) {
        add(player, "gamesfifth", 1);
        addPoints(player, 250);
    }

    /**
     * This method can be executed sync
     */
    public void miniGameFirst(Player player) {
        add(player, "minigamesfirst", 1);
        addPoints(player, 50);
    }

    /**
     * This method can be executed sync
     */
    public void miniGameSecond(Player player) {
        add(player, "minigamessecond", 1);
        addPoints(player, 40);
    }

    /**
     * This method can be executed sync
     */
    public void miniGameThird(Player player) {
        add(player, "minigamesthird", 1);
        addPoints(player, 35);
    }

    /**
     * This method can be executed sync
     */
    public void miniGameFourth(Player player) {
        add(player, "minigamesfourth", 1);
        addPoints(player, 30);
    }

    /**
     * This method can be executed sync
     */
    public void miniGameFifth(Player player) {
        add(player, "minigamesfifth", 1);
        addPoints(player, 25);
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
            PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("SELECT playername FROM stats WHERE UUID = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            return rs.next();
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
                if (!isUserExists(player.getUniqueId().toString().replaceAll("-", ""))) {
                    try {
                        PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("INSERT INTO stats (UUID, playername, points, playtime, minigamesplayed, gamesplayed, gamesended, gamesfirst, gamessecond, gamesthird, gamesfourth, gamesfifth, minigamesfirst, minigamessecond, minigamesthird, minigamesfourth, minigamesfifth) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        ps.setString(1, player.getUniqueId().toString().replaceAll("-", ""));
                        ps.setString(2, player.getName());
                        ps.setLong(3, 0);
                        ps.setLong(4, 0);
                        ps.setLong(5, 0);
                        ps.setLong(6, 0);
                        ps.setLong(7, 0);
                        ps.setLong(8, 0);
                        ps.setLong(9, 0);
                        ps.setLong(10, 0);
                        ps.setLong(11, 0);
                        ps.setLong(12, 0);
                        ps.setLong(13, 0);
                        ps.setLong(14, 0);
                        ps.setLong(15, 0);
                        ps.setLong(16, 0);
                        ps.setLong(17, 0);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("UPDATE stats SET " + statistic + " = ?, playername = ? WHERE UUID = ?");
                    ps.setLong(1, (amount + get(player.getUniqueId().toString().replaceAll("-", ""), statistic)));
                    ps.setString(2, player.getName());
                    ps.setString(3, player.getUniqueId().toString().replaceAll("-", ""));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private long get(String uuid, String statistic) {
        uuid = uuid.replaceAll("-", "");
        try {
            PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("SELECT " + statistic + " FROM stats WHERE UUID = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong(statistic);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getRank(String uuid, String statistic) {
        uuid = uuid.replaceAll("-", "");
        int temp = 0;
        try {
            PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("SELECT * FROM stats ORDER BY " + statistic + " DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                temp++;
                if (rs.getString("UUID").equals(uuid)) {
                    return temp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
