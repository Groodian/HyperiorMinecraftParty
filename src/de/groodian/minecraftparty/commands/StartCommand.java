package de.groodian.minecraftparty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.minecraftparty.gamestates.LobbyState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;

public class StartCommand implements CommandExecutor {

	private Main plugin;
	private static final int START_SECONDS = 3;

	public StartCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (HyperiorCore.getRanks().has(player.getUniqueId(), "minecraftparty.start")) {
				if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) {
					LobbyState lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
					if (lobbyState.getCountdown().isRunning()) {
						if (lobbyState.getCountdown().getSeconds() > START_SECONDS) {
							lobbyState.getCountdown().setSeconds(START_SECONDS);
							player.sendMessage(Main.PREFIX + Messages.get("Commands.start.started"));
						} else
							player.sendMessage(Main.PREFIX + Messages.get("Commands.start.already-started"));
					} else
						player.sendMessage(Main.PREFIX + Messages.get("Commands.start.need-more-player"));
				} else
					player.sendMessage(Main.PREFIX + Messages.get("Commands.start.already-started"));
			} else
				player.sendMessage(Main.NO_PERMISSION);
		} else
			sender.sendMessage(Main.PREFIX + "This command has to be executed by a player.");
		return false;
	}

}
