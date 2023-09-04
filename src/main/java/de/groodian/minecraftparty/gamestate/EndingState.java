package de.groodian.minecraftparty.gamestate;

import de.groodian.hyperiorcore.boards.HTitle;
import de.groodian.hyperiorcore.main.HyperiorCore;
import de.groodian.hyperiorcore.util.HSound;
import de.groodian.hyperiorcosmetics.HyperiorCosmetic;
import de.groodian.minecraftparty.countdown.EndingCountdown;
import de.groodian.minecraftparty.main.Main;
import de.groodian.minecraftparty.main.Messages;
import de.groodian.minecraftparty.util.TeleportManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EndingState implements GameState {

    private final Main plugin;
    private final EndingCountdown endingCountdown;
    private final TeleportManager teleportManager;

    private BukkitTask soundTask = null;
    private int soundCounter = 0;

    public EndingState(Main plugin) {
        this.plugin = plugin;
        this.endingCountdown = new EndingCountdown(plugin);
        this.teleportManager = new TeleportManager(plugin, this::afterAllTeleported);
    }

    @Override
    public void start() {
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§aENDING STATE STARTED!");

        // must be done before the delay, because if a player disconnects while the
        // delay he gets no play time and game ended, but he need to get it because the
        // game is ended. He gets not removed from the stars map. (look in MainListener)
        plugin.getStats().gameEnded();

        new BukkitRunnable() {
            @Override
            public void run() {

                plugin.getServer().broadcast(Main.PREFIX.append(Messages.get("the-winner-are")));
                new HSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    teleportManager.addTeleport(player, plugin.getLocationManager().LOBBY,
                            () -> HyperiorCore.getPaper().getScoreboard().unregisterScoreboard(player));
                }

                teleportManager.startTeleporting();

            }
        }.runTaskLater(plugin, 35);

    }

    private void afterAllTeleported() {
        HyperiorCosmetic.enable();

        playSound();

        // SORT PLAYERS START
        Map<Player, Integer> sorted = plugin.getStars()
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        Map<Player, Integer> forDatabase = new HashMap<>();

        List<List<Player>> winner = new ArrayList<>();
        List<Player> playersAtTheSamePlace = new ArrayList<>();

        int temp = sorted.entrySet().iterator().next().getValue();
        int place = 1;

        for (Map.Entry<Player, Integer> current : sorted.entrySet()) {
            if (!(current.getValue() == temp)) {
                temp = current.getValue();
                winner.add(playersAtTheSamePlace);
                playersAtTheSamePlace = new ArrayList<>();
                place++;
            }
            playersAtTheSamePlace.add(current.getKey());
            forDatabase.put(current.getKey(), place);
        }
        winner.add(playersAtTheSamePlace);
        // SORT PLAYERS END

        // TITLE START
        if (winner.size() > 0) {
            List<Player> firstPlace = winner.get(0);
            TextComponent.Builder title = Component.empty().toBuilder();

            boolean first = true;
            for (Player player : firstPlace) {
                if (!first) {
                    title.append(Component.text(", ", NamedTextColor.GRAY));
                }
                title.append(Component.text(player.getName(), NamedTextColor.GREEN));
                first = false;
            }

            if (firstPlace.size() == 1) {
                new HTitle(Duration.ofMillis(1000), Duration.ofMillis(3000), Duration.ofMillis(1000), title.build(),
                        Messages.get("one-winner-subtitle")).send();
            } else {
                new HTitle(Duration.ofMillis(1000), Duration.ofMillis(3000), Duration.ofMillis(1000), title.build(),
                        Messages.get("multiple-winner-subtitle")).send();
            }
        }
        // TITLE END

        // MESSAGE START
        TextComponent.Builder winnerOutput = Component.empty().toBuilder();

        place = 1;
        for (List<Player> current : winner) {
            winnerOutput
                    .append(Component.text(place, NamedTextColor.GREEN)).decorate(TextDecoration.BOLD)
                    .append(Component.text("# ", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false))
                    .append(Component.text(">> ", NamedTextColor.GRAY)).decorate(TextDecoration.BOLD);

            boolean first = true;
            for (Player player : current) {
                if (!first) {
                    winnerOutput.append(Component.text(", ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false));
                }
                winnerOutput.append(Component.text(player.getName(), getColor(place)).decoration(TextDecoration.BOLD, false));
                first = false;
            }

            winnerOutput
                    .append(Component.text(" - ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                    .append(Component.text(plugin.getStars().get(current.get(0)), NamedTextColor.YELLOW)
                            .decoration(TextDecoration.BOLD, false))
                    .append(Messages.get("points").decoration(TextDecoration.BOLD, false))
                    .append(Component.newline());

            place++;
        }

        TextComponent.Builder output = Component.empty().toBuilder();
        output
                .append(Component.text("--------------------------------", NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH))
                .append(Component.newline())
                .append(Component.newline())
                .append(winnerOutput.build())
                .append(Component.newline())
                .append(Component.text("--------------------------------", NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));

        plugin.getServer().broadcast(output.build());
        // MESSAGE END

        plugin.getStats().gameFinished(forDatabase);
        plugin.getStats().finish();

        endingCountdown.start();
    }

    private void playSound() {
        new HSound(Sound.ENTITY_ENDER_DRAGON_FLAP).play();

        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                new HSound(Sound.ENTITY_PLAYER_LEVELUP).play();
                soundCounter++;
                if (soundCounter > 3) {
                    soundTask.cancel();
                }
            }
        }.runTaskTimer(plugin, 4, 2);
    }

    @Override
    public void stop() {
        plugin.stopServer();
        Bukkit.getConsoleSender().sendMessage(Main.PREFIX_CONSOLE + "§cENDING STATE STOPPED!");
    }

    private TextColor getColor(int place) {
        return switch (place) {
            case 1 -> NamedTextColor.GOLD;
            case 2 -> NamedTextColor.GRAY;
            default -> NamedTextColor.RED;
        };
    }

}
