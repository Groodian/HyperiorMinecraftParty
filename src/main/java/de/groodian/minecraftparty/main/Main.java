package de.groodian.minecraftparty.main;

import de.groodian.hyperiorcore.command.HCommandManagerPaper;
import de.groodian.hyperiorcore.gui.GUIManager;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.minecraftparty.command.BuildCommand;
import de.groodian.minecraftparty.command.SetupCommand;
import de.groodian.minecraftparty.command.StartCommand;
import de.groodian.minecraftparty.gamestate.GameStateManager;
import de.groodian.minecraftparty.gamestate.GameStates;
import de.groodian.minecraftparty.gui.GameOverviewGUI;
import de.groodian.minecraftparty.listener.BreakoutListener;
import de.groodian.minecraftparty.listener.ColorBattleListener;
import de.groodian.minecraftparty.listener.GunGameListener;
import de.groodian.minecraftparty.listener.KingOfTheHillListener;
import de.groodian.minecraftparty.listener.LobbyListener;
import de.groodian.minecraftparty.listener.MainListener;
import de.groodian.minecraftparty.listener.MasterBuilderListener;
import de.groodian.minecraftparty.network.MinecraftPartyClient;
import de.groodian.minecraftparty.playerhider.PlayerHideListener;
import de.groodian.minecraftparty.stats.Stats;
import de.groodian.minecraftparty.stats.Top10;
import de.groodian.minecraftparty.util.LocationManager;
import de.groodian.network.DataPackage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin {

    public static final String PREFIX_CONSOLE = "§7[§eMinecraftParty§7] §r";
    public static final int MIN_PLAYERS = 2, MAX_PLAYERS = 12;

    public static Component PREFIX = null;
    public static Component NO_PERMISSION = null;

    private int groupNumber;
    private BukkitTask stopTask;
    private LocationManager locationManager;
    private GameStateManager gameStateManager;
    private Stats stats;
    private MinecraftPartyClient client;
    private GUIManager gameOverviewGUIManager;

    private List<Player> players;
    private List<Player> build;
    private List<Player> toRemove;
    private Map<Player, Integer> stars;

    private long startTime = 0;

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoading plugin...");

        groupNumber = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(Bukkit.motd()));
        players = new ArrayList<>();
        build = new ArrayList<>();
        toRemove = new ArrayList<>();
        stars = new HashMap<>();
        stopTask = null;

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerHideListener(this), this);
        pluginManager.registerEvents(new LobbyListener(this), this);
        pluginManager.registerEvents(new MainListener(this), this);
        pluginManager.registerEvents(new GunGameListener(this), this);
        pluginManager.registerEvents(new ColorBattleListener(this), this);
        pluginManager.registerEvents(new BreakoutListener(this), this);
        pluginManager.registerEvents(new MasterBuilderListener(this), this);
        pluginManager.registerEvents(new KingOfTheHillListener(this), this);
    }

    public void afterWorldLoad() {
        boolean setupMode = false;

        MainConfig.loadConfig(this);
        Messages.loadConfigs(this);

        locationManager = new LocationManager(this);
        if (locationManager.allLocationsSet()) {
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aAll locations loaded successfully.");
        } else {
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cYou have not set all locations correct, use §4/mpsetup§c.");
            Bukkit.getConsoleSender()
                    .sendMessage(PREFIX_CONSOLE + "§cFollowing locations are missing: " + locationManager.getMissingLocations());
            setupMode = true;
        }

        if (locationManager.JUMPANDRUN_LOCATIONS == null) {
            Bukkit.getConsoleSender()
                    .sendMessage(PREFIX_CONSOLE + "§cYou have to create at least one Jump and Run, use §4/mpsetup jumpandrun create§c.");
            setupMode = true;
        }

        if (setupMode) {
            HCommandManagerPaper commandManager = HyperiorCore.getPaper().getHCommandManagerPaper();
            commandManager.registerCommand(this, new SetupCommand(this));
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoaded in setup mode! (Most features are disabled!)");
            return;
        }


        gameOverviewGUIManager = new GUIManager(GameOverviewGUI.class, this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        gameStateManager = new GameStateManager(this);
        gameStateManager.setGameState(GameStates.LOBBY_STATE);

        stats = new Stats(this);

        Top10 top10 = new Top10(this);
        top10.set();

        HCommandManagerPaper commandManager = HyperiorCore.getPaper().getHCommandManagerPaper();
        commandManager.registerCommand(this, new SetupCommand(this));
        commandManager.registerCommand(this, new StartCommand(this));
        commandManager.registerCommand(this, new BuildCommand(this));

        client = new MinecraftPartyClient(this, "localhost", 4444, new DataPackage("LOGIN", "MinecraftParty", groupNumber));


        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoaded!");
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cStopping plugin...");

        if (client != null) {
            client.stop();
        }

        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cStopped!");
    }

    public void stopServer() {
        List<Player> playersDisconnecting = new ArrayList<>();

        final Main plugin = this;
        stopTask = new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!playersDisconnecting.contains(player)) {
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(b);
                        try {
                            out.writeUTF("Connect");
                            out.writeUTF(MainConfig.getString("fallback-server"));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                        playersDisconnecting.add(player);
                        return;
                    }
                }
                stopTask.cancel();
                Bukkit.shutdown();

            }
        }.runTaskTimer(this, 10, 5);
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public Stats getStats() {
        return stats;
    }

    public MinecraftPartyClient getClient() {
        return client;
    }

    public GUIManager getGameOverviewGUIManager() {
        return gameOverviewGUIManager;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getPlayTime() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getBuild() {
        return build;
    }

    public List<Player> getToRemove() {
        return toRemove;
    }

    public Map<Player, Integer> getStars() {
        return stars;
    }

}
