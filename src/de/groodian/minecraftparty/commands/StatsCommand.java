package de.groodian.minecraftparty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.groodian.hyperiorcore.util.UUIDFetcher;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;

public class StatsCommand implements CommandExecutor {

	private Main plugin;
	private UUIDFetcher uuidFetcher;

	public StatsCommand(Main plugin) {
		this.plugin = plugin;
		this.uuidFetcher = new UUIDFetcher();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.this-take-a-moment"));
				if (!plugin.getStats().isUserExists(player.getUniqueId().toString().replaceAll("-", ""))) {
					player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.you-never-played"));
				} else {
					plugin.getStatsGUI().openGUI(player, player.getUniqueId().toString().replaceAll("-", ""), player.getName());
				}
			} else if (args.length == 1) {
				player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.this-take-a-moment"));
				String uuid = uuidFetcher.getUUID(args[0]);
				if (uuid == null) {
					player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.this-player-does-not-exist"));
				} else if (!plugin.getStats().isUserExists(uuid)) {
					player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.this-player-never-played"));
				} else {
					plugin.getStatsGUI().openGUI(player, uuid, uuidFetcher.getName(args[0]));
				}
			} else
				player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.usage"));

		} else
			sender.sendMessage(Main.PREFIX + "This command has to be executed by a player.");
		return false;
	}
}