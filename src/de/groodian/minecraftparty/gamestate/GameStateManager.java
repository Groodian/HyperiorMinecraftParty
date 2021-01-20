package de.groodian.minecraftparty.gamestate;

import de.groodian.minecraftparty.gamestate.minigame.BreakoutState;
import de.groodian.minecraftparty.gamestate.minigame.ColorBattleState;
import de.groodian.minecraftparty.gamestate.minigame.GunGameState;
import de.groodian.minecraftparty.gamestate.minigame.HotGroundState;
import de.groodian.minecraftparty.gamestate.minigame.JumpAndRunState;
import de.groodian.minecraftparty.gamestate.minigame.KingOfTheHillState;
import de.groodian.minecraftparty.gamestate.minigame.MasterBuildersState;
import de.groodian.minecraftparty.gamestate.minigame.TrafficLightRaceState;
import de.groodian.minecraftparty.gamestate.minigame.WoolBlockState;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameStateManager {

    private Main plugin;
    private Map<GameStates, GameState> gameStates;
    private GameState currentGameState;
    private List<GameStates> gameStatsUsed;

    public GameStateManager(Main plugin) {
        this.plugin = plugin;
        gameStates = new HashMap<>();
        gameStatsUsed = new ArrayList<>();

        gameStates.put(GameStates.LOBBY_STATE, new LobbyState(plugin));
        gameStates.put(GameStates.WOOLBLOCK_STATE, new WoolBlockState("WoolBlock", plugin));
        gameStates.put(GameStates.JUMPANDRUN_STATE, new JumpAndRunState("JumpAndRun", plugin));
        gameStates.put(GameStates.TRAFFICLIGHTRACE_STATE, new TrafficLightRaceState("TrafficLightRace", plugin));
        gameStates.put(GameStates.HOTGROUND_STATE, new HotGroundState("HotGround", plugin));
        gameStates.put(GameStates.GUNGAME_STATE, new GunGameState("GunGame", plugin));
        gameStates.put(GameStates.COLORBATTLE_STATE, new ColorBattleState("ColorBattle", plugin));
        gameStates.put(GameStates.BREAKOUT_STATE, new BreakoutState("Breakout", plugin));
        gameStates.put(GameStates.MASTERBUILDERS_STATE, new MasterBuildersState("MasterBuilders", plugin));
        gameStates.put(GameStates.KINGOFTHEHILL_STATE, new KingOfTheHillState("KingOfTheHill", plugin));
        gameStates.put(GameStates.ENDING_STATE, new EndingState(plugin));
    }

    public void setGameState(GameStates gameState) {
        if (currentGameState != null)
            currentGameState.stop();

        for (Player player : plugin.getToRemove()) {
            plugin.getStars().remove(player);
        }
        plugin.getToRemove().clear();

        currentGameState = gameStates.get(gameState);
        currentGameState.start();

        if (plugin.getClient() != null) {
            plugin.getClient().sendUpdate();
        }
    }

    public void stopCurrentGameState() {
        if (currentGameState != null) {
            currentGameState.stop();
            currentGameState = null;
        }

    }

    public void setRandomGameState() {
        boolean finished = false;
        if (plugin.getPlayers().size() == 1) {
            setGameState(GameStates.ENDING_STATE);
            finished = true;
        }
        if (gameStatsUsed.size() == gameStates.size() - 2) {
            setGameState(GameStates.ENDING_STATE);
            finished = true;
        }
        while (!finished) {
            GameStates ran = GameStates.getRandomGameState();
            if (!gameStatsUsed.contains(ran) && ran != GameStates.LOBBY_STATE && ran != GameStates.ENDING_STATE) {
                gameStatsUsed.add(ran);
                setGameState(ran);
                finished = true;
            }
        }
    }

    public String getCurrentGameStateName() {
        if (getCurrentGameState() instanceof LobbyState) {
            return "Lobby";
        } else if (getCurrentGameState() instanceof EndingState) {
            return "Ending";
        } else if (getCurrentGameState() instanceof MiniGame) {
            MiniGame miniGame = (MiniGame) getCurrentGameState();
            return Messages.get(miniGame.name + ".name");
        } else {
            return "Error";
        }
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

}
