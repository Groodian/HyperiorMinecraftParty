package de.groodian.minecraftparty.gamestates;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.hyperiorcore.util.Particle;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorBattleState extends MiniGame {

    private Location start;

    private List<Snowball> snowballs;
    private Map<Player, Byte> colors;

    private BukkitTask gameTask;

    public ColorBattleState(String name, Main plugin) {
        super(name, plugin);
        this.start = plugin.getLocationManager().COLORBATTLE_START;
        this.snowballs = new ArrayList<>();
        this.colors = new HashMap<>();
        this.gameTask = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void prepare() {
        int x1, x2, y1, y2, z1, z2;
        x1 = (int) plugin.getLocationManager().COLORBATTLE_LOCATION_1.getX();
        x2 = (int) plugin.getLocationManager().COLORBATTLE_LOCATION_2.getX();
        y1 = (int) plugin.getLocationManager().COLORBATTLE_LOCATION_1.getY();
        y2 = (int) plugin.getLocationManager().COLORBATTLE_LOCATION_2.getY();
        z1 = (int) plugin.getLocationManager().COLORBATTLE_LOCATION_1.getZ();
        z2 = (int) plugin.getLocationManager().COLORBATTLE_LOCATION_2.getZ();
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (z1 > z2) {
            int temp = z1;
            z1 = z2;
            z2 = temp;
        }
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                for (int l = z1; l < z2; l++) {
                    Location loc = new Location(plugin.getLocationManager().COLORBATTLE_LOCATION_1.getWorld(), i, j, l);
                    if (loc.getBlock().getType() == Material.STAINED_CLAY) {
                        loc.getBlock().setData((byte) 0);
                    }
                }
            }
        }
    }

    private void snowballTrail() {
        for (Snowball snowball : snowballs) {
            if (snowball.isOnGround() || snowball.isDead()) {
                snowballs.remove(snowball);
                return;
            } else {
                new Particle(EnumParticle.FLAME, snowball.getLocation(), true, 0, 0, 0, 0, 1, 0).sendAll();
            }
        }
    }

    @Override
    protected void beforeCountdownStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(start);
            plugin.getTeleportFix().doFor(player);
        }
    }

    @Override
    protected void startMiniGame() {
        byte data = 0;
        for (Player player : plugin.getPlayers()) {
            player.getInventory().setItem(0, new ItemBuilder(Material.valueOf(MainConfig.getString("ColorBattle.color-gun-material"))).setName(Messages.get("ColorBattle.color-gun-name")).build());
            data++;
            colors.put(player, data);
            ranking.put(player, 0);
        }

        gameTask = new BukkitRunnable() {

            @Override
            public void run() {

                snowballTrail();

                if (secondsGame <= 0 || plugin.getPlayers().size() == 1) {
                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();
                }
            }

        }.runTaskTimer(plugin, 0, 10);
    }

    public boolean isStarted() {
        return started;
    }

    public List<Snowball> getSnowballs() {
        return snowballs;
    }

    public Map<Player, Byte> getColors() {
        return colors;
    }

    public Map<Player, Integer> getRanking() {
        return ranking;
    }

}
