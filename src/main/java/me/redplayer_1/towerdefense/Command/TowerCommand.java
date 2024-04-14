package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Exception.NoSuchTemplateException;
import me.redplayer_1.towerdefense.Plot.Layout.Tower;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Privileged-Only Command
 */
public class TowerCommand extends Command {
    private static final List<String> ARGS = List.of("list", "edit", "create", "save");
    private static final String HELP_MSG =
            MessageUtils.helpEntry("/tower list", null, "list all tower templates") + '\n'
            + MessageUtils.helpEntry("/tower edit", "<name> <attributes|mesh>", "edit a tower's attributes or mesh") + '\n'
            + MessageUtils.helpEntry("/tower create", "<name> <range> <width> <depth> <height>", "create a new tower; tower's item is the item in main hand (must not be air)") + '\n'
            + MessageUtils.helpEntry("/tower save", null, "save the open tower template's mesh");
    private static final HashMap<Player, TowerMeta> towerData = new HashMap<>();

    public TowerCommand() {
        super("tower");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player && TDPlayer.isPrivileged(player) && args.length > 0)) return false;
        switch (args[0].toLowerCase()) {
            case "list" -> {
                // TODO: add tower prices to display next to names
                StringBuilder message = new StringBuilder("<dark_gray>Towers</dark_gray><newline>");
                for (Tower template : Towers.getTowers()) {
                    message.append("<yellow>").append(template.name).append("</yellow><newline>");
                }
                player.sendRichMessage(message.toString());
            }
            case "edit" -> {
                if (args.length < 3) {
                    MessageUtils.log(player, "Not enough args", LogLevel.ERROR);
                } else {
                    try {
                        if (args[2].equalsIgnoreCase("attributes")) {
                            Towers.openAttributeEditorInventory(player, args[1]);
                        } else if (args[2].equalsIgnoreCase("mesh")) {
                            if (MeshEditor.getEditor(player) == null || towerData.containsKey(player)) {
                                Towers.openMeshEditor(player, args[1]);
                            } else {
                                MessageUtils.log(player, "You are already editing another tower.", LogLevel.ERROR);
                            }
                        } else {
                            MessageUtils.log(player, "Invalid edit option.", LogLevel.ERROR);
                        }
                    } catch (NoSuchTemplateException e) {
                        MessageUtils.log(player, "No Tower named \"" + args[1] + "\" exists.", LogLevel.ERROR);
                    }
                }
            }
            case "create" -> {
                if (args.length >= 6) {
                    ItemStack towerItem = player.getInventory().getItemInMainHand();
                    int range, width, depth, height;
                    try {
                        range = Integer.parseInt(args[2]);
                        width = Integer.parseInt(args[3]);
                        depth = Integer.parseInt(args[4]);
                        height = Integer.parseInt(args[5]);
                    } catch (NumberFormatException e) {
                        MessageUtils.log(player, "Range and mesh dimensions must be valid integers.", LogLevel.ERROR);
                        return true;
                    }
                    if (towerItem.getType().isBlock()) {
                        towerData.put(player, new TowerMeta(args[1], towerItem, range));
                        new MeshEditor(player, new BlockMesh(width, depth, height), Material.PODZOL);
                        MessageUtils.log(player,
                                "To finish creating the tower, build it and then " +
                                        "<click:suggest_command:'/tower save'><green>click here</green></click>.",
                                LogLevel.NOTICE
                        );
                    }
                } else {
                    MessageUtils.log(player, "Not enough args", LogLevel.ERROR);
                }
            }
            case "save" -> {
                MeshEditor editor = MeshEditor.getEditor(player);
                if (editor != null) {
                    if (towerData.containsKey(player)) {
                        TowerMeta meta = towerData.get(player);
                        Towers.add(new Tower(meta.name, meta.item, meta.range, editor.close(true)));
                        MessageUtils.log(player, "Tower created.", LogLevel.SUCCESS);
                    } else {
                        editor.close(true);
                        MessageUtils.log(player, "Mesh updated.", LogLevel.SUCCESS);
                    }
                } else {
                    MessageUtils.log(player, "You don't have an editor open.", LogLevel.ERROR);
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!(sender instanceof Player player && TDPlayer.isPrivileged(player))) return Collections.emptyList();
        if (args.length == 1) return ARGS;
        if (args.length == 3) return List.of("attributes", "mesh");
        return Collections.emptyList();
    }

    private record TowerMeta(String name, ItemStack item, int range) {
        /**
             * @param mesh the tower's BlockMesh
             * @return a new tower created using the data in this class and a BlockMesh
             */
            public Tower toTower(BlockMesh mesh) {
                return new Tower(name, item, range, mesh);
            }
        }
}
