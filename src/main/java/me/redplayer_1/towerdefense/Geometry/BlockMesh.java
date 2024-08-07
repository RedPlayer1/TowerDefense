package me.redplayer_1.towerdefense.Geometry;

import me.redplayer_1.towerdefense.Util.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * A rectangular area of blocks
 */
public class BlockMesh extends Area {
    private final Material[][][] mesh; // [y][z][x]

    /**
     * Creates a new BlockMesh filled with {@link Material#AIR}. Width, height, and depth must be greater than 0
     * @param width x-axis
     * @param depth z-axis
     * @param height y-axis
     */
    public BlockMesh(int width, int depth, int height) {
        super(width, depth, height);
        mesh = new Material[height][depth][width];
        fillMesh(Material.AIR);
    }

    /**
     * Copy constructor
     * @param original the mesh to copy from
     */
    public BlockMesh(BlockMesh original) {
        super(original.width, original.depth, original.height, original.getBottomLeft());
        mesh = original.mesh;
    }

    /**
     * Checks if the mesh can be placed without replacing any non-air blocks
     * @param bottomLeft the bottom left corner of the area to check
     * @return if the mesh can be placed
     */
    public boolean canPlace(Location bottomLeft) {
        AtomicBoolean result = new AtomicBoolean(true);
        forEachBlock(bottomLeft, (loc, rel) -> {
            if (loc.getWorld().getType(loc) != Material.AIR) {
                result.set(false);
            }
        });
        return result.get();
    }

    /**
     * Places the mesh in the world
     * @param bottomLeft the location of the bottom left corner of the placement
     */
    public void place(Location bottomLeft) {
        setBottomLeft(bottomLeft);
        forEachBlock(bottomLeft, (loc, rel) ->
                loc.getWorld().setType(loc, mesh[rel.y][rel.z][rel.x])
        );
    }

    /**
     * Sets all blocks within the mesh's area to air. Fails silently if the mesh isn't placed.
     */
    public void destroy() {
        if (bottomLeft != null) {
            forEachBlock(bottomLeft, (loc, rel) -> loc.getWorld().setType(loc, Material.AIR));
        }
    }

    /**
     * Sets the bottom left coordinate of the mesh. Changing this will alter where the {@link BlockMesh#destroy()}
     * method occurs (a null value means that the mesh is not placed).
     * @param bottomLeft the bottom left location of the mesh
     */
    @Override
    public void setBottomLeft(@Nullable Location bottomLeft) {
        if (bottomLeft != null) {
            bottomLeft = bottomLeft.toBlockLocation();
        }
        super.setBottomLeft(bottomLeft);
    }

    /**
     * Runs the action for each block in the mesh. If bottomLeft is not set, the method fails silently.
     *
     * @param bottomLeft the bottom-left corner of the region to loop over
     * @param action the action to perform which takes the world location and relative vector location (xyz indices)
     *               in the mesh.
     */
    public void forEachBlock(Location bottomLeft, BiConsumer<Location, Vector3> action) {
        final int maxY = bottomLeft.getBlockY() + height;
        final int maxZ = bottomLeft.getBlockZ() - depth;
        final int maxX = bottomLeft.getBlockX() + width;

        for (int y = bottomLeft.getBlockY(), relY = 0; y < maxY; y++, relY++) {
            for (int z = bottomLeft.getBlockZ(), relZ = 0; z > maxZ; z--, relZ++) {
                for (int x = bottomLeft.getBlockX(), relX = 0; x < maxX; x++, relX++) {
                    action.accept(new Location(bottomLeft.getWorld(), x, y, z), new Vector3(relX, relY, relZ));
                }
            }
        }
    }

    /**
     * Sets every block in the mesh to a material
     * @param material the material to fill the mesh with
     */
    public void fillMesh(Material material) {
        forEachBlock(
                new Location(null, 0, 0, 0),
                (loc, vec) -> mesh[vec.y][vec.z][vec.x] = material
        );
    }

    /**
     * Sets the mesh to the blocks in an area of the same size.
     * @param bottomLeft the bottom-left corner of the capture location
     */
    public void capture(Location bottomLeft) {
        forEachBlock(bottomLeft, (loc, vec) -> mesh[vec.y][vec.z][vec.x] = loc.getBlock().getType());
    }

    /**
     * Checks if the location is within the mesh's placement.
     * @param location the location to check
     * @return if the location is in the mesh's placement (false if mesh isn't placed)
     */
    public boolean contains(Location location) {
        if (bottomLeft == null) return false;
        Vector3 tr = Vector3.of(bottomLeft).add(width - 1, height - 1, -depth + 1);
        Vector3 bl = Vector3.of(bottomLeft);
        Vector3 loc = Vector3.of(location);
        /*
        return location.getWorld() == bottomLeft.getWorld()
                && location.getBlockX() >= bl.x && location.getBlockX() <= tr.x
                && location.getBlockY() >= bl.y && location.getBlockY() <= tr.y
                && location.getBlockZ() <= bl.z && location.getBlockZ() >= tr.z;
         */
        boolean xCheck = loc.x >= bl.x && loc.x <= tr.x;
        boolean yCheck = loc.y >= bl.y && loc.y <= tr.y;
        boolean zCheck = loc.z >= tr.z && loc.z <= bl.z;
        return location.getWorld() == bottomLeft.getWorld()
                && xCheck
                && yCheck
                && zCheck;
    }

    /**
     * @return the location's location relative to the mesh's bottom-left coordinate.
     * @throws IllegalStateException if this mesh isn't placed
     */
    public Vector3 toRelativeLocation(Location location) {
        if (bottomLeft == null) throw new IllegalStateException("The mesh's bottom left must not be null");
        return new Vector3(location.getBlockX() - bottomLeft.getBlockX(),
                location.getBlockY() - bottomLeft.getBlockY(),
                bottomLeft.getBlockZ() - location.getBlockZ());
    }

    /**
     * Turns a vector that is relative to the mesh's bottom left into a location relative to the world's (0, 0, 0)
     * @param rel the relative position
     * @param world the location's world
     * @return the new location
     * @throws IllegalStateException if the mesh is not placed
     */
    public Location fromRelativeLocation(Vector3 rel, @Nullable World world) {
        if (bottomLeft == null) {
            throw new IllegalStateException("Mesh must be placed before a relative location can be converted");
        }
        return new Location(
                world,
                bottomLeft.getBlockX() + rel.x,
                bottomLeft.getBlockY() + rel.y,
                bottomLeft.getBlockZ() - rel.z
        );
    }

    /**
     * Serializes the mesh into a new section created within the root.
     * If a section with this name already exists in the section, it is overwritten.
     *
     * @param rootSection the root section for the mesh to be stored
     * @param meshName the name of this mesh in the config section (may contain spaces)
     */
    public void serialize(ConfigurationSection rootSection, String meshName) {
        ConfigurationSection section = rootSection.createSection(meshName);
        if (bottomLeft != null) {
            LocationUtils.serialize(bottomLeft, section, "bottomLeft");
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
     * @return the deserialized mesh or null if it couldn't be found
     * @throws InvalidConfigurationException if required values are missing from the root section
     */
    public static BlockMesh deserialize(ConfigurationSection section) throws InvalidConfigurationException {
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
        if (section.isConfigurationSection("bottomLeft")) {
            mesh.bottomLeft = LocationUtils.deserialize(section.getConfigurationSection("bottomLeft"));
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