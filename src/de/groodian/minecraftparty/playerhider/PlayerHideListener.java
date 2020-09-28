package de.groodian.minecraftparty.playerhider;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;

public class PlayerHideListener implements Listener {

	private Main plugin;

	public PlayerHideListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getPlayer().getItemInHand() != null) {
				if (e.getPlayer().getItemInHand().getItemMeta() != null) {
					if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null) {
						if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(Messages.get("PlayerHider.item-hide"))) {
							for (Player all : plugin.getPlayers()) {
								e.getPlayer().hidePlayer(all);
							}
							plugin.getPlayerHider().giveShowItem(e.getPlayer());
							e.getPlayer().sendMessage(Main.PREFIX + Messages.get("PlayerHider.message-hide"));
							return;
						}
						if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(Messages.get("PlayerHider.item-show"))) {
							for (Player all : plugin.getPlayers()) {
								e.getPlayer().showPlayer(all);
							}
							plugin.getPlayerHider().giveHideItem(e.getPlayer());
							e.getPlayer().sendMessage(Main.PREFIX + Messages.get("PlayerHider.message-show"));
							return;
						}
					}
				}
			}
		}
	}
}
