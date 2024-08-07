package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Plot;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static me.redplayer_1.towerdefense.Util.MessageUtils.*;

/**
 * Root command for locating and managing plots
 */
public class PlotCommand extends Command {
    private static final List<String> NORMAL_ARGS = List.of("help");
    private static final List<String> PRIVILEGED_ARGS = List.of("help", "manage", "debug", "size", "resize", "origin", "setOrigin");
    private static final String NORMAL_HELP_MSG =
            helpEntry("/plot", null, "teleport to your plot") + '\n'
            + helpEntry("/plot", "<player>", "teleport to a player's plot") + '\n'
            + helpEntry("/plot help", null, "show this help page");
    private static final String PRIVILEGED_HELP_MSG = NORMAL_HELP_MSG + '\n' +
            helpEntry("/plot manage", "<player>", "open plot management GUI for a player's plot") + '\n'
            + helpEntry("/plot debug", "<player>", "print debug information about a player's plot") + '\n'
            + helpEntry("/plot size", null, "get the current size of the plot grid") + '\n'
            + helpEntry("/plot resize", "<size>", "resize the plot grid") + '\n'
            + helpEntry("/plot origin", null, "get the origin of the plot grid") + '\n'
            + helpEntry("/plot setOrigin", "<x> <y> <z>", "set the origin of the plot grid");

    public PlotCommand() {
        super("plot", "", NORMAL_HELP_MSG, Collections.emptyList());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to run this command.");
            return true;
        }

        if (args.length == 0) {
            // attempt to tp player to plot
            TDPlayer tdPlayer = TDPlayer.of(player);
            if (tdPlayer == null) {
                log(player, "You must have a plot to run this command!", LogLevel.ERROR);
                return true;
            }
            tdPlayer.getPlot().teleportPlayer(player);
            return true;
        }

        if (TDPlayer.isPrivileged(player)) {
            // privileged/management commands
            switch (args[0].toLowerCase()) {
                case "help" -> player.sendRichMessage(PRIVILEGED_HELP_MSG);
                case "manage" -> log(player, "not implemented", LogLevel.ERROR); //TODO
                case "debug" -> {
                    if (args.length < 2) {
                        log(player, "Not enough args", LogLevel.ERROR);
                    } else {
                        TDPlayer tdPlayer = TDPlayer.of(Bukkit.getPlayer(args[1]));
                        if (tdPlayer != null) {
                            player.sendPlainMessage(tdPlayer.getPlot().getLayout().getGrid().toString());
                            Layout layout = tdPlayer.getPlot().getLayout();
                            if (layout.isSpawnerEnabled()) {
                                layout.stop();
                                log(player, "Spawner <dark_red>stopped</dark_red>", LogLevel.SUCCESS);
                            } else {
                                layout.start(tdPlayer);
                                log(player, "Spawner <dark_green>started</dark_green>", LogLevel.SUCCESS);
                            }

                        } else {
                            log(player, "Player \"" + args[1] + "\" does not have a plot", LogLevel.ERROR);
                        }
                    }
                }
                case "size" -> log(player, String.valueOf(Plot.getPlotGridSize()), LogLevel.SUCCESS);
                case "resize" -> {
                    if (args.length < 2) {
                        log(player, "Not enough args", LogLevel.ERROR);
                    } else {
                        Plot.resizePlotGrid(Integer.parseInt(args[1]));
                        log(player, "Plot grid size is now " + args[1], LogLevel.SUCCESS);
                    }
                }
                case "origin" -> {
                    if (Plot.getGridOrigin() != null) {
                        log(player, locationToString(Plot.getGridOrigin()), LogLevel.SUCCESS);
                    } else {
                        log(player, "The plot origin isn't set", LogLevel.WARN);
                    }
                }
                case "setorigin" -> {
                    if (args.length < 4) {
                        log(player, "Not enough args", LogLevel.ERROR);
                    } else {
                        try {
                            Location newOrigin = new Location(
                                    player.getWorld(),
                                    Integer.parseInt(args[1]),
                                    Integer.parseInt(args[2]),
                                    Integer.parseInt(args[3])
                            );
                            log(player,
                                    "New plot grid origin is " + locationToString(newOrigin),
                                    LogLevel.SUCCESS);
                            Bukkit.getOnlinePlayers().forEach(p ->
                                    p.kick(MessageUtils.asMiniMessage("<gold>Plot origin changed</gold>"), PlayerKickEvent.Cause.PLUGIN)
                            );
                            Plot.setPlotGridOrigin(newOrigin);
                        } catch (NumberFormatException e) {
                            log(player, "Origin x, y, and z must be integers", LogLevel.ERROR);
                        }
                    }
                }
            }
            return true;
        }

        // unprivileged commands
        if (args[0].equalsIgnoreCase("help")) {
            player.sendRichMessage(NORMAL_HELP_MSG);
        } else if (args.length > 1) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                log(player, "Cannot find player \"" + args[1] + "\"", LogLevel.ERROR);
                return true;
            }
            TDPlayer tdPlayer = TDPlayer.of(targetPlayer);
            if (tdPlayer == null) {
                log(player, "Player \"" + args[1] + "\" doesn't have a plot", LogLevel.ERROR);
            } else {
                tdPlayer.getPlot().teleportPlayer(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!(sender instanceof Player player) || args.length > 1) return Collections.emptyList();
        return TDPlayer.isPrivileged(player) ? PRIVILEGED_ARGS : NORMAL_ARGS;
    }
}
