package de.groodian.minecraftparty.util;

import java.util.List;
import org.bukkit.Location;

public class JumpAndRunLocations {

    private final String name;
    private final Location start;
    private final Location win;
    private final List<Location> checkpoints;

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
