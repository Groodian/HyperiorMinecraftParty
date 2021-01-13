package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {

    private Main plugin;

    public BuildCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (HyperiorCore.getRanks().has(player.getUniqueId(), "minecraftparty.build")) {
                if (!plugin.getBuild().contains(player)) {
                    plugin.getBuild().add(player);
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(Main.PREFIX + Messages.get("Commands.build.enabled"));
                } else {
                    plugin.getBuild().remove(player);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(Main.PREFIX + Messages.get("Commands.build.disabled"));
                }
            } else {
                player.sendMessage(Main.NO_PERMISSION);
            }
        } else
            sender.sendMessage(Main.PREFIX + "This command has to be executed by a player.");
        return false;
    }

}