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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GameOverviewGUI extends GUI {

    private final Main plugin;

    public GameOverviewGUI(Main plugin) {
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
            putHead(player, current.getKey(), i, current.getValue());
            i++;
        }

        // Adding player with zero points
        for (Player player1 : plugin.getPlayers()) {
            if (!plugin.getStars().containsKey(player1)) {
                putHead(player, player1, i, 0);
                i++;
            }
        }
    }

    @Override
    public void onUpdate() {
    }

    private void putHead(Player inventoryOwner, Player player, int pos, int points) {
        Component component = Component.text(player.getName(), NamedTextColor.GOLD);
        if (player.getUniqueId().equals(inventoryOwner.getUniqueId())) {
            component = component.decoration(TextDecoration.BOLD, true);
        }

        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                .setName(component.append(Component.text(": " + points, NamedTextColor.GRAY)).append(Messages.get("points")))
                .setSkullOwner(player.getUniqueId())
                .build();

        putItem(head, pos, () -> {
            if (!plugin.getPlayers().contains(inventoryOwner)) {
                inventoryOwner.teleport(player);
            }
        });
    }

    public static void giveItem(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS)
                .setName(Messages.get("player-overview-item-name"))
                .addCustomData("interact", PersistentDataType.STRING, "Overview")
                .build());
    }

}
