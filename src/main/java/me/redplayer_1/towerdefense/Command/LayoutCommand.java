package me.redplayer_1.towerdefense.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LayoutCommand extends Command {
    private static final List<String> ARGS = List.of("create", "edit", "save", "help");

    public LayoutCommand() {
        super("layout");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length < 1) {
            return ARGS;
        }
        return Collections.emptyList();
    }


}
