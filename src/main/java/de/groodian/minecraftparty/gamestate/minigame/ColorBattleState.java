package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.util.HParticle;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ColorBattleState extends MiniGame {

    private final Location start;

    private final List<Snowball> snowballs;
    private final Map<Player, Byte> colors;

    private BukkitTask gameTask;

    public ColorBattleState(String name, Main plugin) {
        super(name, plugin);
        this.start = plugin.getLocationManager().COLORBATTLE_START;
        this.snowballs = new ArrayList<>();
        this.colors = new HashMap<>();
        this.gameTask = null;
    }

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
                    if (Tag.TERRACOTTA.isTagged(loc.getBlock().getType())) {
                        loc.getBlock().setType(Material.TERRACOTTA);
                    }
                }
            }
        }
    }

    private void snowballTrail() {
        List<Snowball> toRemove = new ArrayList<>();

        for (Snowball snowball : snowballs) {
            if (snowball.isOnGround() || snowball.isDead()) {
                toRemove.add(snowball);
            } else {
                new HParticle(Particle.FLAME).send(snowball.getLocation());
            }
        }

        for (Snowball snowball : toRemove) {
            snowballs.remove(snowball);
        }
    }

    @Override
    protected void beforeCountdownStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportManager.addTeleport(player, start, null);
        }
    }

    @Override
    protected void startMiniGame() {
        byte data = 0;
        for (Player player : plugin.getPlayers()) {
            player.getInventory()
                    .setItem(0, new ItemBuilder(Material.valueOf(MainConfig.getString("ColorBattle.color-gun-material"))).setName(
                            Messages.get("ColorBattle.color-gun-name")).build());
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

        }.runTaskTimer(plugin, 0, 2);
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
