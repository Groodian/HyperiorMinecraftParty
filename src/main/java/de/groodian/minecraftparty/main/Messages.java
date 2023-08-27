package de.groodian.minecraftparty.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {

    private static final String MESSAGES_PATH = "plugins/HyperiorMinecraftParty/messages";

    private static FileConfiguration config;

    public static void loadConfigs(Main plugin) {
        try {
            // german
            File germanFile = new File(MESSAGES_PATH, "german.yml");
            FileConfiguration germanConfig = YamlConfiguration.loadConfiguration(germanFile);
            germanConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("german.yml"))));
            germanConfig.options().copyDefaults(true);
            germanConfig.save(germanFile);
            // english
            File englishFile = new File(MESSAGES_PATH, "english.yml");
            FileConfiguration englishConfig = YamlConfiguration.loadConfiguration(englishFile);
            englishConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("english.yml"))));
            englishConfig.options().copyDefaults(true);
            englishConfig.save(englishFile);

            String language = MainConfig.getString("language");
            if (language.equalsIgnoreCase("english")) {
                config = YamlConfiguration.loadConfiguration(new File(MESSAGES_PATH, "english.yml"));
            } else if (language.equalsIgnoreCase("german")) {
                config = YamlConfiguration.loadConfiguration(new File(MESSAGES_PATH, "german.yml"));
            } else {
                Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cUnknown language, using English instead!");
                config = YamlConfiguration.loadConfiguration(new File(MESSAGES_PATH, "english.yml"));
            }

            Main.PREFIX = get("prefix");
            Main.NO_PERMISSION = Main.PREFIX.append(get("no-permission"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Component get(String name) {
        String msg = config.getString(name);
        if (msg == null) {
            return Component.text(name, NamedTextColor.DARK_RED);
        } else {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        }
    }

    public static Component getWithReplace(String name, Map<String, String> replacements) {
        String msg = config.getString(name);
        if (msg == null) {
            return Component.text(name, NamedTextColor.DARK_RED);
        } else {
            for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                msg = msg.replace(replacement.getKey(), replacement.getValue());
            }
            return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        }
    }

    public static List<Component> getStringList(String name) {
        List<String> list = config.getStringList(name);
        List<Component> listReplaced = new ArrayList<>();

        for (String string : list) {
            listReplaced.add(LegacyComponentSerializer.legacyAmpersand().deserialize(string));
        }

        return listReplaced;
    }

}
