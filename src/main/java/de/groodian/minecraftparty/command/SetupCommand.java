package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.command.HArgument;
import de.groodian.hyperiorcore.command.HCommand;
import de.groodian.hyperiorcore.command.HCommandPaper;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class SetupCommand extends HCommandPaper<Player> {

    public SetupCommand(Main plugin) {
        super(Player.class, "mpsetup", "Setting up the game", Main.PREFIX, "minecraftparty.setup",
                List.of(new SetLocation(plugin, "lobby"),
                        new SetTopLocation(plugin, "woolblock", List.of("start", "field", "spectator")),
                        new SetTopLocation(plugin, "trafficlightrace", List.of("start", "win"),
                                "Must be a straight street in negative x direction!", List.of()),
                        new SetLocation(plugin, "hotground"),
                        new SetTopLocation(plugin, "colorbattle", List.of("start", "location1", "location2")),
                        new SetMultipleLocation(plugin, "gungame", MainConfig.getInt("GunGame.respawn-points")),
                        new SetMultipleLocation(plugin, "kingofthehill", MainConfig.getInt("KingOfTheHill.respawn-points")),
                        new SetLocation(plugin, "top10"),
                        new SetTopLocation(plugin, "breakout", List.of("spectator"), "",
                                List.of(new SetMultipleLocation(plugin, "player", Main.MAX_PLAYERS))),
                        new SetTopLocation(plugin, "masterbuilders", List.of("pictures"),
                                "From this point is takes 5x5x1 pictures in positive x and y direction! And multiple pictures in positive z direction with 1 block space between pictures!",
                                List.of(new SetMultipleLocation(plugin, "player", Main.MAX_PLAYERS))),
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
            this(plugin, locationName, setLocationCommands, "", List.of());
        }

        public SetTopLocation(Main plugin, String locationName, List<String> setLocationCommands, String extraDescription,
                              List<HCommand<Player, de.groodian.hyperiorcore.main.Main>> hSubCommands) {
            super(Player.class, locationName, "Set the locations for '" + locationName + "' " + extraDescription, Main.PREFIX, null,
                    constructSetLocationCommands(plugin, locationName, hSubCommands, setLocationCommands), List.of());
        }

        @Override
        public void onCall(Player player, String[] args) {
        }

    }

    private static class SetMultipleLocation extends HCommandPaper<Player> {

        private final Main plugin;
        protected int maxNumber;
        protected String locationName;

        public SetMultipleLocation(Main plugin, String locationName, int maxNumber) {
            this(plugin, locationName, maxNumber, "", List.of());
        }

        public SetMultipleLocation(Main plugin, String locationName, int maxNumber, String extraDescription,
                                   List<HArgument> extraArguments) {
            super(Player.class, locationName, "Set the locations 1-" + maxNumber + " for '" + locationName + "' " + extraDescription,
                    Main.PREFIX, null, List.of(),
                    Stream.concat(extraArguments.stream(), Arrays.stream(new HArgument[]{new HArgument("1-" + maxNumber)}))
                            .collect(Collectors.toList()));
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
            super(Player.class, "jumpandrun", "Setup jump and run", Main.PREFIX, null,
                    List.of(new Create(plugin), new StartWin(plugin, "start"), new StartWin(plugin, "win"), new Checkpoint(plugin)),
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

                plugin.getLocationManager().saveLocation("jumpandrun." + args[0] + "." + name, player.getLocation());
                sendMsg(player, "The location 'jumpandrun." + args[0] + "." + name + "' was set.", NamedTextColor.GREEN);
            }

        }

        private static class Checkpoint extends SetMultipleLocation {

            private final Main plugin;

            public Checkpoint(Main plugin) {
                super(plugin, "checkpoint", 0, "", List.of(new HArgument("name")));
                this.plugin = plugin;
            }

            @Override
            public void onCall(Player player, String[] args) {
                if (!plugin.getLocationManager().existsJumpAndRun(args[0])) {
                    sendMsg(player, Component.text("The Jump and Run " + args[0] + " does not exists.", NamedTextColor.RED));
                    return;
                }

                maxNumber = plugin.getLocationManager().getJumpAndRunCheckpoints(args[0]);

                super.onCall(player, args);
            }

        }

    }

    public static List<HCommand<Player, de.groodian.hyperiorcore.main.Main>> constructSetLocationCommands(
            Main plugin,
            String locationName,
            List<HCommand<Player, de.groodian.hyperiorcore.main.Main>> commands,
            List<String> setLocationCommands) {
        for (String setLocationCommand : setLocationCommands) {
            commands.add(new SetLocation(plugin, locationName + setLocationCommand));
        }

        return commands;
    }

}
