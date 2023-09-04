package de.groodian.minecraftparty.stats;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.user.MinecraftPartyStats;
import de.groodian.hyperiorcore.util.Task;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.profile.PlayerTextures;

public class Top10 {

    private final Main plugin;

    public Top10(Main plugin) {
        this.plugin = plugin;
    }

    public void set() {
        new Task(plugin) {
            @Override
            public void executeAsync() {
                cache.add(MinecraftPartyStats.getTop10(HyperiorCore.getPaper().getDatabaseManager()));
            }

            @Override
            public void executeSyncOnFinish() {
                setBlocks((List<MinecraftPartyStats.Top10>) cache.get(0));
            }
        };
    }

    private void setBlocks(List<MinecraftPartyStats.Top10> top10) {
        Location top10Loc = plugin.getLocationManager().TOP10.clone();
        double x = top10Loc.getX();
        double y = top10Loc.getY();
        double z = top10Loc.getZ();

        for (int i = 0; i < 10; i++) {
            MinecraftPartyStats.Top10 player = null;
            if (i < top10.size()) {
                player = top10.get(i);
            }

            top10Loc.setZ(z - i);

            if (top10Loc.getBlock().getState() instanceof Skull skull) {
                skull.setType(Material.PLAYER_HEAD);
                if (player != null) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(player.uuid()));
                } else {
                    PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
                    PlayerTextures playerTextures = playerProfile.getTextures();
                    try {
                        playerTextures.setSkin(
                                new URL("http://textures.minecraft.net/texture/65b95da1281642daa5d022adbd3e7cb69dc0942c81cd63be9c3857d222e1c8d9"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    playerProfile.setTextures(playerTextures);
                    skull.setPlayerProfile(playerProfile);
                }
                Rotatable rotatable = (Rotatable) skull.getBlockData();
                rotatable.setRotation(BlockFace.WEST);
                skull.setBlockData(rotatable);
                skull.update();
            }

            top10Loc.setX(x + 1);
            top10Loc.setY(y - 1);

            if (top10Loc.getBlock().getState() instanceof Sign sign) {
                sign.line(0, Messages.getWithReplace("Top10.line-1", Map.of("%place%", (String.valueOf(i + 1)))));
                if (player != null) {
                    sign.line(1, Messages.getWithReplace("Top10.line-2", Map.of("%player%", player.name())));
                    sign.line(2, Messages.getWithReplace("Top10.line-3", Map.of("%points%", String.valueOf(player.points()))));
                    sign.line(3, Messages.getWithReplace("Top10.line-4", Map.of("%wins%", String.valueOf(player.wins()))));
                } else {
                    sign.line(1, Messages.get("Top10.no-player"));
                    sign.line(2, Component.empty());
                    sign.line(3, Component.empty());
                }
                sign.update();
            }

            top10Loc.setX(x);
            top10Loc.setY(y);

        }

    }

}
