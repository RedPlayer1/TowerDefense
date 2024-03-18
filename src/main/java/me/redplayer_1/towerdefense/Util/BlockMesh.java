package me.redplayer_1.towerdefense.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * A rectangular area of blocks
 */
public class BlockMesh {
    private static final String DATA_SEPARATOR = "::";

    private final int width; // x
    private final int depth; // z
    private final int height; // y
    private Material[][][] mesh; // [y][z][x]
    private @Nullable Location bottomLeft;

    public BlockMesh(int width, int depth, int height) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        mesh = new Material[height][depth][width];
        bottomLeft = null;
    }

    /**
     * Checks if the mesh can be placed without replacing any non-air blocks
     * @param bottomLeft the bottom left corner of the area to check
     * @return if the mesh can be placed
     */
    public boolean canPlace(Location bottomLeft) {
        Location prevBottomLeft = this.bottomLeft;
        this.bottomLeft = bottomLeft;
        AtomicBoolean result = new AtomicBoolean(true);
        forEachBlock((loc, rel) -> {
            if (loc.getWorld().getType(loc) != Material.AIR) result.set(false);
        });
        this.bottomLeft = prevBottomLeft;
        return result.get();
    }

    public void place(Location bottomLeft) {
        this.bottomLeft = bottomLeft;
        forEachBlock((loc, rel) ->
                loc.getWorld().setType(loc, mesh[rel.y][rel.z][rel.x])
        );
    }

    /**
     * Sets all blocks within the mesh's area to Material.AIR
     * (does not modify the stored Mesh)
     */
    public void destroy() {
        forEachBlock((loc, rel) -> loc.getWorld().setType(loc, Material.AIR));
    }

    /**
     * @return the bottom left corner of the block mesh (if it exists)
     */
    public @Nullable Location getBottomLeft() {
        return bottomLeft;
    }

    /**
     * Runs the action for each block in the mesh. If bottomLeft is not set, the method fails silently.
     *
     * @param action the action to perform which takes the world location and relative vector location in the mesh.
     */
    public void forEachBlock(BiConsumer<Location, Vector3> action) {
        if (bottomLeft == null) return;
        final int maxY = bottomLeft.getBlockY() + height - 1;
        final int maxZ = bottomLeft.getBlockZ() + depth - 1;
        final int maxX = bottomLeft.getBlockX() + width - 1;

        for (int y = bottomLeft.getBlockY(), relY = 0; y < maxY; y++, relY++) {
            for (int z = bottomLeft.getBlockZ(), relZ = 0; z < maxZ; z++, relZ++) {
                for (int x = bottomLeft.getBlockX(), relX = 0; x < maxX; x++, relX++) {
                    action.accept(new Location(bottomLeft.getWorld(), x, y, z), new Vector3(relX, relY, relZ));
                }
            }
        }
    }

    /**
     * Serializes the mesh into the config section provided (includes bottomLeft).
     * If a section with this name already exists in the section, it is overwritten.
     *
     * @param rootSection the root section for the mesh to be stored
     * @param meshName    the name of this mesh in the config section (may contain spaces)
     */
    public void serialize(ConfigurationSection rootSection, String meshName) {
        ConfigurationSection section = rootSection.createSection(meshName);
        if (bottomLeft != null) {
            section.set("location",
                    bottomLeft.getWorld().getUID() + DATA_SEPARATOR
                            + bottomLeft.getBlockX() + DATA_SEPARATOR
                            + bottomLeft.getBlockY() + DATA_SEPARATOR
                            + bottomLeft.getBlockZ()
            );
        }
        section.set("width", width);
        section.set("depth", depth);
        section.set("height", height);

        // since the mesh is looped over in the same ways, relative coordinates
        // don't need to be saved with the block types
        List<String> types = new LinkedList<>();
        for (Material[][] materials : mesh) {
            for (Material[] material : materials) {
                for (Material value : material) {
                    types.add(value.name());
                }
            }
        }
        section.set("blocks", types);
    }

    /**
     * Deserializes a named BlockMesh from a ConfigurationSection.
     *
     * @param rootSection the section that the mesh is stored in
     * @param name        the case-sensitive name of the mesh
     * @return the deserialized mesh or null if it couldn't be found
     * @throws InvalidConfigurationException if required values are missing from the root section
     */
    public static @Nullable BlockMesh deserialize(ConfigurationSection rootSection, String name) throws InvalidConfigurationException {
        ConfigurationSection section = rootSection.getConfigurationSection(name);
        if (section == null) return null;

        BlockMesh mesh;
        try {
            mesh = new BlockMesh(
                    section.getInt("width"),
                    section.getInt("depth"),
                    section.getInt("height")
            );
        } catch (NullPointerException e) {
            throw new InvalidConfigurationException("Missing width, depth, and/or height");
        }

        String[] strs = section.getString("location", "").split("::");
        if (strs.length >= 4) {
            try {
                mesh.bottomLeft = new Location(
                        Bukkit.getWorld(UUID.fromString(strs[0])),
                        Integer.parseInt(strs[1]),
                        Integer.parseInt(strs[2]),
                        Integer.parseInt(strs[3])
                );
            } catch (NullPointerException | NumberFormatException e) {
                throw new InvalidConfigurationException("Invalid Location");
            }
        }

        List<String> blocks = section.getStringList("blocks");
        if (blocks.isEmpty() || blocks.size() < mesh.height * mesh.depth * mesh.width)
            throw new InvalidConfigurationException("Not enough blocks are in the mesh (found " + blocks.size() + ")");
        int i = 0;
        for (int y = 0; y < mesh.height; y++) {
            for (int z = 0; z < mesh.depth; z++) {
                for (int x = 0; x < mesh.width; x++) {
                    mesh.mesh[y][z][x] = Material.valueOf(blocks.get(i));
                    i++;
                }
            }
        }

        return mesh;
    }
}
