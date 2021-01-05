package de.groodian.minecraftparty.gamestates;

import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class MasterBuildersState extends MiniGame {

    private final int SIZE;

    public MasterBuildersState(String name, Main plugin) {
        super(name, plugin);
        SIZE = MainConfig.getInt("MasterBuilders.size");
    }

    @Override
    protected void prepare() {
        int picturesAmount = getPicturesAmount();
        Bukkit.getConsoleSender().sendMessage("picturesAmount: " + picturesAmount);
    }

    @Override
    protected void beforeCountdownStart() {

    }

    @Override
    protected void startMiniGame() {

    }

    private int getPicturesAmount() {
        int amount = 0;
        while (!isPictureEmpty(getPicture(amount))) {
            amount++;
        }
        return amount;
    }

    private Material[][] getPicture(int pos) {
        Location loc = plugin.getLocationManager().MASTERBUILDERS_PICTURES;
        int locX = (int) plugin.getLocationManager().MASTERBUILDERS_PICTURES.getX();
        int locY = (int) plugin.getLocationManager().MASTERBUILDERS_PICTURES.getY();

        Material[][] picture = new Material[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                picture[x][y] = loc.clone().add(x, y, 2 * pos).getBlock().getType();
            }
        }

        return picture;
    }

    private boolean isPictureEmpty(Material[][] picture) {
        for (Material[] pictureLine : picture) {
            for (Material material : pictureLine) {
                if (material != Material.AIR) {
                    return false;
                }
            }
        }
        return true;
    }

}
