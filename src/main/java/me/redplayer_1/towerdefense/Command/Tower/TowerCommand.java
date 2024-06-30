package me.redplayer_1.towerdefense.Command.Tower;

import me.redplayer_1.towerdefense.Geometry.BlockMesh;
import me.redplayer_1.towerdefense.Geometry.MeshEditor;
import me.redplayer_1.towerdefense.Plot.Tower.Tower;
import me.redplayer_1.towerdefense.Plot.Tower.Towers;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.Util.LogLevel;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static me.redplayer_1.towerdefense.Util.MessageUtils.*;

public class TowerCommand  extends Command {
    // privileged only command
    private static final List<String> PRIVILEGED_ARGS = List.of("help", "create", "edit", "save", "quit", "list", "give");
    private static final String PRIVILEGED_HELP_MSG =
            helpEntry("/tower help", null, "show this help page") + '\n'
            + helpEntry("/tower create", "<width (x)> <height (y)> <depth (z)>", "create a new editor with the provided tower dimensions") + '\n'
            + helpEntry("/tower edit", "<name>", "edit a tower's mesh") + '\n'
            + helpEntry("/tower save", "<name>", "creates a new tower with the provided params and with an item set to the held item") + '\n'
            + helpEntry("/tower delete", "<name>", "delete a tower") + '\n'
            + helpEntry("/tower quit", null, "exit the active editor without saving it") + '\n'
            + helpEntry("/tower list", null, "lists all the created towers") + '\n'
            + helpEntry("/tower give", "<name>", "gives you the specified tower in item form");
    private final HashMap<Player, MeshEditor> editors = new HashMap<>();

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
                if (editors.containsKey(player)) {
                    log(player, "You are already editing a tower", LogLevel.ERROR);
                } else if (args.length < 4) {
                    log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    try {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        BlockMesh mesh = new BlockMesh(x, z, y);
                        mesh.setBottomLeft(player.getLocation());
                        mesh.fillMesh(Material.AIR);
                        editors.put(player, new MeshEditor(
                                player, mesh, Material.BEDROCK //TODO: make config value
                        ));
                        log(player, "Now editing a new tower", LogLevel.SUCCESS);
                    } catch (NumberFormatException e) {
                        log(player, "width, height and depth arguments must be integers", LogLevel.ERROR);
                    }
                }
            }
            case "edit" -> {
                if (editors.containsKey(player)) {
                    log(player, "You are already editing a tower", LogLevel.ERROR);
                } else if (args.length < 2) {
                    log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    Tower tower = Towers.get(args[1]);
                    if (tower != null) {
                        editors.put(player, new MeshEditor(player, new BlockMesh(tower.getMesh()), Material.BEDROCK));
                        log(player, "Now editing tower \"" + tower.name + "\"", LogLevel.SUCCESS);
                    } else {
                        log(player, "No tower with that name exists", LogLevel.ERROR);
                    }
                }
            }
            case "save" -> {
                MeshEditor editor = editors.get(player);
                ItemStack item = player.getInventory().getItemInMainHand();
                if (editor == null) {
                    log(player, "You must be editing a tower to do this", LogLevel.ERROR);
                } else if (item.isEmpty() || !item.getType().isBlock()) {
                    log(player, "You must be holding a placeable item", LogLevel.ERROR);
                } else {
                    new TowerConversation(player, editor.close(true), item);
                    editors.remove(player);
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    Tower tower = Towers.get(args[1]);
                    if (tower != null) {
                        Towers.remove(args[1]);
                        log(player, "Removed \"" + args[1] + "\"", LogLevel.SUCCESS);
                    } else {
                        log(player, "No such tower exists", LogLevel.ERROR);
                    }
                }
            }
            case "quit" -> {
                MeshEditor editor = editors.get(player);
                if (editor != null) {
                    editor.close(false);
                    editors.remove(player);
                    log(player, "Quit the active editor", LogLevel.SUCCESS);
                } else {
                    log(player, "You are not editing a tower", LogLevel.ERROR);
                }
            }
            case "list" -> {
                player.sendRichMessage("<gray>----- <b><gold>Towers</gold></b> ------</gray>");
                for (Tower t : Towers.getTowers()) {
                    player.sendMessage(asMiniMessage("<yellow>" + t.name + "</yellow>").hoverEvent(asMiniMessage(
                            "<red>Range</red>: <gray>" + t.getRange() + "</gray><newline><red>Damage</red>: <gray>" + t.getDamage() + "</gray><newline><red>Cost</red>: <gray>not implemented</gray><newline><red>Item</red>: "
                    ).append(t.getItem().displayName())));
                }
                player.sendRichMessage("<gray>------------</gray>");
            }
            case "give" -> {
                if (args.length < 2) {
                    log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    Tower t = Towers.get(args[1]);
                    if (t != null) {
                        player.getInventory().addItem(t.getItem());
                        log(player, "Gave 1 " + t.name, LogLevel.SUCCESS);
                    } else {
                        log(player, "Invalid tower name", LogLevel.ERROR);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return PRIVILEGED_ARGS;
        } else {
            return Collections.emptyList();
        }
    }
}
