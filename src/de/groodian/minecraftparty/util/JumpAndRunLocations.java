package de.groodian.minecraftparty.util;

import java.util.List;

import org.bukkit.Location;

public class JumpAndRunLocations {

	private String name;
	private Location start;
	private Location win;
	private List<Location> checkpoints;

	public JumpAndRunLocations(String name, Location start, Location win, List<Location> checkpoints) {
		this.name = name;
		this.start = start;
		this.win = win;
		this.checkpoints = checkpoints;
	}

	public String getName() {
		return name;
	}

	public Location getStart() {
		return start;
	}

	public Location getWin() {
		return win;
	}

	public List<Location> getCheckpoints() {
		return checkpoints;
	}

}
