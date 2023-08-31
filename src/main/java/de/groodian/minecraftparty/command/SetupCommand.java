package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.command.HArgument;
import de.groodian.hyperiorcore.command.HCommand;
import de.groodian.hyperiorcore.command.HCommandPaper;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class SetupCommand extends HCommandPaper<Player> {

    public SetupCommand(Main plugin) {
        super(Player.class, "mpsetup", "Setting up the game", Main.PREFIX, "minecraftparty.setup",
                List.of(new SetLocation(plugin, "Lobby"),
                        new SetTopLocation(plugin, "WoolBlock", List.of("Start", "Field", "Spectator")),
                        new SetTopLocation(plugin, "TrafficLightRace", List.of("Start", "Win"),
                                "Must be a straight street in negative x direction!", new ArrayList<>(List.of())),
                        new SetLocation(plugin, "HotGround"),
                        new SetTopLocation(plugin, "ColorBattle", List.of("Start", "Location1", "Location2")),
                        new SetMultipleLocation(plugin, "GunGame", MainConfig.getInt("GunGame.respawn-points")),
                        new SetMultipleLocation(plugin, "KingOfTheHill", MainConfig.getInt("KingOfTheHill.respawn-points")),
                        new SetLocation(plugin, "Top10"),
                        new SetTopLocation(plugin, "Breakout", List.of("Spectator"), "",
                                new ArrayList<>(List.of(new SetMultipleLocation(plugin, "BreakoutPlayer", Main.MAX_PLAYERS)))),
                        new SetTopLocation(plugin, "MasterBuilders", List.of("Pictures"),
                                "From this point is takes 5x5x1 pictures in positive x and y direction! And multiple pictures in positive z direction with 1 block space between pictures!",
                                new ArrayList<>(List.of(new SetMultipleLocation(plugin, "MasterBuildersPlayer", Main.MAX_PLAYERS)))),
                        new JumpAndRun(plugin)
                ), List.of());
    }

    @Override
    protected void onCall(Player player, String[] args) {
    }

    private static class SetLocation extends HCommandPaper<Player> {

        private final Main plugin;
        private final String locationName;

        public SetLocation(Main plugin, String locationName) {
            this(plugin, locationName, "");
        }

        public SetLocation(Main plugin, String locationName, String extraDescription) {
            super(Player.class, locationName, "Set the location '" + locationName + "' " + extraDescription, Main.PREFIX, null, List.of(),
                    List.of());
            this.plugin = plugin;
            this.locationName = locationName;
        }

        @Override
        public void onCall(Player player, String[] args) {
            plugin.getLocationManager().saveLocation(locationName, player.getLocation());
            sendMsg(player, "The location '" + locationName + "' was set.", NamedTextColor.GREEN);
        }

    }

    private static class SetTopLocation extends HCommandPaper<Player> {

        public SetTopLocation(Main plugin, String locationName, List<String> setLocationCommands) {
            this(plugin, locationName, setLocationCommands, "", new ArrayList<>());
        }

        public SetTopLocation(Main plugin, String locationName, List<String> setLocationCommands, String extraDescription,
                              ArrayList<HCommand<Player, de.groodian.hyperiorcore.main.Main>> hSubCommands) {
            super(Player.class, locationName, "Set the locations for '" + locationName + "'. " + extraDescription, Main.PREFIX, null,
                    constructSetLocationCommands(plugin, locationName, hSubCommands, setLocationCommands), List.of());
        }

        @Override
        public void onCall(Player player, String[] args) {
        }

    }

    private static class SetMultipleLocation extends HCommandPaper<Player> {

        private final Main plugin;
        protected final int maxNumber;
        protected final String locationName;

        public SetMultipleLocation(Main plugin, String locationName, int maxNumber) {
            this(plugin, locationName, maxNumber, "");
        }

        public SetMultipleLocation(Main plugin, String locationName, int maxNumber, String extraDescription) {
            super(Player.class, locationName,
                    "Set the locations 1-" + (maxNumber == 0 ? "?" : maxNumber) + " for '" + locationName + "'. " + extraDescription,
                    Main.PREFIX, null, List.of(), List.of(new HArgument("1-" + maxNumber)));
            this.plugin = plugin;
            this.maxNumber = maxNumber;
            this.locationName = locationName;
        }

        @Override
        public void onCall(Player player, String[] args) {
            int number;
            try {
                number = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sendMsg(player, Component.text("The argument 1-" + maxNumber + " has to be a number.", NamedTextColor.RED));
                return;
            }

            if (number <= 0) {
                sendMsg(player, Component.text("The number has to be higher than zero.", NamedTextColor.RED));
                return;
            }

            if (number > maxNumber) {
                sendMsg(player,
                        Component.text("The number is higher as the defined number. Max value is " + maxNumber + ".", NamedTextColor.RED));
                return;
            }

            plugin.getLocationManager().saveLocation(locationName + number, player.getLocation());
            sendMsg(player, "The location '" + locationName + number + "' was set.", NamedTextColor.GREEN);
        }

    }

    private static class JumpAndRun extends HCommandPaper<Player> {

        public JumpAndRun(Main plugin) {
            super(Player.class, "JumpAndRun", "Setup jump and run", Main.PREFIX, null,
                    List.of(new Create(plugin), new StartWin(plugin, "Start"), new StartWin(plugin, "Win"), new Checkpoint(plugin)),
                    List.of());
        }

        @Override
        public void onCall(Player player, String[] args) {
        }

        private static class Create extends HCommandPaper<Player> {

            private final Main plugin;

            public Create(Main plugin) {
                super(Player.class, "create", "Create a new jump and run", Main.PREFIX, null, List.of(),
                        List.of(new HArgument("name"), new HArgument("checkpoints")));
                this.plugin = plugin;
            }

            @Override
            protected void onCall(Player player, String[] args) {
                int checkpoints;
                try {
                    checkpoints = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendMsg(player, Component.text("The argument checkpoints has to be a number.", NamedTextColor.RED));
                    return;
                }

                if (plugin.getLocationManager().existsJumpAndRun(args[0])) {
                    sendMsg(player, Component.text("The Jump and Run " + args[0] + " already exists.", NamedTextColor.RED));
                    return;
                }

                plugin.getLocationManager().createJumpAndRun(args[0], checkpoints);
                sendMsg(player, Component.text("The Jump and Run " + args[0] + " was successfully created.", NamedTextColor.GREEN));
            }

        }

        private static class StartWin extends HCommandPaper<Player> {

            private final Main plugin;

            public StartWin(Main plugin, String name) {
                super(Player.class, name, "Set the " + name + " for a jump and run", Main.PREFIX, null, List.of(),
                        List.of(new HArgument("name")));
                this.plugin = plugin;
            }

            @Override
            protected void onCall(Player player, String[] args) {
                if (!plugin.getLocationManager().existsJumpAndRun(args[0])) {
                    sendMsg(player, Component.text("The Jump and Run " + args[0] + " does not exists.", NamedTextColor.RED));
                    return;
                }

                plugin.getLocationManager().saveLocation("JumpAndRun." + args[0] + "." + name, player.getLocation());
                sendMsg(player, "The location 'JumpAndRun." + args[0] + "." + name + "' was set.", NamedTextColor.GREEN);
            }

        }

        private static class Checkpoint extends HCommandPaper<Player> {

            private final Main plugin;

            public Checkpoint(Main plugin) {
                super(Player.class, "Checkpoint", "Set the checkpoints for a jump and run", Main.PREFIX, null, List.of(),
                        List.of(new HArgument("name"), new HArgument("1-?")));
                this.plugin = plugin;
            }

            @Override
            public void onCall(Player player, String[] args) {
                if (!plugin.getLocationManager().existsJumpAndRun(args[0])) {
                    sendMsg(player, Component.text("The Jump and Run " + args[0] + " does not exists.", NamedTextColor.RED));
                    return;
                }

                int maxNumber = plugin.getLocationManager().getJumpAndRunCheckpoints(args[0]);

                int number;
                try {
                    number = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendMsg(player, Component.text("The argument 1-" + maxNumber + " has to be a number.", NamedTextColor.RED));
                    return;
                }

                if (number <= 0) {
                    sendMsg(player, Component.text("The number has to be higher than zero.", NamedTextColor.RED));
                    return;
                }

                if (number > maxNumber) {
                    sendMsg(player,
                            Component.text("The number is higher as the defined number. Max value is " + maxNumber + ".",
                                    NamedTextColor.RED));
                    return;
                }

                plugin.getLocationManager().saveLocation("JumpAndRun." + args[0] + "." + name + number, player.getLocation());
                sendMsg(player, "The location 'JumpAndRun." + args[0] + "." + name + number + "' was set.", NamedTextColor.GREEN);
            }

        }

    }

    public static List<HCommand<Player, de.groodian.hyperiorcore.main.Main>> constructSetLocationCommands(
            Main plugin,
            String locationName,
            ArrayList<HCommand<Player, de.groodian.hyperiorcore.main.Main>> commands,
            List<String> setLocationCommands) {
        for (String setLocationCommand : setLocationCommands) {
            commands.add(new SetLocation(plugin, locationName + setLocationCommand));
        }

        return commands;
    }

}
