package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GunGameState extends MiniGame {

    private final List<ItemStack> items;

    private BukkitTask gameTask;

    public GunGameState(String name, Main plugin) {
        super(name, plugin);
        super.timeRecords = true;
        super.lowerIsBetterRecords = true;
        super.setRecords = false;
        this.items = MainConfig.getItemWithNameList("GunGame.items");
        this.gameTask = null;
    }

    public void giveWeapon(Player player) {
        if (started) {

            int temp = ranking.get(player);

            if (temp >= items.size()) {
                player.getInventory().setItem(0, items.get(items.size() - 1));
            } else {
                player.getInventory().setItem(0, items.get(temp));
            }

            if (player.getInventory().getItem(0).getType() == Material.BOW) {
                player.getInventory()
                        .setItem(8, new ItemBuilder(Material.ARROW).setAmount(MainConfig.getInt("GunGame.arrow-amount"))
                                .setName(Messages.get("GunGame.arrow"))
                                .build());
            }

        }

    }

    @Override
    protected void prepare() {
    }

    @Override
    protected void beforeCountdownStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportManager.addTeleport(player, plugin.getLocationManager().getRespawnLocation(plugin.getLocationManager().GUNGAME), null);
        }
    }

    @Override
    protected void startMiniGame() {

        for (Player player : plugin.getPlayers()) {
            ranking.put(player, 0);
            giveWeapon(player);
        }

        gameTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (plugin.getPlayers().size() == 1 || secondsGame <= 0 || ranking.containsValue(items.size())) {

                    for (Player player : plugin.getPlayers()) {
                        if (ranking.get(player) == 7) {
                            plugin.getStats().record(player, "GunGame", (int) (System.currentTimeMillis() - startTime), false);
                        }
                    }

                    gameTask.cancel();

                    // Make sure the last player has respawned
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        plugin.getGameStateManager().setRandomGameState();
                    }, 20);
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    public boolean isStarted() {
        return started;
    }

    public Map<Player, Integer> getRanking() {
        return ranking;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
