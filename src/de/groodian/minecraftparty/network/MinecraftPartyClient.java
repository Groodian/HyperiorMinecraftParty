package de.groodian.minecraftparty.network;

import org.bukkit.Bukkit;

import de.groodian.minecraftparty.main.Main;
import de.groodian.network.Client;
import de.groodian.network.DataPackage;

public class MinecraftPartyClient extends Client {

	private Main plugin;

	public MinecraftPartyClient(Main plugin, String hostname, int port, DataPackage loginPack) {
		super(hostname, port, loginPack);
		this.plugin = plugin;
	}

	@Override
	protected void handleDataPackage(DataPackage dataPackage) {
		System.out.println(dataPackage);
	}

	public void sendUpdate() {
		sendMessage(new DataPackage("SERVER_INFO", plugin.getGameStateManager().getCurrentGameStateName(), Bukkit.getOnlinePlayers().size(), Main.MAX_PLAYERS));
	}

}
