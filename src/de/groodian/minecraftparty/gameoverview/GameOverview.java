package de.groodian.minecraftparty.gameoverview;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GameOverview {

    private Main plugin;
    private HashMap<Player, Integer> sorted;

    public GameOverview(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, Messages.get("player-overview-inventory-name"));
        sorted = plugin.getStars().entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        int i = 0;
        for (Map.Entry<Player, Integer> current : sorted.entrySet()) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(current.getKey().getName());
            meta.setDisplayName("§6" + current.getKey().getName() + "§7: " + current.getValue() + Messages.get("points"));
            skull.setItemMeta(meta);
            inventory.setItem(i, skull);
            i++;
        }

        // Adding player with zero points
        for (Player player1 : plugin.getPlayers()) {
            if (!plugin.getStars().containsKey(player1)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwner(player1.getName());
                meta.setDisplayName("§6" + player1.getName() + "§7: 0" + Messages.get("points"));
                skull.setItemMeta(meta);
                inventory.setItem(i, skull);
                i++;
            }
        }
        player.openInventory(inventory);
    }

    public void giveItem(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS).setName(Messages.get("player-overview-item-name")).build());
    }
}
