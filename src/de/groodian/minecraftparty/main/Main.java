package de.groodian.minecraftparty.main;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.MySQL;
import de.groodian.hyperiorcore.util.MySQLConnection;
import de.groodian.minecraftparty.commands.BuildCommand;
import de.groodian.minecraftparty.commands.SetupCommand;
import de.groodian.minecraftparty.commands.StartCommand;
import de.groodian.minecraftparty.commands.StatsCommand;
import de.groodian.minecraftparty.gameoverview.GameOverview;
import de.groodian.minecraftparty.gamestates.GameStateManager;
import de.groodian.minecraftparty.gamestates.GameStates;
import de.groodian.minecraftparty.listeners.BreakoutListener;
import de.groodian.minecraftparty.listeners.ColorBattleListener;
import de.groodian.minecraftparty.listeners.GunGameListener;
import de.groodian.minecraftparty.listeners.LobbyListener;
import de.groodian.minecraftparty.listeners.MainListener;
import de.groodian.minecraftparty.network.MinecraftPartyClient;
import de.groodian.minecraftparty.playerhider.PlayerHideListener;
import de.groodian.minecraftparty.playerhider.PlayerHider;
import de.groodian.minecraftparty.stats.Record;
import de.groodian.minecraftparty.stats.Stats;
import de.groodian.minecraftparty.stats.Top10;
import de.groodian.minecraftparty.util.JumpAndRunLocations;
import de.groodian.minecraftparty.util.LocationManager;
import de.groodian.minecraftparty.util.TeleportFix;
import de.groodian.network.DataPackage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin {

    public static String PREFIX = null;
    public static String NO_PERMISSION = null;
    public static final String VERSION = "4.0 BETA";
    public static final String PREFIX_CONSOLE = "§7[§eMinecraftParty§7] §r";
    public static final int MIN_PLAYERS = 2, MAX_PLAYERS = 12;

    private LocationManager locationManager;
    private GameStateManager gameStateManager;
    private Record record;
    private Stats stats;
    private PlayerHider playerHider;
    private GameOverview gameOverview;
    private TeleportFix teleportFix;
    private MinecraftPartyClient client;

    private List<Player> players;
    private List<Player> build;
    private List<Player> toRemove;
    private Map<Player, Integer> stars;

    private long startTime = 0;

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoading plugin...");

        boolean setupMode = false;

        MainConfig.loadConfig(this);
        Messages.loadConfigs(this);

        locationManager = new LocationManager();
        if (locationManager.allLocationsSet()) {
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aAll locations loaded successfully.");
        } else {
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cYou have not set all locations correct, use §4/mpsetup§c.");
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cFollowing locations are missing: " + locationManager.getMissingLocations());
            setupMode = true;
        }

        if (locationManager.JUMPANDRUN_LOCATIONS == null) {
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cYou have to create at least one Jump and Run, use §4/mpsetup jumpandrun create§c.");
            setupMode = true;
        }

        if (setupMode) {
            getCommand("mpsetup").setExecutor(new SetupCommand(this));
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoaded in setup mode! (Most features are disabled!)");
            return;
        }

        players = new ArrayList<>();
        build = new ArrayList<>();
        toRemove = new ArrayList<>();
        stars = new HashMap<>();

        MySQL minecraftPartyMySQL = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL();

        try {

            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps01 = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS woolblock INT(100)");
            PreparedStatement ps02 = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS trafficlightrace INT(100)");
            PreparedStatement ps03 = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS hotground INT(100)");
            PreparedStatement ps04 = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS gungame INT(100)");
            PreparedStatement ps05 = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS colorbattle INT(100)");
            PreparedStatement ps06 = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS breakout INT(100)");
            ps01.executeUpdate();
            ps01.close();
            ps02.executeUpdate();
            ps02.close();
            ps03.executeUpdate();
            ps03.close();
            ps04.executeUpdate();
            ps04.close();
            ps05.executeUpdate();
            ps05.close();
            ps06.executeUpdate();
            ps06.close();

            for (JumpAndRunLocations jumpAndRun : locationManager.JUMPANDRUN_LOCATIONS) {
                PreparedStatement ps = connection.getConnection().prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS jumpandrun" + jumpAndRun.getName() + " INT(100)");
                ps.executeUpdate();
                ps.close();
            }

            connection.finish();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        gameStateManager = new GameStateManager(this);
        gameStateManager.setGameState(GameStates.MASTERBUILDERS_STATE);

        record = new Record(this);
        stats = new Stats(this);
        playerHider = new PlayerHider(this);
        gameOverview = new GameOverview(this);
        teleportFix = new TeleportFix(this);

        Top10 top10 = new Top10(this);
        top10.set();

        init(Bukkit.getPluginManager());

        client = new MinecraftPartyClient(this, "localhost", 4444, new DataPackage("LOGIN", "MinecraftParty", Integer.parseInt(Bukkit.getServerName())));

        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoaded!");
    }

    private void init(PluginManager pluginManager) {
        getCommand("mpsetup").setExecutor(new SetupCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));

        pluginManager.registerEvents(new PlayerHideListener(this), this);

        pluginManager.registerEvents(new LobbyListener(this), this);
        pluginManager.registerEvents(new MainListener(this), this);
        pluginManager.registerEvents(new GunGameListener(this), this);
        pluginManager.registerEvents(new ColorBattleListener(this), this);
        pluginManager.registerEvents(new BreakoutListener(this), this);
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cStopping plugin...");

        if(client != null) {
            client.stop();
        }

        Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§cStopped!");
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public Record getRecord() {
        return record;
    }

    public Stats getStats() {
        return stats;
    }

    public TeleportFix getTeleportFix() {
        return teleportFix;
    }

    public MinecraftPartyClient getClient() {
        return client;
    }

    public PlayerHider getPlayerHider() {
        return playerHider;
    }

    public GameOverview getGameOverview() {
        return gameOverview;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getPlayTime() {
        return System.currentTimeMillis() - startTime;
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
