package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Root command for managing and creating layouts
 */
public class LayoutCommand extends Command {
    private static final List<String> ARGS = List.of("create", "edit", "save", "list", "help");
    private static final String HELP_MSG =
            """
                    <white>/layout create</white> <gold><name></gold> <gray>- open/start editor</gray>
                    <white>/layout edit</white> <gold><name></gold> <gray>- open existing editor</gray>
                    <white>/layout save</white> <gray>- save open editor</gray>
                    <white>/layout list</white> <gray>- list all saved layout templates</gray>
                    <white>/layout help</white> <gray- show this help page</gray>
                    """.trim();

    public LayoutCommand() {
        super("layout");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to run this command.");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> new LayoutEditor(player);
            case "edit" -> MessageUtils.sendError(player, "Not Implemented");
            case "save" -> {
                LayoutEditor editor = LayoutEditor.getEditor(player);
                if (editor != null) {
                    if (args.length >= 2) {
                        editor.save(args[1]);
                    } else {
                        MessageUtils.sendError(player, "Not enough args");
                    }
                } else {
                    MessageUtils.sendError(player, "You don't have an open editor");
                }
            }
            case "help" -> player.sendRichMessage(HELP_MSG);
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length < 1) {
            return ARGS;
        }
        return Collections.emptyList();
    }


}
