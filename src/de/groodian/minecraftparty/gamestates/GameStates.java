package de.groodian.minecraftparty.gamestates;

import java.security.SecureRandom;

public enum GameStates {

	LOBBY_STATE, 
	WOOLBLOCK_STATE, 
	JUMPANDRUN_STATE, 
	TRAFFICLIGHTRACE_STATE, 
	HOTGROUND_STATE, 
	GUNGAME_STATE, 
	COLORBATTLE_STATE, 
	BREAKOUT_STATE, 
	ENDING_STATE;

	private static SecureRandom random = new SecureRandom();

	public static GameStates getRandomGameState() {
		return values()[random.nextInt(values().length)];
	}

}
