package de.groodian.minecraftparty.command;

import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.MainConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    private Main plugin;

    private String USAGE =
            "§7§m=============§r §6MinecraftParty §7§m=============§r" +
                    "\n " +
                    "\n§6/mpsetup lobby §7- Set the lobby location" +
                    "\n§6/mpsetup woolblock §7- Setting up wool block" +
                    "\n§6/mpsetup jumpandrun §7- Setting up jump and run" +
                    "\n§6/mpsetup trafficlightrace §7- Setting up traffic light race" +
                    "\n§6/mpsetup hotground §7- Setting up hot ground" +
                    "\n§6/mpsetup gungame §7- Setting up gun game" +
                    "\n§6/mpsetup colorbattle §7- Setting up color battle" +
                    "\n§6/mpsetup breakout §7- Setting up breakout" +
                    "\n§6/mpsetup masterbuilders §7- Setting up master builders" +
                    "\n§6/mpsetup top10 §7- Set the top ten location" +
                    "\n " +
                    "\n§aMinecraftParty by Groodian" +
                    "\n§aVersion: " + Main.VERSION +
                    "\n " +
                    "\n§7§m=============§r §6MinecraftParty §7§m=============§r";

    public SetupCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (HyperiorCore.getRanks().has(player.getUniqueId(), "minecraftparty.setup")) {
                if (args.length == 0) {
                    player.sendMessage(USAGE);
                } else {

                    // lobby
                    if (args[0].equalsIgnoreCase("lobby")) {
                        if (args.length == 1) {
                            plugin.getLocationManager().saveLocation("Lobby", player.getLocation());
                            player.sendMessage(Main.PREFIX + "§aLobby-Location was set.");
                        } else
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup lobby");
                    }

                    // woolblock
                    else if (args[0].equalsIgnoreCase("woolblock")) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("start")) {
                                plugin.getLocationManager().saveLocation("WoolBlockStart", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aWoolBlockStart-Location was set.");
                            } else if (args[1].equalsIgnoreCase("field")) {
                                plugin.getLocationManager().saveLocation("WoolBlockField", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aWoolBlockField-Location was set.");
                            } else if (args[1].equalsIgnoreCase("spectator")) {
                                plugin.getLocationManager().saveLocation("WoolBlockSpectator", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aWoolBlockSpectator-Location was set.");
                            } else
                                player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup woolblock <start/field/spectator>");
                        } else
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup woolblock <start/field/spectator>");
                    }

                    // jumpandrun
                    else if (args[0].equalsIgnoreCase("jumpandrun")) {
                        if (args.length >= 2) {
                            if (args[1].equalsIgnoreCase("create")) {
                                if (args.length == 4) {
                                    if (!plugin.getLocationManager().existsJumpAndRun(args[2])) {
                                        if (args[3].chars().allMatch(Character::isDigit)) {
                                            plugin.getLocationManager().createJumpAndRun(args[2], Integer.parseInt(args[3]));
                                            player.sendMessage(Main.PREFIX + "§aThe Jump and Run " + args[2] + " was successfully created.");
                                        } else {
                                            player.sendMessage(Main.PREFIX + "§cThe parameter checkpoints has to be a number.");
                                        }
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe Jump and Run " + args[2] + " already exists.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup jumpandrun create <name> <checkpoints>");
                                }
                            } else if (args[1].equalsIgnoreCase("start")) {
                                if (args.length == 3) {
                                    if (plugin.getLocationManager().existsJumpAndRun(args[2])) {
                                        plugin.getLocationManager().saveLocation("JumpAndRun." + args[2] + ".start", player.getLocation());
                                        player.sendMessage(Main.PREFIX + "§aJumpAndRun." + args[2] + ".start-Location was set.");
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe Jump and Run " + args[2] + " not exists.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup jumpandrun start <name>");
                                }
                            } else if (args[1].equalsIgnoreCase("checkpoint")) {
                                if (args.length == 4) {
                                    if (plugin.getLocationManager().existsJumpAndRun(args[3])) {
                                        if (args[2].chars().allMatch(Character::isDigit)) {
                                            int number = Integer.parseInt(args[2]);
                                            if (number <= plugin.getLocationManager().getJumpAndRunCheckpoints(args[3])) {
                                                if (number > 0) {
                                                    plugin.getLocationManager().saveLocation("JumpAndRun." + args[3] + ".checkpoints." + number, player.getLocation());
                                                    player.sendMessage(Main.PREFIX + "§aJumpAndRun." + args[3] + ".checkpoints." + number + "-Location was set.");
                                                } else {
                                                    player.sendMessage(Main.PREFIX + "§cThe number is less than or equal to zero.");
                                                }
                                            } else {
                                                player.sendMessage(Main.PREFIX + "§cThe number is higher as the defined number of checkpoints.");
                                            }
                                        } else {
                                            player.sendMessage(Main.PREFIX + "§cThe parameter number has to be a number.");
                                        }
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe Jump and Run " + args[3] + " not exists.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup jumpandrun checkpoint <number> <name>");
                                }
                            } else if (args[1].equalsIgnoreCase("win")) {
                                if (args.length == 3) {
                                    if (plugin.getLocationManager().existsJumpAndRun(args[2])) {
                                        plugin.getLocationManager().saveLocation("JumpAndRun." + args[2] + ".win", player.getLocation());
                                        player.sendMessage(Main.PREFIX + "§aJumpAndRun." + args[2] + ".win-Location was set.");
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe Jump and Run " + args[2] + " not exists.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup jumpandrun win <name>");
                                }
                            } else {
                                player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup jumpandrun <create/start/checkpoint/win>");
                            }
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup jumpandrun <create/start/checkpoint/win>");
                        }
                    }

                    // trafficlightrace
                    else if (args[0].equalsIgnoreCase("trafficlightrace")) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("start")) {
                                plugin.getLocationManager().saveLocation("TrafficLightRaceStart", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aTrafficLightRaceStart-Location was set. §7(§cMust be a straight street in negative x direction!§7)");
                            } else if (args[1].equalsIgnoreCase("win")) {
                                plugin.getLocationManager().saveLocation("TrafficLightRaceWin", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aTrafficLightRaceWin-Location was set. §7(§cMust be a straight street in negative x direction!§7)");
                            } else {
                                player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup trafficlightrace <start/win> §7(§cMust be a straight street in negative x direction!§7)");
                            }
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup trafficlightrace <start/win> §7(§cMust be a straight street in negative x direction!§7)");
                        }
                    }

                    // hotground
                    else if (args[0].equalsIgnoreCase("hotground")) {
                        if (args.length == 1) {
                            plugin.getLocationManager().saveLocation("HotGround", player.getLocation());
                            player.sendMessage(Main.PREFIX + "§aHotGround-Location was set.");
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup hotground");
                        }
                    }

                    // gungame
                    else if (args[0].equalsIgnoreCase("gungame")) {
                        if (args.length == 2) {
                            if (args[1].chars().allMatch(Character::isDigit)) {
                                int number = Integer.parseInt(args[1]);
                                if (number <= MainConfig.getInt("GunGame.respawn-points")) {
                                    if (number > 0) {
                                        plugin.getLocationManager().saveLocation("GunGame" + number, player.getLocation());
                                        player.sendMessage(Main.PREFIX + "§aGunGame" + number + "-Location was set.");
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe number is less than or equal to zero.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cThe number is higher as the defined number of respawn points. Max value is " + MainConfig.getInt("GunGame.respawn-points") + ".");
                                }
                            } else {
                                player.sendMessage(Main.PREFIX + "§cThe parameter 1-" + MainConfig.getInt("GunGame.respawn-points") + " has to be a number.");
                            }
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup gungame <1-" + MainConfig.getInt("GunGame.respawn-points") + ">");
                        }
                    }

                    // colorbattle
                    else if (args[0].equalsIgnoreCase("colorbattle")) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("start")) {
                                plugin.getLocationManager().saveLocation("ColorBattleStart", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aColorBattleStart-Location was set.");
                            } else if (args[1].equalsIgnoreCase("location1")) {
                                plugin.getLocationManager().saveLocation("ColorBattleLocation1", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aColorBattleLocation1-Location was set.");
                            } else if (args[1].equalsIgnoreCase("location2")) {
                                plugin.getLocationManager().saveLocation("ColorBattleLocation2", player.getLocation());
                                player.sendMessage(Main.PREFIX + "§aColorBattleLocation2-Location was set.");
                            } else {
                                player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup colorbattle <start/location1/location2>");
                            }
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup colorbattle <start/location1/location2>");
                        }
                    }

                    // breakout
                    else if (args[0].equalsIgnoreCase("breakout")) {
                        if (args.length >= 2) {
                            if (args[1].equalsIgnoreCase("player")) {
                                if (args.length == 3) {
                                    if (args[2].chars().allMatch(Character::isDigit)) {
                                        int number = Integer.parseInt(args[2]);
                                        if (number <= Main.MAX_PLAYERS) {
                                            if (number > 0) {
                                                plugin.getLocationManager().saveLocation("BreakoutPlayer" + number, player.getLocation());
                                                player.sendMessage(Main.PREFIX + "§aBreakoutPlayer" + number + "-Location was set.");
                                            } else {
                                                player.sendMessage(Main.PREFIX + "§cThe number is less than or equal to zero.");
                                            }
                                        } else {
                                            player.sendMessage(Main.PREFIX + "§cThe number is higher as the defined number of max players. Max value is " + Main.MAX_PLAYERS + ".");
                                        }
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe parameter 1-" + Main.MAX_PLAYERS + " has to be a number.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup breakout player <1-" + Main.MAX_PLAYERS + ">");
                                }
                            } else if (args[1].equalsIgnoreCase("spectator")) {
                                if (args.length == 2) {
                                    plugin.getLocationManager().saveLocation("BreakoutSpectator", player.getLocation());
                                    player.sendMessage(Main.PREFIX + "§aBreakoutSpectator-Location was set.");
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup breakout spectator");
                                }
                            } else {
                                player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup breakout <player/spectator>");
                            }
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup breakout <player/spectator>");
                        }
                    }

                    // master builders
                    else if (args[0].equalsIgnoreCase("masterbuilders")) {
                        if (args.length >= 2) {
                            if (args[1].equalsIgnoreCase("player")) {
                                if (args.length == 3) {
                                    if (args[2].chars().allMatch(Character::isDigit)) {
                                        int number = Integer.parseInt(args[2]);
                                        if (number <= Main.MAX_PLAYERS) {
                                            if (number > 0) {
                                                plugin.getLocationManager().saveLocation("MasterBuildersPlayer" + number, player.getLocation());
                                                player.sendMessage(Main.PREFIX + "§aMasterBuildersPlayer" + number + "-Location was set.");
                                            } else {
                                                player.sendMessage(Main.PREFIX + "§cThe number is less than or equal to zero.");
                                            }
                                        } else {
                                            player.sendMessage(Main.PREFIX + "§cThe number is higher as the defined number of max players. Max value is " + Main.MAX_PLAYERS + ".");
                                        }
                                    } else {
                                        player.sendMessage(Main.PREFIX + "§cThe parameter 1-" + Main.MAX_PLAYERS + " has to be a number.");
                                    }
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup masterbuilders player <1-" + Main.MAX_PLAYERS + ">");
                                }
                            } else if (args[1].equalsIgnoreCase("pictures")) {
                                if (args.length == 2) {
                                    plugin.getLocationManager().saveLocation("MasterBuildersPictures", player.getLocation());
                                    player.sendMessage(Main.PREFIX + "§aMasterBuilderPictures-Location was set. §7(§cFrom this point is takes 5x5x1 pictures in positive x and y direction! And multiple pictures in positive z direction with 1 block space between pictures!§7)");
                                } else {
                                    player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup masterbuilders pictures §7(§cFrom this point is takes 5x5x1 pictures in positive x and y direction! And multiple pictures in positive z direction with 1 block space between pictures!§7)");
                                }
                            } else {
                                player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup masterbuilders <player/pictures>");
                            }
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup masterbuilders <player/pictures>");
                        }
                    }

                    // top10
                    else if (args[0].equalsIgnoreCase("top10")) {
                        if (args.length == 1) {
                            plugin.getLocationManager().saveLocation("Top10", player.getLocation());
                            player.sendMessage(Main.PREFIX + "§aTop10-Location was set.");
                        } else {
                            player.sendMessage(Main.PREFIX + "§cUsage: §6/mpsetup top10");
                        }
                    }

                    // wrong input
                    else {
                        player.sendMessage(USAGE);
                    }

                }

            } else {
                player.sendMessage(Main.NO_PERMISSION);
            }

        } else {
            sender.sendMessage(Main.PREFIX + "This command has to be executed by a player.");
        }

        return false;

    }

}
