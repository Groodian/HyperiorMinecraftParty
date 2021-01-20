package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class KingOfTheHillState extends MiniGame {

    private ItemStack item;
    private int goldPoints;
    private int ironPoints;

    private BukkitTask gameTask;

    public KingOfTheHillState(String name, Main plugin) {
        super(name, plugin);
        item = new ItemBuilder(MainConfig.getItemWithName("KingOfTheHill.item")).addEnchantment(Enchantment.getByName(MainConfig.getString("KingOfTheHill.enchantment")), MainConfig.getInt("KingOfTheHill.enchantment-level")).build();
        goldPoints = MainConfig.getInt("KingOfTheHill.gold-points");
        ironPoints = MainConfig.getInt("KingOfTheHill.iron-points");
    }

    public void giveWeapon(Player player) {
        player.getInventory().setItem(0, item);
    }

    @Override
    protected void prepare() {
    }

    @Override
    protected void beforeCountdownStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayerToTeleport(player, plugin.getLocationManager().getRespawnLocation(plugin.getLocationManager().KINGOFTHEHILL));
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

                for (Player player : plugin.getPlayers()) {
                    Material material = player.getLocation().add(0, -1, 0).getBlock().getType();
                    if (material == Material.GOLD_BLOCK) {
                        ranking.put(player, ranking.get(player) + goldPoints);
                    } else if (material == Material.IRON_BLOCK) {
                        ranking.put(player, ranking.get(player) + ironPoints);
                    }
                }

                if (plugin.getPlayers().size() == 1 || secondsGame <= 0) {
                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public boolean isStarted() {
        return started;
    }

}
