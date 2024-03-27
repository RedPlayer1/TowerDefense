package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Plot.Plot;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Root command for locating and managing plots
 */
public class PlotCommand extends Command {
    private static final List<String> NORMAL_ARGS = List.of("help");
    private static final List<String> PRIVILEGED_ARGS = List.of("manage", "size", "resize", "origin", "setOrigin");
    private static final String NORMAL_HELP_MSG =
            """
                    <white>/plot</white> <gray>- teleport to your plot</gray>
                    <white>/plot</white <gold><player></gold> <gray>- teleport to a player's plot</gray>
                    <white>/plot</white> <gold>help</gold> <gray>- show this help page</gray>
                    """.trim();
    private static final String PRIVILEGED_HELP_MSG = NORMAL_HELP_MSG +
            """
                    <white>/plot</white> <gold>manage</gold> <gray>- open plot management GUI</gray>
                    <white>/plot</white> <gold>size</gold> <gray>- get the current size of the plot grid</gray>
                    <white>/plot</white> <gold>resize <size></gold> <gray>- resize the plot grid</gray>
                    <white>/plot</white> <gold>origin</gold> <gray>- get the origin of the plot grid</gray>
                    <white>/plot</white> <gold>setOrigin <x> <y> <z></gold <gray>- set the origin of the plot grid</gray>
                    """.trim();

    public PlotCommand() {
        super("plot", "", NORMAL_HELP_MSG, Collections.emptyList());
    }

    private boolean isPrivileged(Player player) {
        return player.isOp(); //TODO: privileged perms list in config
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to run this command.");
            return true;
        }

        if (args.length == 0) {
            TDPlayer tdPlayer = TDPlayer.of(player);
            if (tdPlayer == null) {
                MessageUtils.sendError(player, "You must have a plot to run this command!");
                return true;
            }
            tdPlayer.getPlot().teleportPlayer(player);
            return true;
        }

        if (isPrivileged(player)) {
            switch (args[0].toLowerCase()) {
                case "help" -> player.sendRichMessage(PRIVILEGED_HELP_MSG);
                case "manage" -> MessageUtils.sendError(player, "not implemented"); //TODO
                case "size" -> MessageUtils.sendSuccess(player, String.valueOf(Plot.getPlotGridSize()));
                case "resize" -> {
                    if (args.length < 2) {
                        MessageUtils.sendError(player, "Not enough args");
                    } else {
                        Plot.resizePlotGrid(Integer.parseInt(args[1]));
                        MessageUtils.sendSuccess(player, "Plot grid size is now " + args[1]);
                    }
                }
                case "origin" -> MessageUtils.sendSuccess(player, Plot.getGridOrigin().toString());
                case "setorigin" -> {
                    if (args.length < 4) {
                        MessageUtils.sendError(player, "Not enough args");
                    } else {
                        try {
                            Location newOrigin = new Location(
                                    Plot.getGridOrigin().getWorld(),
                                    Integer.parseInt(args[1]),
                                    Integer.parseInt(args[2]),
                                    Integer.parseInt(args[3])
                            );
                            Plot.setPlotGridOrigin(newOrigin);
                            MessageUtils.sendSuccess(player, "New plot grid origin is " + newOrigin);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendError(player, "Origin x, y, and z must be integers");
                        }
                    }
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            player.sendRichMessage(NORMAL_HELP_MSG);
        } else if (args.length > 1) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                MessageUtils.sendError(player, "Cannot find player \"" + args[1] + "\"");
                return true;
            }
            TDPlayer tdPlayer = TDPlayer.of(targetPlayer);
            if (tdPlayer == null) {
                MessageUtils.sendError(player, "Player \"" + args[1] + "\" doesn't have a plot");
            } else {
                tdPlayer.getPlot().teleportPlayer(player);
            }
            return true;
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        return isPrivileged(player) ? PRIVILEGED_ARGS : NORMAL_ARGS;
    }
}
