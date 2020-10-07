package de.groodian.minecraftparty.network;

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

    @Override
    protected void onSuccessfulLogin() {
        sendUpdate();
    }

    public void sendUpdate() {
        sendMessage(new DataPackage("SERVICE_INFO", plugin.getGameStateManager().getCurrentGameStateName(), plugin.getPlayers().size(), Main.MAX_PLAYERS));
    }

}
