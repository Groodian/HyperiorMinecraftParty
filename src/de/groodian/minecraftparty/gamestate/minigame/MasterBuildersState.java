package de.groodian.minecraftparty.gamestate.minigame;

import de.groodian.hyperiorcore.util.HSound;
import de.groodian.minecraftparty.gamestate.MiniGame;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import de.groodian.minecraftparty.main.Messages;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.material.Stairs;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterBuildersState extends MiniGame {

    private static final int ROUNDS = 4;

    private final int SIZE;

    private List<Integer> availablePicturePositions;
    private int picturePositionCounter;
    private int roundsCounter;
    private boolean inBuildMode;
    private BukkitTask gameTask;

    private Map<Player, Location> playerPictureLoc;
    private Block[][] currentPicture;

    public MasterBuildersState(String name, Main plugin) {
        super(name, plugin);
        SIZE = MainConfig.getInt("MasterBuilders.size");
        availablePicturePositions = new ArrayList<>();
        picturePositionCounter = 0;
        inBuildMode = false;
        playerPictureLoc = new HashMap<>();
    }

    @Override
    protected void prepare() {
        for (Location location : plugin.getLocationManager().MASTERBUILDERS_PLAYER) {
            cleanReplicaPicture(location);
            cleanDisplayPicture(location);
        }
    }

    @Override
    protected void beforeCountdownStart() {
        int count = 0;
        while (!isPictureEmpty(getPicture(count))) {
            availablePicturePositions.add(count++);
        }
        Collections.shuffle(availablePicturePositions);

        count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = plugin.getLocationManager().MASTERBUILDERS_PLAYER.get(count);
            addPlayerToTeleport(player, loc.clone().add(0, 1, 0));
            if (plugin.getPlayers().contains(player)) {
                playerPictureLoc.put(player, loc);
                player.setGameMode(GameMode.CREATIVE);
                ranking.put(player, 0);
                count++;
            }
        }
    }

    @Override
    protected void startMiniGame() {
        inBuildMode = true;
        showNewPicture();

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsGame <= 0 || plugin.getPlayers().size() == 1 || !inBuildMode) {
                    gameTask.cancel();
                    for (Player player : plugin.getPlayers()) {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.getInventory().clear();
                    }
                    plugin.getGameStateManager().setRandomGameState();
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    private void roundFinished(Player winner) {
        new HSound(Sound.LEVEL_UP).play();
        Bukkit.broadcastMessage(Main.PREFIX + Messages.get("MasterBuilders.winner-message").replace("%player%", winner.getName()).replace("%round%", roundsCounter + "").replace("%max-rounds%", ROUNDS + ""));

        for (Map.Entry<Player, Location> entry : playerPictureLoc.entrySet()) {
            float percentage = getPercentage(currentPicture, getReplicaPicture(entry.getValue()));
            int points = (int) (percentage / 10);

            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setMaximumFractionDigits(2);
            decimalFormat.setMinimumFractionDigits(2);
            entry.getKey().sendMessage(Main.PREFIX + Messages.get("MasterBuilders.round-info").replace("%points%", points + "").replace("%percentage%", decimalFormat.format(percentage)));

            if (ranking.containsKey(entry.getKey())) {
                ranking.put(entry.getKey(), points + ranking.get(entry.getKey()));
            }
        }
        showNewPicture();
    }

    public void pictureModified(Player player) {
        float percentage = getPercentage(currentPicture, getReplicaPicture(playerPictureLoc.get(player)));
        if (percentage == 100.0f) {
            roundFinished(player);
        }
    }

    private void showNewPicture() {
        if (roundsCounter < ROUNDS) {
            currentPicture = getPicture(availablePicturePositions.get(picturePositionCounter++));
            if (picturePositionCounter >= availablePicturePositions.size()) {
                picturePositionCounter = 0;
            }
            for (Location location : plugin.getLocationManager().MASTERBUILDERS_PLAYER) {
                cleanReplicaPicture(location);
                setDisplayPicture(currentPicture, location);
            }
            for (Player player : plugin.getPlayers()) {
                player.getInventory().clear();
            }
            roundsCounter++;
        } else {
            inBuildMode = false;
        }
    }

    private float getPercentage(Block[][] display, Block[][] replica) {
        int count = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (compareBlocks(display[x][y], replica[x][y])) {
                    count++;
                }
            }
        }

        if (count == 0) {
            return 0.0f;
        } else {
            return ((float) count / (SIZE * SIZE)) * 100;
        }
    }

    private boolean compareBlocks(Block display, Block replica) {
        if (display.getType() == replica.getType()) {
            if (display.getState().getData() instanceof Stairs) {
                return true;
            } else if (display.getState().getData() instanceof Door) {
                return true;
            } else if (display.getType() == Material.LEAVES || display.getType() == Material.LEAVES_2) {
                return true;
            } else {
                return display.getData() == replica.getData();
            }
        }
        return false;
    }

    public boolean isBlockInReplicaPicture(Player player, Block block) {
        Block[][] picture = getReplicaPicture(playerPictureLoc.get(player));
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (picture[x][y].getLocation().equals(block.getLocation())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Block[][] getReplicaPicture(Location loc) {
        return getPicture(loc.clone().add(-2, 1, -5));
    }

    private void setReplicaPicture(Block[][] picture, Location loc) {
        setPicture(picture, loc.clone().add(-2, 1, -5));
    }

    private Block[][] getDisplayPicture(Location loc) {
        return getPicture(loc.clone().add(-2, 1, 5));
    }

    private void setDisplayPicture(Block[][] picture, Location loc) {
        setPicture(picture, loc.clone().add(-2, 1, 5));
    }

    private void setPicture(Block[][] picture, Location loc) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                setBlockInNativeWorld(loc.clone().add(x, y, 0), picture[x][y]);
            }
        }
        // Cleaning dropped items (flowers, doors)
        for (Entity current : loc.getWorld().getEntities()) {
            if (current.getType() == EntityType.DROPPED_ITEM) {
                current.remove();
            }
        }
    }

    private void cleanReplicaPicture(Location loc) {
        cleanPicture(loc.clone().add(-2, 1, -5));
    }

    private void cleanDisplayPicture(Location loc) {
        cleanPicture(loc.clone().add(-2, 1, 5));
    }

    private void cleanPicture(Location loc) {
        for (int x = SIZE - 1; x >= 0; x--) {
            for (int y = SIZE - 1; y >= 0; y--) {
                setBlockInNativeWorld(loc.clone().add(x, y, 0), null);
            }
        }
    }

    private Block[][] getPicture(int pos) {
        return getPicture(plugin.getLocationManager().MASTERBUILDERS_PICTURES.clone().add(0, 0, 2 * pos));
    }

    private Block[][] getPicture(Location loc) {
        Block[][] picture = new Block[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Block block = loc.clone().add(x, y, 0).getBlock();
                picture[x][y] = block;
            }
        }
        return picture;
    }

    private boolean isPictureEmpty(Block[][] picture) {
        for (Block[] pictureLine : picture) {
            for (Block block : pictureLine) {
                if (block.getType() != Material.AIR) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setBlockInNativeWorld(Location loc, Block block) {
        BlockPosition bp = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        int id = block != null ? block.getTypeId() : 0;
        byte data = block != null ? block.getData() : (byte) 0;
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(id + (data << 12));
        ((CraftWorld) loc.getWorld()).getHandle().setTypeAndData(bp, ibd, 3);
    }

    public boolean isInBuildMode() {
        return inBuildMode;
    }

}
