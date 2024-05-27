package me.redplayer_1.towerdefense.Command;

import static me.redplayer_1.towerdefense.Util.MessageUtils.log;

import me.redplayer_1.towerdefense.Plot.Layout.Tower;
import me.redplayer_1.towerdefense.Plot.Layout.TowerFactory;
import me.redplayer_1.towerdefense.Plot.Layout.Towers;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MeshEditor;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class TowerCommand  extends Command {
    // privileged only command
    private static final List<String> PRIVILEGED_ARGS = List.of("help", "create", "edit", "save", "quit", "list");
    private static final String PRIVILEGED_HELP_MSG =
            MessageUtils.helpEntry("/tower help", null, "show this help page") + '\n'
            + MessageUtils.helpEntry("/tower create", "<width (x)> <height (y)> <depth (z)>", "create a new editor with the provided tower dimensions") + '\n'
            + MessageUtils.helpEntry("/tower edit", "<name>", "edit a tower's mesh") + '\n'
            + MessageUtils.helpEntry("/tower save", "<name> <range> <damage> <cost>", "creates a new tower with the provided params and with an item set to the held item") + '\n'
            + MessageUtils.helpEntry("/tower quit", null, "exit the active editor without saving it") + '\n'
            + MessageUtils.helpEntry("/tower list", null, "lists all the created towers");
    private HashMap<Player, TowerFactory> factories = new LinkedHashMap<>();

    public TowerCommand() {
        super("tower");
    }
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            log(sender, "Must be a player to use this command", LogLevel.ERROR);
            return false;
        }
        if (!TDPlayer.isPrivileged(player)) {
            log(player, "You are not allowed to use this command", LogLevel.ERROR);
            return false;
        }
        if (args.length == 0) {
            log(player, "Not enough args", LogLevel.ERROR);
            return false;
        }
        switch (args[0]) {
            case "help" -> player.sendRichMessage(PRIVILEGED_HELP_MSG);
            case "create" -> {
                if (factories.containsKey(player)) {
                    log(player, "You are already editing a tower", LogLevel.ERROR);
                } else if (args.length < 4) {
                    log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    try {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        factories.put(player, new TowerFactory().setEditor(new MeshEditor(
                                player, new BlockMesh(x, z, y), Material.BEDROCK //TODO: make config value
                        )));
                        log(player, "Now editing a new tower", LogLevel.SUCCESS);
                    } catch (NumberFormatException e) {
                        log(player, "width, height and depth arguments must be integers", LogLevel.ERROR);
                    }
                }
            }
            case "edit" -> {
                if (factories.containsKey(player)) {
                    log(player, "You are already editing a tower", LogLevel.ERROR);
                } else if (args.length < 2) {
                    log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    Tower t = Towers.get(args[1]);
                    if (t != null) {
                        factories.put(player, new TowerFactory(t).setMesh(null).setEditor(
                                new MeshEditor(player, new BlockMesh(t.getMesh()), Material.BEDROCK)
                        ));
                        log(player, "Now editing tower \"" + t.name + "\"", LogLevel.SUCCESS);
                    } else {
                        log(player, "No tower with that name exists", LogLevel.ERROR);
                    }
                }
            }
            case "save" -> {
                TowerFactory factory = factories.get(player);
                if (factory == null) {
                    log(player, "You must be editing a tower to do this", LogLevel.ERROR);
                } else if (args.length < 5) {
                    if (args.length == 1) {
                        // no args supplied, check if the factory was based on a preexisting tower
                        try {
                            Towers.add(factory.build());
                            log(player, "Updated existing tower", LogLevel.SUCCESS);
                        } catch (IllegalStateException e) {
                            log(player, "Not enough args", LogLevel.ERROR);
                        }
                    } else {
                        log(player, "Not enough args", LogLevel.ERROR);
                    }
                } else {
                    try {
                        factory.setItem(player.getInventory().getItemInMainHand());
                        factory.setName(args[1]);
                        factory.setRange(Integer.parseInt(args[2]));
                        factory.setDamage(Integer.parseInt(args[3]));
                        // TODO: cost
                        try {
                            Towers.add(factory.build());
                            log(player, "Tower created", LogLevel.SUCCESS);
                        } catch (IllegalStateException e) {
                            log(player, "Something went wrong while creating the tower, please report", LogLevel.CRITICAL);
                        }
                    } catch (NumberFormatException e) {
                        log(player, "The range and damage arguments must be valid integers", LogLevel.ERROR);
                    }
                }
            }
            case "quit" -> {
                TowerFactory factory = factories.get(player);
                if (factory != null) {
                    if (factory.getEditor() != null) {
                        factory.getEditor().close(false);
                    }
                    factories.remove(player);
                    log(player, "Quit the active editor", LogLevel.SUCCESS);
                } else {
                    log(player, "You are not editing a tower", LogLevel.ERROR);
                }
            }
            case "list" -> {
                player.sendRichMessage("<gray>----- <b><gold>Towers</gold></b> ------</gray>");
                for (Tower t : Towers.getTowers()) {
                    player.sendRichMessage(
                            // TODO: config val (w/ format specifiers)?
                            "<hover:show_text:'<red>Range</red>: <gray>" + t.getRange() + "</gray><newline><red>Damage</red>: <gray>" + t.getDamage() + "</gray><newline><red>Cost</red>: <gray>not implemented</gray><newline><red>Item</red>: " + MessageUtils.fromMiniMessage(t.getItem().displayName()) + "'><yellow>" + t.name + "</yellow></hover>"
                    );
                }
                player.sendRichMessage("<gray>------------</gray>");
            }
        }
        return true;
    }
}
