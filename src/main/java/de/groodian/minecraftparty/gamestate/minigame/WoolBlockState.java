package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.util.ItemBuilder;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WoolBlockState extends MiniGame {

    private final int TIME_BETWEEN_ROUNDS;
    private final int REMOVE_START_TIME;
    private final int TIME_GETS_FASTER_EVERY_ROUND;
    private final int MIN_REMOVE_TIME;
    private final Location start;
    private final Location field;
    private final Location spectator;
    private final Map<Material, Component> wools;
    private final Random random;

    private int mode;
    private int currentTime;
    private int removeTime;
    private int levelCounter;
    private int placeCount;
    private int lastPlayerDiePlaceCount;
    private boolean delayIsRunning;
    private boolean generate;
    private Map.Entry<Material, Component> currentWool;
    private BukkitTask gameTask;

    public WoolBlockState(String name, Main plugin) {
        super(name, plugin);
        super.timeScoreboardGame = false;
        super.setRecords = false;
        this.TIME_BETWEEN_ROUNDS = MainConfig.getInt("WoolBlock.time-between-rounds");
        this.REMOVE_START_TIME = MainConfig.getInt("WoolBlock.remove-start-time");
        this.TIME_GETS_FASTER_EVERY_ROUND = MainConfig.getInt("WoolBlock.time-gets-faster-every-round");
        this.MIN_REMOVE_TIME = MainConfig.getInt("WoolBlock.min-remove-time");
        this.start = plugin.getLocationManager().WOOLBLOCK_START;
        this.field = plugin.getLocationManager().WOOLBLOCK_FIELD;
        this.spectator = plugin.getLocationManager().WOOLBLOCK_SPECTATOR;
        this.mode = 0;
        this.currentTime = 0;
        this.removeTime = REMOVE_START_TIME;
        this.levelCounter = 0;
        this.placeCount = -1;
        this.delayIsRunning = false;
        this.generate = true;
        this.wools = new HashMap<>();
        this.random = new Random();
        this.gameTask = null;

        wools.put(Material.ORANGE_WOOL, Messages.get("WoolBlock.orange"));
        wools.put(Material.LIGHT_BLUE_WOOL, Messages.get("WoolBlock.light-blue"));
        wools.put(Material.YELLOW_WOOL, Messages.get("WoolBlock.yellow"));
        wools.put(Material.GREEN_WOOL, Messages.get("WoolBlock.green"));
        wools.put(Material.PINK_WOOL, Messages.get("WoolBlock.pink"));
        wools.put(Material.BLUE_WOOL, Messages.get("WoolBlock.blue"));
        wools.put(Material.RED_WOOL, Messages.get("WoolBlock.red"));
    }

    @Override
    protected void prepare() {
        Location temp = field.clone();
        int x = (int) temp.getX();
        int z = (int) temp.getZ();

        for (int i = 0; i < 30; i++) {
            temp.setX(i + x);
            for (int j = 0; j < 30; j++) {
                temp.setZ(j + z);
                temp.getBlock().setType(Material.WHITE_WOOL);
            }
        }

    }

    @Override
    protected void beforeCountdownStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportManager.addTeleport(player, start, null);
        }
    }

    @Override
    protected void startMiniGame() {

        gameTask = new BukkitRunnable() {

            @Override
            public void run() {

                levelCounter++;
                float exp = (1.0f / currentTime) * levelCounter;

                if (exp > 1f) {
                    exp = 1f;
                }

                if (!delayIsRunning) {
                    delayIsRunning = true;
                    delay();
                    levelCounter = 0;
                }

                for (Player player : plugin.getPlayers()) {
                    player.setExp(exp);
                    if (!diePlayers.contains(player)) {
                        ranking.put(player, placeCount);
                        if (player.getLocation().getY() <= field.getY()) {
                            lastPlayerDiePlaceCount = placeCount;
                            addDiePlayer(player);
                            plugin.getStats().record(player, "woolblock", placeCount, true);
                            player.teleport(spectator);
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            for (Player all : Bukkit.getOnlinePlayers()) {
                                all.hidePlayer(plugin, player);
                            }
                        }
                    }
                }

                if (((plugin.getPlayers().size() == diePlayers.size() + 1) && placeCount != lastPlayerDiePlaceCount) ||
                    plugin.getPlayers().size() == 1 || diePlayers.size() == plugin.getPlayers().size()) {

                    for (Player player : plugin.getPlayers()) {
                        if (!diePlayers.contains(player)) {
                            plugin.getStats().record(player, "woolblock", placeCount, true);
                        }
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (Player player1 : plugin.getPlayers()) {
                            player.showPlayer(plugin, player1);
                        }
                    }

                    for (Player player : plugin.getPlayers()) {
                        player.setExp(0.0f);
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }

                    gameTask.cancel();
                    plugin.getGameStateManager().setRandomGameState();

                }

            }

        }.runTaskTimer(plugin, 0, 1);

    }

    private void delay() {

        new BukkitRunnable() {

            @Override
            public void run() {
                if (started) {

                    if (generate) {

                        currentWool = getRandomWool();
                        generateWool(currentWool.getKey());

                        currentTime = removeTime;

                        for (Player all : plugin.getPlayers()) {
                            for (int i = 0; i < 9; i++) {
                                all.getInventory()
                                        .setItem(i, new ItemBuilder(currentWool.getKey()).setName(currentWool.getValue()).build());
                            }
                        }

                        generate = false;
                    } else {

                        for (int i = 0; i < 30; i++) {
                            for (int j = 0; j < 30; j++) {
                                Block block = field.getWorld().getBlockAt(field.getBlockX() + i, field.getBlockY(), field.getBlockZ() + j);
                                if (block.getType() != currentWool.getKey()) {
                                    block.setType(Material.WATER);
                                }
                            }
                        }

                        removeTime -= TIME_GETS_FASTER_EVERY_ROUND;
                        if (removeTime < MIN_REMOVE_TIME) {
                            removeTime = MIN_REMOVE_TIME;
                        }
                        currentTime = TIME_BETWEEN_ROUNDS;

                        generate = true;
                    }

                    delayIsRunning = false;

                }
            }
        }.runTaskLater(plugin, currentTime);

    }

    private void generateWool(Material material) {
        int count = 0;

        placeCount++;

        do {

            count = 1;

            if (mode % 4 == 0) {
                randomWool();
            } else if (mode % 4 == 1) {
                lineWool();
            } else if (mode % 4 == 2) {
                threeXThreeWool();
            } else {
                twoXTwoWool();
            }

            for (int i = 0; i < 30; i++) {
                for (int j = 0; j < 30; j++) {
                    if (field.getWorld().getBlockAt(field.getBlockX() + i, field.getBlockY(), field.getBlockZ() + j).getType() ==
                        material) {
                        count++;
                    }
                }
            }

        } while (count < 91);

        mode++;

    }

    private void randomWool() {
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                Material wool = getRandomWool().getKey();
                setBlock(field.getWorld(), field.getBlockX() + i, field.getBlockY(), field.getBlockZ() + j, wool);
            }
        }

    }

    private void lineWool() {
        int count = 1;
        Material wool = getRandomWool().getKey();

        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                if (count == 1) {
                    wool = getRandomWool().getKey();
                }
                setBlock(field.getWorld(), field.getBlockX() + i, field.getBlockY(), field.getBlockZ() + j, wool);
                if (count >= 30) {
                    count = 0;
                }
                count++;
            }
        }

    }

    private void threeXThreeWool() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Material wool = getRandomWool().getKey();
                for (int o = 0; o < 3; o++) {
                    for (int m = 0; m < 3; m++) {
                        setBlock(field.getWorld(), o + j * 3 + field.getBlockX(), field.getBlockY(), m + i * 3 + field.getBlockZ(), wool);
                    }
                }
            }
        }

    }

    private void twoXTwoWool() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Material wool = getRandomWool().getKey();
                for (int o = 0; o < 2; o++) {
                    for (int m = 0; m < 2; m++) {
                        setBlock(field.getWorld(), o + j * 2 + field.getBlockX(), field.getBlockY(), m + i * 2 + field.getBlockZ(), wool);
                    }
                }
            }
        }

    }

    private void setBlock(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
    }

    private Map.Entry<Material, Component> getRandomWool() {
        int i = 0;
        int item = random.nextInt(wools.size());
        for (Map.Entry<Material, Component> entry : wools.entrySet()) {
            if (i == item) {
                return entry;
            }
            i++;
        }

        return wools.entrySet().stream().findFirst().orElseThrow();
    }

}
