package de.groodian.minecraftparty.gamestates;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MasterBuildersState extends MiniGame {

    private final int SIZE;

    private Random random;
    private int picturesAmount;

    public MasterBuildersState(String name, Main plugin) {
        super(name, plugin);
        SIZE = MainConfig.getInt("MasterBuilders.size");
        random = new Random();
    }

    @Override
    protected void prepare() {
        BlockData[][] picture = new BlockData[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                picture[x][y] = new BlockData(0, (byte) 0);
            }
        }
        for (Location loc : plugin.getLocationManager().MASTERBUILDERS_PLAYER) {
            setReplicaPicture(picture, loc);
            setDisplayPicture(picture, loc);
        }
    }

    @Override
    protected void beforeCountdownStart() {
        picturesAmount = getPicturesAmount();
    }

    @Override
    protected void startMiniGame() {
        new BukkitRunnable() {
            @Override
            public void run() {
                BlockData[][] picture = getPicture(random.nextInt(picturesAmount));

                for (Location loc : plugin.getLocationManager().MASTERBUILDERS_PLAYER) {
                    setDisplayPicture(picture, loc);
                }
            }
        }.runTaskTimer(plugin, 0, 40);
    }

    private BlockData[][] getReplicaPicture(Location loc) {
        return getPicture(loc.clone().add(-2, 1, -5));
    }

    private void setReplicaPicture(BlockData[][] picture, Location loc) {
        setPicture(picture, loc.clone().add(-2, 1, -5));
    }

    private void setDisplayPicture(BlockData[][] picture, Location loc) {
        setPicture(picture, loc.clone().add(-2, 1, 5));
    }

    private void setPicture(BlockData[][] picture, Location loc) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                setBlockInNativeWorld(loc.clone().add(x, y, 0), picture[x][y]);
            }
        }
    }

    private int getPicturesAmount() {
        int amount = 0;
        while (!isPictureEmpty(getPicture(amount))) {
            amount++;
        }
        return amount;
    }

    private BlockData[][] getPicture(int pos) {
        return getPicture(plugin.getLocationManager().MASTERBUILDERS_PICTURES.clone().add(0, 0, 2 * pos));
    }

    private BlockData[][] getPicture(Location loc) {
        BlockData[][] picture = new BlockData[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Block block = loc.clone().add(x, y, 0).getBlock();
                picture[x][y] = new BlockData(block.getTypeId(), block.getData());
            }
        }
        return picture;
    }

    private boolean isPictureEmpty(BlockData[][] picture) {
        for (BlockData[] pictureLine : picture) {
            for (BlockData blockData : pictureLine) {
                if (blockData.id != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setBlockInNativeWorld(Location loc, BlockData blockData) {
        BlockPosition bp = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockData.id + (blockData.data << 12));
        ((CraftWorld) loc.getWorld()).getHandle().setTypeAndData(bp, ibd, 3);
    }

    private static class BlockData {

        private int id;
        private byte data;

        public BlockData(int id, byte data) {
            this.id = id;
            this.data = data;
        }

    }

}
