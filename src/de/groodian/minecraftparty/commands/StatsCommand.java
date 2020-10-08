package de.groodian.minecraftparty.commands;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private Main plugin;

    public StatsCommand(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                new StatsGUI(plugin).open(player, player.getName());
            } else if (args.length == 1) {
                new StatsGUI(plugin).open(player, args[0]);
            } else
                player.sendMessage(Main.PREFIX + Messages.get("Commands.stats.usage"));
        } else
            sender.sendMessage(Main.PREFIX + "This command has to be executed by a player.");
        return false;
    }
}