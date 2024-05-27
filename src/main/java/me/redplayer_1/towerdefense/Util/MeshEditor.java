package me.redplayer_1.towerdefense.Util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class MeshEditor {
    private static final HashMap<Player, MeshEditor> editors = new HashMap<>();
    private final Player player;
    Location bottomLeft;
    private final @Nullable Location prevBottomLeft;
    private final BlockMesh mesh;
    private final BlockMesh platform;
    private final BlockMesh originalArea; // stores the landscape before it was changed by the editor

    /**
     * Creates a new editor at the player's location and creates a base platform
     * @param player the player editing the mesh
     * @param mesh the mesh to be edited
     * @param platformMaterial the material the base platform will be made of
     */
    public MeshEditor(Player player, BlockMesh mesh, Material platformMaterial) {
        this.player = player;
        this.mesh = mesh;
        prevBottomLeft = mesh.getBottomLeft();
        bottomLeft = player.getLocation().subtract(0, 1, 0);
        originalArea = new BlockMesh(mesh.width, mesh.depth, mesh.height + 1 /* includes platform */);
        originalArea.capture(player.getLocation().subtract(0, 1, 0));
        platform = new BlockMesh(mesh.width, mesh.depth, 1);
        platform.fillMesh(platformMaterial);
        platform.place(bottomLeft);
        mesh.place(bottomLeft.clone().add(0, 1, 0));
        editors.put(player, this);
    }

    /**
     * Closes the editor and restores the area to its original state
     * @param save if the changes to the mesh should be saved
     * @return the mesh that the editor was editing
     * @throws IllegalStateException if the editor has already been quit
     */
    public BlockMesh close(boolean save) {
        Location bottomLeft = originalArea.getBottomLeft();
        if (bottomLeft == null) throw new IllegalStateException("Editor has already been quit");
        if (save) mesh.capture(bottomLeft.add(0, 1, 0));
        mesh.destroy();
        mesh.setBottomLeft(prevBottomLeft);
        platform.destroy();
        originalArea.place(originalArea.getBottomLeft());
        editors.remove(player);
        return mesh;
    }

    /**
     * @param player the player the editor belongs to
     * @return the editor associated with the player, if it exists
     */
    public static @Nullable MeshEditor getEditor(Player player) {
        return editors.get(player);
    }

    /**
     * Closes all open editors without saving them
     */
    public static void closeAll() {
        editors.forEach((p, editor) -> editor.close(false));
    }
}
