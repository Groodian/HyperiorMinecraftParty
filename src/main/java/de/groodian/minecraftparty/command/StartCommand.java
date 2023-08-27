package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.command.HCommandPaper;
import de.groodian.minecraftparty.gamestate.LobbyState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import java.util.List;
import org.bukkit.entity.Player;

public class StartCommand extends HCommandPaper<Player> {

    private static final int START_SECONDS = 3;

    private final Main plugin;

    public StartCommand(Main plugin) {
        super(Player.class, "start", "Start the round", Main.PREFIX, "minecraftparty.start", List.of(), List.of());
        this.plugin = plugin;
    }

    @Override
    protected void onCall(Player player, String[] args) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState lobbyState) {
            if (lobbyState.getCountdown().isRunning()) {
                if (lobbyState.getCountdown().getSeconds() > START_SECONDS) {
                    lobbyState.getCountdown().setSeconds(START_SECONDS);
                    sendMsg(player, Messages.get("Commands.start.started"));
                } else {
                    sendMsg(player, Messages.get("Commands.start.already-started"));
                }
            } else {
                sendMsg(player, Messages.get("Commands.start.need-more-player"));
            }
        } else {
            sendMsg(player, Messages.get("Commands.start.already-started"));
        }
    }

}
