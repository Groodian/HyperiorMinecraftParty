package de.groodian.minecraftparty.stats;

import de.groodian.hyperiorcore.boards.Title;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcore.util.MySQL;
import de.groodian.hyperiorcore.util.MySQLConnection;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Record {

    private Main plugin;
    private MySQL minecraftPartyMySQL;
    private HSound sound;
    private Title title;

    public Record(Main plugin) {
        this.plugin = plugin;
        this.minecraftPartyMySQL = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL();
        this.sound = new HSound(Sound.WITHER_DEATH);
        this.title = new Title(5, 40, 5, Messages.get("new-record-title"), null);
    }

    /**
     * This method can be executed sync
     */
    public void setRecord(final Player player, final String game, final long record, final boolean mustBeHigher) {

        new Task(plugin) {
            @Override
            public void executeAsync() {
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

                if (isUserExists(player)) {
                    try {
                        MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
                        PreparedStatement ps = connection.getConnection().prepareStatement("UPDATE records SET " + game + " = ?, playername = ? WHERE UUID = ?");
                        ps.setLong(1, record);
                        ps.setString(2, player.getName());
                        ps.setString(3, player.getUniqueId().toString().replaceAll("-", ""));
                        ps.executeUpdate();
                        ps.close();
                        connection.finish();
                        cache.add(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
                        PreparedStatement ps = connection.getConnection().prepareStatement("INSERT INTO records (UUID,playername," + game + ") VALUES (?,?,?)");
                        ps.setString(1, player.getUniqueId().toString().replaceAll("-", ""));
                        ps.setString(2, player.getName());
                        ps.setLong(3, record);
                        ps.executeUpdate();
                        ps.close();
                        connection.finish();
                        cache.add(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void executeSyncOnFinish() {
                if (cache.size() == 1) {
                    if ((boolean) cache.get(0)) {
                        player.sendMessage(Main.PREFIX + Messages.get("new-record-message"));
                        sound.playFor(player);
                        title.sendTo(player);
                    }
                }
            }
        };

    }

    /**
     * This method should be executed async
     */
    public int getRecord(String uuid, String game) {
        uuid = uuid.replaceAll("-", "");
        try {
            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps = connection.getConnection().prepareStatement("SELECT " + game + " FROM records WHERE UUID = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            int record = -1;
            if (rs.next()) {
                int temp = rs.getInt(game);
                if (temp != 0) {
                    record = temp;
                }
            }
            ps.close();
            connection.finish();
            return record;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean isUserExists(Player player) {
        try {
            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps = connection.getConnection().prepareStatement("SELECT playername FROM records WHERE UUID = ?");
            ps.setString(1, player.getUniqueId().toString().replaceAll("-", ""));
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

}
