package de.groodian.minecraftparty.gui;

import de.groodian.hyperiorcore.gui.GUI;
import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameOverview extends GUI {

    private final Main plugin;

    public GameOverview(Main plugin) {
        super(Messages.get("player-overview-inventory-name"), 27);
        this.plugin = plugin;
    }

    @Override
    protected void onOpen() {
        HashMap<Player, Integer> sorted = plugin.getStars()
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        int i = 0;
        for (Map.Entry<Player, Integer> current : sorted.entrySet()) {
            putItem(craftHead(player, current.getKey()), i);
            i++;
        }

        // Adding player with zero points
        for (Player player1 : plugin.getPlayers()) {
            if (!plugin.getStars().containsKey(player1)) {
                putItem(craftHead(player, player1), i);
                i++;
            }
        }
    }

    @Override
    public void onUpdate() {
    }

    public void giveItem(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS).setName(Messages.get("player-overview-item-name")).build());
    }

    private ItemStack craftHead(Player inventoryOwner, Player player) {
        String isOwn = "";
        if (player.getUniqueId().equals(inventoryOwner.getUniqueId())) {
            isOwn = "§l";
        }

        return new ItemBuilder(Material.PLAYER_HEAD).setName(
                        "§6" + isOwn + player.getName() + "§7: 0" + Messages.get("points"))
                .setSkullOwner(player.getUniqueId())
                .build();
    }

}
