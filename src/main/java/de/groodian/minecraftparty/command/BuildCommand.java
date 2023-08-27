package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.command.HCommandPaper;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class BuildCommand extends HCommandPaper<Player> {

    private final Main plugin;

    public BuildCommand(Main plugin) {
        super(Player.class, "build", "Enable building for you", Main.PREFIX, "minecraftparty.build", List.of(), List.of());
        this.plugin = plugin;
    }

    @Override
    protected void onCall(Player player, String[] strings) {
        if (!plugin.getBuild().contains(player)) {
            plugin.getBuild().add(player);
            player.setGameMode(GameMode.CREATIVE);
            sendMsg(player, Messages.get("Commands.build.enabled"));
        } else {
            plugin.getBuild().remove(player);
            player.setGameMode(GameMode.SURVIVAL);
            sendMsg(player, Messages.get("Commands.build.disabled"));
        }
    }

}
