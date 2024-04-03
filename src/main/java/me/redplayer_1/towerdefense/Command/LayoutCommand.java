package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Util.LogLevel;
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
    private static final List<String> ARGS = List.of("create", "edit", "save", "delete", "quit", "list", "default", "help");
    private static final String HELP_MSG =
            MessageUtils.helpEntry("/layout create", null, "open/start editor") + '\n'
            + MessageUtils.helpEntry("/layout edit", "<name>", "open existing layout") + '\n'
            + MessageUtils.helpEntry("/layout save", "<name>", "save open editor and create a layout") + '\n'
            + MessageUtils.helpEntry("/layout delete", "<name>", "delete a layout") + '\n'
            + MessageUtils.helpEntry("/layout quit", null, "exit the open editor without saving it") + '\n'
            + MessageUtils.helpEntry("/layout list",  null, "list all saved layout templates") + '\n'
            + MessageUtils.helpEntry("/layout default", null, "get the default layout's name") + '\n'
            + MessageUtils.helpEntry("/layout default", "<layout name>", "set the default layout") + '\n'
            + MessageUtils.helpEntry("/layout help", null, "show this help page");

    public LayoutCommand() {
        super("layout");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to run this command.");
            return true;
        }
        if (args.length < 1) {
            MessageUtils.log(player, "Not enough args", LogLevel.ERROR);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> new LayoutEditor(player);
            case "edit" -> MessageUtils.log(player, "Not Implemented", LogLevel.ERROR);
            case "save" -> {
                LayoutEditor editor = LayoutEditor.getEditor(player);
                if (editor != null) {
                    if (args.length >= 2) {
                        editor.save(args[1]);
                    } else {
                        MessageUtils.log(player, "Not enough args", LogLevel.ERROR);
                    }
                } else {
                    MessageUtils.log(player, "You don't have an open editor", LogLevel.ERROR);
                }
            }
            case "delete" -> MessageUtils.log(player, "Not Implemented", LogLevel.ERROR);
            case "quit" -> {
                LayoutEditor editor = LayoutEditor.getEditor(player);
                if (editor != null) {
                    editor.close();
                    MessageUtils.log(player, "Closed the editor", LogLevel.SUCCESS);
                } else {
                    MessageUtils.log(player, "No open editor", LogLevel.ERROR);
                }
            }
            case "list" -> {
                StringBuilder message = new StringBuilder("<dark_gray>Layouts</dark_gray><newline>");
                for (Layout layout : Layout.getLayouts()) {
                    message.append("<yellow>").append(layout.getName());
                    if (layout == Layout.defaultLayout) {
                        message.append("<white> - </white><dark_green>DEFAULT</dark_green>");
                    }
                    message.append("</yellow><newline>");
                }
                player.sendRichMessage(message.toString());
            }
            case "default" -> {
                if (args.length > 1) {
                    // default layout name supplied
                    Layout newDefault = Layout.getLayout(args[1]);
                    if (newDefault != null) {
                        MessageUtils.log(player,
                                "The default layout is now <white><i>" + args[1] + "</i></white> (was <white><i>"
                                        + (Layout.defaultLayout != null? Layout.defaultLayout.getName() : "none")
                                        + "</i></white>)",
                                LogLevel.SUCCESS
                        );
                        Layout.defaultLayout = newDefault;
                    } else {
                        MessageUtils.log(player, "No layout named " + args[1] + " exists", LogLevel.ERROR);
                    }
                } else {
                    // send current default layout
                    if (Layout.defaultLayout != null) {
                        MessageUtils.log(player,
                                "The default layout is <white><i>" + Layout.defaultLayout.getName() + "</i></white",
                                LogLevel.SUCCESS
                        );
                    } else {
                        MessageUtils.log(player, "There is no default layout", LogLevel.WARN);
                    }
                }
            }
            case "help" -> player.sendRichMessage(HELP_MSG);
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length <= 1) {
            return ARGS;
        }
        return Collections.emptyList();
    }
}
