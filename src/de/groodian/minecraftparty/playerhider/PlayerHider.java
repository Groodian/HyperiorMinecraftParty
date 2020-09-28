package de.groodian.minecraftparty.playerhider;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;

public class PlayerHider {

	private Main plugin;

	public PlayerHider(Main plugin) {
		this.plugin = plugin;
	}

	public void giveHideItem(Player p) {
		ItemStack item = new ItemBuilder(Material.GLOWSTONE_DUST).setName(Messages.get("PlayerHider.item-hide")).build();
		p.getInventory().setItem(8, item);
	}

	public void giveShowItem(Player p) {
		ItemStack item = new ItemBuilder(Material.SULPHUR).setName(Messages.get("PlayerHider.item-show")).build();
		p.getInventory().setItem(8, item);
	}

	public void remove() {
		for (Player all : plugin.getPlayers()) {
			for (Player all2 : plugin.getPlayers()) {
				all2.showPlayer(all);
			}
			all.getInventory().clear(8);
		}
	}

}
