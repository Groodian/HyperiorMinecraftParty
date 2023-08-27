package de.groodian.minecraftparty.util;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationManager {

    public static final String DATA_PATH = "plugins/HyperiorMinecraftParty/data";

    // Lobby
    public Location LOBBY;
    public Location TOP10;

    // ColorBattle
    public Location COLORBATTLE_START;
    public Location COLORBATTLE_LOCATION_1;
    public Location COLORBATTLE_LOCATION_2;

    // GunGame
    public List<Location> GUNGAME;

    // HotGround
    public Location HOTGROUND;

    // JumpAndRun
    public List<JumpAndRunLocations> JUMPANDRUN_LOCATIONS;

    // TrafficLightRace
    public Location TRAFFICLIGHTRACE_START;
    public Location TRAFFICLIGHTRACE_WIN;

    // WoolBlock
    public Location WOOLBLOCK_START;
    public Location WOOLBLOCK_SPECTATOR;
    public Location WOOLBLOCK_FIELD;

    // Breakout
    public List<Location> BREAKOUT_PLAYERS;
    public Location BREAKOUT_SPECTATOR;

    // MasterBuilders
    public List<Location> MASTERBUILDERS_PLAYER;
    public Location MASTERBUILDERS_PICTURES;

    // KingOfTheHill
    public List<Location> KINGOFTHEHILL;

    private Main plugin;
    private File fileLocations;
    private FileConfiguration configLocations;
    private File fileJumpAndRuns;
    private FileConfiguration configJumpAndRuns;
    private String missingLocations;

    public LocationManager(Main plugin) {
        this.plugin = plugin;

        try {
            // locations
            this.fileLocations = new File(DATA_PATH, "locations.yml");
            this.configLocations = YamlConfiguration.loadConfiguration(fileLocations);
            configLocations.save(fileLocations);

            // jump and runs
            this.fileJumpAndRuns = new File(DATA_PATH, "jumpandruns.yml");
            this.configJumpAndRuns = YamlConfiguration.loadConfiguration(fileJumpAndRuns);
            configJumpAndRuns.save(fileJumpAndRuns);

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.missingLocations = null;
    }

    public boolean allLocationsSet() {

        // Lobby
        LOBBY = loadLocation("Lobby");
        TOP10 = loadLocation("Top10");

        // ColorBattle
        COLORBATTLE_START = loadLocation("ColorBattleStart");
        COLORBATTLE_LOCATION_1 = loadLocation("ColorBattleLocation1");
        COLORBATTLE_LOCATION_2 = loadLocation("ColorBattleLocation2");

        // GunGame
        GUNGAME = loadLocations("GunGame", MainConfig.getInt("GunGame.respawn-points"));

        // HotGround
        HOTGROUND = loadLocation("HotGround");

        // JumpAndRun
        JUMPANDRUN_LOCATIONS = loadJumpAndRuns();

        // TrafficLightRace
        TRAFFICLIGHTRACE_START = loadLocation("TrafficLightRaceStart");
        TRAFFICLIGHTRACE_WIN = loadLocation("TrafficLightRaceWin");

        // WoolBlock
        WOOLBLOCK_START = loadLocation("WoolBlockStart");
        WOOLBLOCK_SPECTATOR = loadLocation("WoolBlockSpectator");
        WOOLBLOCK_FIELD = loadLocation("WoolBlockField");

        // Breakout
        BREAKOUT_PLAYERS = loadLocations("BreakoutPlayer", Main.MAX_PLAYERS);
        BREAKOUT_SPECTATOR = loadLocation("BreakoutSpectator");

        // MasterBuilders
        MASTERBUILDERS_PLAYER = loadLocations("MasterBuildersPlayer", Main.MAX_PLAYERS);
        MASTERBUILDERS_PICTURES = loadLocation("MasterBuildersPictures");

        // KingOfTheHill
        KINGOFTHEHILL = loadLocations("KingOfTheHill", MainConfig.getInt("KingOfTheHill.respawn-points"));

        return missingLocations == null;

    }

    public void saveLocation(String name, Location location) {
        try {
            configLocations.set(name + ".World", location.getWorld().getName());
            configLocations.set(name + ".X", location.getX());
            configLocations.set(name + ".Y", location.getY());
            configLocations.set(name + ".Z", location.getZ());
            configLocations.set(name + ".Yaw", location.getYaw());
            configLocations.set(name + ".Pitch", location.getPitch());
            configLocations.save(fileLocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Location loadLocation(String name) {
        if (configLocations.contains(name)) {
            World world = Bukkit.getWorld(configLocations.getString(name + ".World"));
            double x = configLocations.getDouble(name + ".X");
            double y = configLocations.getDouble(name + ".Y");
            double z = configLocations.getDouble(name + ".Z");
            float yaw = (float) configLocations.getDouble(name + ".Yaw");
            float pitch = (float) configLocations.getDouble(name + ".Pitch");
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            if (missingLocations == null) {
                missingLocations = name;
            } else {
                missingLocations += ", " + name;
            }
            return null;
        }
    }

    private List<Location> loadLocations(String name, int amount) {
        List<Location> locations = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            locations.add(loadLocation(name + i));
        }
        return locations;
    }

    public boolean existsJumpAndRun(String name) {
        return configJumpAndRuns.contains(name);
    }

    public void createJumpAndRun(String name, int checkpoints) {
        try {
            configJumpAndRuns.set(name + ".checkpoints", checkpoints);
            configJumpAndRuns.save(fileJumpAndRuns);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getJumpAndRunCheckpoints(String name) {
        return configJumpAndRuns.getInt(name + ".checkpoints");
    }

    private List<JumpAndRunLocations> loadJumpAndRuns() {
        List<JumpAndRunLocations> jumpAndRunsLocations = new ArrayList<>();
        ConfigurationSection configSection = configJumpAndRuns.getConfigurationSection("");
        Map<String, Object> jumpandruns = configSection.getValues(false);
        if (jumpandruns.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, Object> currentJumpAndRun : jumpandruns.entrySet()) {
            ConfigurationSection currentJumpAndRunSection = (ConfigurationSection) currentJumpAndRun.getValue();
            Location start = loadLocation("JumpAndRun." + currentJumpAndRun.getKey() + ".start");
            Location win = loadLocation("JumpAndRun." + currentJumpAndRun.getKey() + ".win");
            List<Location> checkpoints = new ArrayList<>();
            for (int i = 1; i <= currentJumpAndRunSection.getInt("checkpoints"); i++) {
                checkpoints.add(loadLocation("JumpAndRun." + currentJumpAndRun.getKey() + ".checkpoints." + i));
            }
            jumpAndRunsLocations.add(new JumpAndRunLocations(currentJumpAndRun.getKey(), start, win, checkpoints));
        }
        return jumpAndRunsLocations;
    }

    public String getMissingLocations() {
        return missingLocations;
    }

    public Location getRespawnLocation(List<Location> respawnLocations) {

        Collections.shuffle(respawnLocations);

        Map<Location, Double> minDistances = new HashMap<>();

        for (Location respawnLocation : respawnLocations) {
            double minDistance = Double.MAX_VALUE;
            for (Player player : plugin.getPlayers()) {
                double distance = respawnLocation.distance(player.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
            minDistances.put(respawnLocation, minDistance);

            if (minDistance > 20) {
                return respawnLocation;
            }
        }

        return Collections.max(minDistances.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

}
