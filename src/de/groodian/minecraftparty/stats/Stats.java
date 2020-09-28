package de.groodian.minecraftparty.stats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.MySQL;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;

public class Stats {

	private Main plugin;
	private MySQL minecraftPartyMySQL;
	private Map<Player, Integer> points;

	public Stats(Main plugin) {
		this.plugin = plugin;
		this.minecraftPartyMySQL = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL();
		this.points = new HashMap<>();
	}

	public void playTime(Player player) {
		add(player, "playtime", plugin.getPlayTime());
		addPoints(player, (int) plugin.getPlayTime() / 3000); // 20 points per minute
	}

	public void miniGamePlayed(Player player) {
		add(player, "minigamesplayed", 1);
		addPoints(player, 15);
	}

	public void gamePlayed(Player player) {
		add(player, "gamesplayed", 1);
		addPoints(player, 30);
	}

	public void gameEnded(Player player) {
		add(player, "gamesended", 1);
		addPoints(player, 150);
	}

	public void gameFirst(Player player) {
		add(player, "gamesfirst", 1);
		addPoints(player, 500);
	}

	public void gameSecond(Player player) {
		add(player, "gamessecond", 1);
		addPoints(player, 400);
	}

	public void gameThird(Player player) {
		add(player, "gamesthird", 1);
		addPoints(player, 350);
	}

	public void gameFourth(Player player) {
		add(player, "gamesfourth", 1);
		addPoints(player, 300);
	}

	public void gameFifth(Player player) {
		add(player, "gamesfifth", 1);
		addPoints(player, 250);
	}

	public void miniGameFirst(Player player) {
		add(player, "minigamesfirst", 1);
		addPoints(player, 50);
	}

	public void miniGameSecond(Player player) {
		add(player, "minigamessecond", 1);
		addPoints(player, 40);
	}

	public void miniGameThird(Player player) {
		add(player, "minigamesthird", 1);
		addPoints(player, 35);
	}

	public void miniGameFourth(Player player) {
		add(player, "minigamesfourth", 1);
		addPoints(player, 30);
	}

	public void miniGameFifth(Player player) {
		add(player, "minigamesfifth", 1);
		addPoints(player, 25);
	}

	public long getPlayTime(String uuid) {
		return get(uuid, "playtime");
	}

	public long getPoints(String uuid) {
		return get(uuid, "points");
	}

	public int getRank(String uuid) {
		return getRank(uuid, "points");
	}

	public long getMiniGamesPlayed(String uuid) {
		return get(uuid, "minigamesplayed");
	}

	public long getGamesPlayed(String uuid) {
		return get(uuid, "gamesplayed");
	}

	public long getGamesEnded(String uuid) {
		return get(uuid, "gamesended");
	}

	public long getGamesFirst(String uuid) {
		return get(uuid, "gamesfirst");
	}

	public long getGamesSecond(String uuid) {
		return get(uuid, "gamessecond");
	}

	public long getGamesThird(String uuid) {
		return get(uuid, "gamesthird");
	}

	public long getGamesFourth(String uuid) {
		return get(uuid, "gamesfourth");
	}

	public long getGamesFifth(String uuid) {
		return get(uuid, "gamesfifth");
	}

	public long getMiniGamesFirst(String uuid) {
		return get(uuid, "minigamesfirst");
	}

	public long getMiniGamesSecond(String uuid) {
		return get(uuid, "minigamessecond");
	}

	public long getMiniGamesThird(String uuid) {
		return get(uuid, "minigamesthird");
	}

	public long getMiniGamesFourth(String uuid) {
		return get(uuid, "minigamesfourth");
	}

	public long getMiniGamesFifth(String uuid) {
		return get(uuid, "minigamesfifth");
	}

	private void addPoints(Player player, int pointsToAdd) {
		if (points.containsKey(player)) {
			points.put(player, points.get(player) + pointsToAdd);
		} else {
			points.put(player, pointsToAdd);
		}
		add(player, "points", pointsToAdd);
	}

	public void finish() {
		for (Map.Entry<Player, Integer> current : points.entrySet()) {
			current.getKey().sendMessage(Messages.get("points-summary").replace("%points%", current.getValue() + ""));
			HyperiorCore.getCoinSystem().addCoins(current.getKey(), current.getValue(), true);
			HyperiorCore.getLevel().updateLevel(current.getKey());
		}
	}

	private void add(Player player, String statistic, long amount) {
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
			amount += get(player.getUniqueId().toString().replaceAll("-", ""), statistic);
			PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("UPDATE stats SET " + statistic + " = ?, playername = ? WHERE UUID = ?");
			ps.setLong(1, amount);
			ps.setString(2, player.getName());
			ps.setString(3, player.getUniqueId().toString().replaceAll("-", ""));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isUserExists(String uuid) {
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

	private long get(String uuid, String statistic) {
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
