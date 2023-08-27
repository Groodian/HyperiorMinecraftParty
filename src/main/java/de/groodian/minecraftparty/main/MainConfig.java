package de.groodian.minecraftparty.main;

import de.groodian.hyperiorcore.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class MainConfig {

    private static FileConfiguration config;

    public static void loadConfig(Main plugin) {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public static String getString(String name) {
        return config.getString(name);
    }

    public static int getInt(String name) {
        return config.getInt(name);
    }

    public static boolean getBoolean(String name) {
        return config.getBoolean(name);
    }

    public static ItemStack getItemWithName(String name) {
        return new ItemBuilder(Material.valueOf(config.getString(name))).setName(Messages.get(name)).build();
    }

    public static List<String> getStringList(String name) {
        return config.getStringList(name);
    }

    public static List<Material> getMaterialList(String name) {
        List<Material> materials = new ArrayList<>();
        for (String material : config.getStringList(name)) {
            materials.add(Material.valueOf(material));
        }
        return materials;
    }

    public static List<ItemStack> getItemWithNameList(String name) {
        List<Material> materials = getMaterialList(name);
        List<Component> names = Messages.getStringList(name);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < materials.size(); i++) {
            items.add(new ItemBuilder(materials.get(i)).setName(names.get(i)).build());
        }
        return items;
    }

}
