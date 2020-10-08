package de.groodian.minecraftparty.stats;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.UUIDFetcher;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Top10 {

    private Main plugin;
    private Map<Integer, String> top10;
    private UUIDFetcher uuidFetcher;
    private Location top10Loc;

    private double x;
    private double y;
    private double z;

    public Top10(Main plugin) {
        this.plugin = plugin;
        this.top10 = new HashMap<>();
        this.uuidFetcher = new UUIDFetcher();
    }

    public void set() {

        top10Loc = plugin.getLocationManager().TOP10;
        x = top10Loc.getX();
        y = top10Loc.getY();
        z = top10Loc.getZ();

        try {
            PreparedStatement ps = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL().getConnection().prepareStatement("SELECT UUID FROM stats ORDER BY points DESC LIMIT 10");
            ResultSet rs = ps.executeQuery();
            int rank = 0;
            while (rs.next()) {
                rank++;
                top10.put(rank, rs.getString("UUID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {

            top10Loc.setZ(z - i);

            if (top10Loc.getBlock().getState() instanceof Skull) {
                Skull skull = (Skull) top10Loc.getBlock().getState();
                skull.setSkullType(SkullType.PLAYER);
                if (top10.get(i + 1) != null) {
                    skull.setOwner(uuidFetcher.getNameFromUUID(top10.get(i + 1)));
                } else {
                    skull.setOwner("Steve");
                }
                skull.setRotation(BlockFace.EAST);
                skull.update();
            }

            top10Loc.setX(x + 1);
            top10Loc.setY(y - 1);

            if (top10Loc.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) top10Loc.getBlock().getState();
                sign.setLine(0, Messages.get("Top10.line-1").replace("%place%", (i + 1) + ""));
                if (top10.get(i + 1) != null) {
                    sign.setLine(1, Messages.get("Top10.line-2").replace("%player%", uuidFetcher.getNameFromUUID(top10.get(i + 1))));
                    sign.setLine(2, Messages.get("Top10.line-3").replace("%points%", plugin.getStats().getPoints(top10.get(i + 1)) + ""));
                    sign.setLine(3, Messages.get("Top10.line-4").replace("%wins%", plugin.getStats().getGamesFirst(top10.get(i + 1)) + ""));
                } else {
                    sign.setLine(1, Messages.get("Top10.no-player"));
                    sign.setLine(2, "");
                    sign.setLine(3, "");
                }
                sign.update();
            }

            top10Loc.setX(x);
            top10Loc.setY(y);

        }

    }

}
