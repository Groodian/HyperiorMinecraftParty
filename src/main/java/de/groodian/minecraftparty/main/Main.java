package de.groodian.minecraftparty.main;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.MySQL;
import de.groodian.hyperiorcore.util.MySQLConnection;
import de.groodian.minecraftparty.command.BuildCommand;
import de.groodian.minecraftparty.command.SetupCommand;
import de.groodian.minecraftparty.command.StartCommand;
import de.groodian.minecraftparty.command.StatsCommand;
import de.groodian.minecraftparty.gui.GameOverview;
import de.groodian.minecraftparty.gamestate.GameStateManager;
import de.groodian.minecraftparty.gamestate.GameStates;
import de.groodian.minecraftparty.listener.BreakoutListener;
import de.groodian.minecraftparty.listener.ColorBattleListener;
import de.groodian.minecraftparty.listener.GunGameListener;
import de.groodian.minecraftparty.listener.KingOfTheHillListener;
import de.groodian.minecraftparty.listener.LobbyListener;
import de.groodian.minecraftparty.listener.MainListener;
import de.groodian.minecraftparty.listener.MasterBuilderListener;
import de.groodian.minecraftparty.network.MinecraftPartyClient;
import de.groodian.minecraftparty.playerhider.PlayerHideListener;
import de.groodian.minecraftparty.stats.Record;
import de.groodian.minecraftparty.stats.Stats;
import de.groodian.minecraftparty.stats.Top10;
import de.groodian.minecraftparty.util.JumpAndRunLocations;
import de.groodian.minecraftparty.util.LocationManager;
import de.groodian.network.DataPackage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
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

    private BukkitTask stopTask;
    private LocationManager locationManager;
    private GameStateManager gameStateManager;
    private Record record;
    private Stats stats;
    private GameOverview gameOverview;
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
            getCommand("mpsetup").setExecutor(new SetupCommand(this));
            Bukkit.getConsoleSender().sendMessage(PREFIX_CONSOLE + "§aLoaded in setup mode! (Most features are disabled!)");
            return;
        }

        players = new ArrayList<>();
        build = new ArrayList<>();
        toRemove = new ArrayList<>();
        stars = new HashMap<>();
        stopTask = null;

        MySQL minecraftPartyMySQL = HyperiorCore.getMySQLManager().getMinecraftPartyMySQL();

        try {

            MySQLConnection connection = minecraftPartyMySQL.getMySQLConnection();
            PreparedStatement ps01 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS woolblock INT(100)");
            PreparedStatement ps02 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS trafficlightrace INT(100)");
            PreparedStatement ps03 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS hotground INT(100)");
            PreparedStatement ps04 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS gungame INT(100)");
            PreparedStatement ps05 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS colorbattle INT(100)");
            PreparedStatement ps06 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS breakout INT(100)");
            PreparedStatement ps07 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS masterbuilders INT(100)");
            PreparedStatement ps08 = connection.getConnection()
                    .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS kingofthehill INT(100)");
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
            ps07.executeUpdate();
            ps07.close();
            ps08.executeUpdate();
            ps08.close();

            for (JumpAndRunLocations jumpAndRun : locationManager.JUMPANDRUN_LOCATIONS) {
                PreparedStatement ps = connection.getConnection()
                        .prepareStatement("ALTER TABLE records ADD COLUMN IF NOT EXISTS jumpandrun" + jumpAndRun.getName() + " INT(100)");
                ps.executeUpdate();
                ps.close();
            }

            connection.finish();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        gameStateManager = new GameStateManager(this);
        gameStateManager.setGameState(GameStates.LOBBY_STATE);

        record = new Record(this);
        stats = new Stats(this);
        gameOverview = new GameOverview(this);

        Top10 top10 = new Top10(this);
        top10.set();

        init(Bukkit.getPluginManager());

        client = new MinecraftPartyClient(this, "localhost", 4444,
                new DataPackage("LOGIN", "MinecraftParty", Integer.parseInt(Bukkit.getServerName())));

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
        pluginManager.registerEvents(new MasterBuilderListener(this), this);
        pluginManager.registerEvents(new KingOfTheHillListener(this), this);
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

    public MinecraftPartyClient getClient() {
        return client;
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
