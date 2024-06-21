package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Plot.Layout.Layouts;
import me.redplayer_1.towerdefense.Util.LogLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static me.redplayer_1.towerdefense.Util.MessageUtils.helpEntry;
import static me.redplayer_1.towerdefense.Util.MessageUtils.log;

/**
 * Root command for managing and creating layouts
 */
public class LayoutCommand extends Command {
    private static final List<String> ARGS = List.of("create", "edit", "save", "delete", "quit", "list", "default", "help");
    private static final String HELP_MSG =
            helpEntry("/layout create", null, "open/start editor") + '\n'
            + helpEntry("/layout edit", "<name>", "open existing layout") + '\n'
            + helpEntry("/layout save", "<name>", "save open editor and create a layout (not needed if editing an existing layout)") + '\n'
            + helpEntry("/layout delete", "<name>", "delete a layout") + '\n'
            + helpEntry("/layout quit", null, "exit the open editor without saving it") + '\n'
            + helpEntry("/layout list",  null, "list all saved layout templates") + '\n'
            + helpEntry("/layout default", null, "get the default layout's name") + '\n'
            + helpEntry("/layout default", "<layout name>", "set the default layout") + '\n'
            + helpEntry("/layout help", null, "show this help page");

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
            log(player, "Not enough args", LogLevel.ERROR);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> new LayoutEditor(player);
            case "edit" -> {
                if (args.length >= 2) {
                    Layout layout = Layouts.getTemplate(args[1]);
                    if (layout != null) {
                        new LayoutEditor(player, layout);
                    } else {
                        log(player, "Layout \"" + args[1] + "\" does not exist.", LogLevel.ERROR);
                    }
                } else {
                    log(player, "Not enough args", LogLevel.ERROR);
                }
            }
            case "save" -> {
                LayoutEditor editor = LayoutEditor.getEditor(player);
                if (editor != null) {
                    if (editor.getEditedLayoutName() != null) {
                        editor.save(editor.getEditedLayoutName());
                        log(player, "Layout updated.", LogLevel.SUCCESS);
                    } else if (args.length >= 2) {
                        editor.save(args[1]);
                        log(player, "Layout saved.", LogLevel.SUCCESS);
                    } else {
                        log(player, "Not enough args", LogLevel.ERROR);
                    }
                } else {
                    log(player, "You don't have an open editor", LogLevel.ERROR);
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    log(player, "Not Enough args", LogLevel.ERROR);
                } else {
                    if (Layouts.removeTemplate(args[1])) {
                        log(player, "Removed layout", LogLevel.SUCCESS);
                    } else {
                        log(player, "No layout named \"" + args[1] + "\" exists", LogLevel.WARN);
                    }
                }
            }
            case "quit" -> {
                LayoutEditor editor = LayoutEditor.getEditor(player);
                if (editor != null) {
                    editor.close();
                    log(player, "Closed the editor", LogLevel.SUCCESS);
                } else {
                    log(player, "No open editor", LogLevel.ERROR);
                }
            }
            case "list" -> {
                StringBuilder message = new StringBuilder("<dark_gray>Layouts</dark_gray><newline>");
                for (Layout layout : Layouts.getTemplates()) {
                    message.append("<yellow>").append(layout.getName());
                    if (layout.getName().equals(Layout.defaultLayout)) {
                        message.append("<white> - </white><dark_green>DEFAULT</dark_green>");
                    }
                    message.append("</yellow><newline>");
                }
                player.sendRichMessage(message.toString());
            }
            case "default" -> {
                if (args.length > 1) {
                    // default layout name supplied
                    if (Layouts.isTemplate(args[1])) {
                        log(player,
                                "The default layout is now <white><i>" + args[1] + "</i></white> (was <white><i>"
                                        + (Layout.defaultLayout != null? Layout.defaultLayout : "none")
                                        + "</i></white>)",
                                LogLevel.SUCCESS
                        );
                        Layout.defaultLayout = args[1];
                    } else {
                        log(player, "No layout named " + args[1] + " exists", LogLevel.ERROR);
                    }
                } else {
                    // send current default layout
                    if (Layout.defaultLayout != null) {
                        log(player,
                                "The default layout is <white><i>" + Layout.defaultLayout + "</i></white>",
                                LogLevel.SUCCESS
                        );
                    } else {
                        log(player, "There is no default layout", LogLevel.WARN);
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
