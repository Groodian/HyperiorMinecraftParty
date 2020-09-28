package de.groodian.minecraftparty.stats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import de.groodian.hyperiorcore.boards.Title;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.MySQL;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;

public class Record {

	private MySQL minecraftPartyMySQL;
	private HSound sound;
	private Title title;

	public Record() {
		this.minecraftPartyMySQL = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL();
		this.sound = new HSound(Sound.WITHER_DEATH);
		this.title = new Title(5, 40, 5, Messages.get("new-record-title"), null);
	}

	public boolean isUserExists(Player player, String game) {
		try {
			PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("SELECT playername FROM records WHERE UUID = ?");
			ps.setString(1, player.getUniqueId().toString().replaceAll("-", ""));
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setRecord(Player player, String game, long record, boolean mustBeHigher) {

		int oldRecord = getRecord(player.getUniqueId().toString().replaceAll("-", ""), game);

		if (!(oldRecord == -1)) {
			if (mustBeHigher) {
				if (oldRecord >= record)
					return;
			} else {
				if (oldRecord <= record) {
					return;
				}
			}
		}

		if (isUserExists(player, game)) {
			try {
				PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("UPDATE records SET " + game + " = ?, playername = ? WHERE UUID = ?");
				ps.setLong(1, record);
				ps.setString(2, player.getName());
				ps.setString(3, player.getUniqueId().toString().replaceAll("-", ""));
				ps.executeUpdate();
				player.sendMessage(Main.PREFIX + Messages.get("new-record-message"));
				sound.playFor(player);
				title.sendTo(player);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("INSERT INTO records (UUID,playername," + game + ") VALUES (?,?,?)");
				ps.setString(1, player.getUniqueId().toString().replaceAll("-", ""));
				ps.setString(2, player.getName());
				ps.setLong(3, record);
				ps.executeUpdate();
				player.sendMessage(Main.PREFIX + Messages.get("new-record-message"));
				sound.playFor(player);
				title.sendTo(player);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

	}

	public int getRecord(String uuid, String game) {
		try {
			PreparedStatement ps = minecraftPartyMySQL.getConnection().prepareStatement("SELECT " + game + " FROM records WHERE UUID = ?");
			ps.setString(1, uuid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int temp = rs.getInt(game);
				if (temp == 0) {
					return -1;
				} else {
					return temp;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
