package de.groodian.minecraftparty.playerhider;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PlayerHideListener implements Listener {

    private final Main plugin;
    private final PlayerHider playerHider;

    public PlayerHideListener(Main plugin) {
        this.plugin = plugin;
        this.playerHider = new PlayerHider(plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
            String interact = ItemBuilder.getCustomData(itemInMainHand, "interact", PersistentDataType.STRING);
            if (interact != null) {
                if (interact.equals("PlayerHide")) {
                    for (Player all : plugin.getPlayers()) {
                        e.getPlayer().hidePlayer(plugin, all);
                    }
                    playerHider.giveShowItem(e.getPlayer());
                    e.getPlayer().sendMessage(Main.PREFIX.append(Messages.get("PlayerHider.message-hide")));
                } else if (interact.equals("PlayerShow")) {
                    for (Player all : plugin.getPlayers()) {
                        e.getPlayer().showPlayer(plugin, all);
                    }
                    playerHider.giveHideItem(e.getPlayer());
                    e.getPlayer().sendMessage(Main.PREFIX.append(Messages.get("PlayerHider.message-show")));
                }
            }
        }
    }
}
