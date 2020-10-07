package de.groodian.minecraftparty.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {

	private static FileConfiguration config;

	public static void loadConfigs(Main plugin) {
		try {
			// german
			File germanFile = new File("plugins/HyperiorMinecraftParty_by_Groodian/messages", "german.yml");
			FileConfiguration germanConfig = YamlConfiguration.loadConfiguration(germanFile);
			germanConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("german.yml"))));
			germanConfig.options().copyDefaults(true);
			germanConfig.save(germanFile);
			// english
			File englishFile = new File("plugins/HyperiorMinecraftParty_by_Groodian/messages", "english.yml");
			FileConfiguration englishConfig = YamlConfiguration.loadConfiguration(englishFile);
			englishConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("english.yml"))));
			englishConfig.options().copyDefaults(true);
			englishConfig.save(englishFile);

			String language = MainConfig.getString("language");
			if (language.equalsIgnoreCase("english")) {
				config = YamlConfiguration.loadConfiguration(new File("plugins/HyperiorMinecraftParty_by_Groodian/messages", "english.yml"));
			} else if (language.equalsIgnoreCase("german")) {
				config = YamlConfiguration.loadConfiguration(new File("plugins/HyperiorMinecraftParty_by_Groodian/messages", "german.yml"));
			} else {
				Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cUnknown language, using English instead!");
				config = YamlConfiguration.loadConfiguration(new File("plugins/HyperiorMinecraftParty_by_Groodian/messages", "english.yml"));
			}

			Main.PREFIX = get("prefix");
			Main.NO_PERMISSION = Main.PREFIX + get("no-permission");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String get(String name) {
		return ChatColor.translateAlternateColorCodes('&', config.getString(name));
	}

	public static List<String> getStringList(String name) {
		List<String> list = config.getStringList(name);
		List<String> listReplaced = new ArrayList<>();
		for (String string : list) {
			string = ChatColor.translateAlternateColorCodes('&', string);
			listReplaced.add(string);
		}
		return listReplaced;
	}

}
