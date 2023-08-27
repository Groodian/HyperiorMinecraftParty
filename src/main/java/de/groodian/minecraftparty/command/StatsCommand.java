package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.command.HArgument;
import de.groodian.hyperiorcore.command.HCommandPaper;
import de.groodian.hyperiorcore.command.HTabCompleteType;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.gui.StatsGUI;
import java.util.List;
import org.bukkit.entity.Player;

public class StatsCommand extends HCommandPaper<Player> {

    private final Main plugin;

    public StatsCommand(Main plugin) {
        super(Player.class, "stats", "Stats of an player", Main.PREFIX, null, List.of(),
                List.of(new HArgument("player", false, HTabCompleteType.PLAYER, true)));
        this.plugin = plugin;
    }

    @Override
    protected void onCall(Player player, String[] args) {
        if (args.length == 0) {
            new StatsGUI(plugin).open(player, player.getName());
        } else if (args.length == 1) {
            new StatsGUI(plugin).open(player, args[0]);
        }
    }

}
