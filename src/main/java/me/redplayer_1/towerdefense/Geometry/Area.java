package me.redplayer_1.towerdefense.Geometry;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A cubical area
 */
public class Area {
    /** x length */
    public final int width;
    /** z length */
    public final int depth;
    /** y length */
    public final int height;
    /**
     * The location of the bottom left corner of the area
     */
    protected @Nullable Location bottomLeft;

    /**
     * Creates a new area. Width, depth, and height must be greater than zero.
     * @param width length on the x-axis
     * @param depth length on the z-axis
     * @param height length on the y-axis
     * @throws IllegalArgumentException if width, depth, or height are less than or equal to zero
     */
    public Area(int width, int depth, int height) {
        this(width, depth, height, null);
    }

    /**
     * Creates a new area. Width, depth, and height must be greater than zero.
     * @param width length on the x-axis
     * @param depth length on the z-axis
     * @param height length on the y-axis
     * @param bottomLeft the location of the bottom-left corner of the area
     * @throws IllegalArgumentException if width, depth, or height are less than or equal to zero
     */
    public Area(int width, int depth, int height, @Nullable Location bottomLeft) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.bottomLeft = bottomLeft;
    }

    /**
     * @param bottomLeft the bottom left corner of the area
     * @throws IllegalArgumentException if the location's world is null
     */
    public void setBottomLeft(@Nullable Location bottomLeft) {
        if (bottomLeft != null && bottomLeft.getWorld() == null) {
            throw new IllegalArgumentException("The bottom left must have a non-null world");
        }
        this.bottomLeft = bottomLeft;
    }

    /**
     * @return a copy of the bottom left corner of the block mesh (if it exists)
     */
    public @Nullable Location getBottomLeft() {
        return bottomLeft != null? bottomLeft.clone() : null;
    }

    /**
     * @return an immutable list of all entities inside this area
     */
    public List<Entity> getEntitiesInside() {
        if (bottomLeft != null) {
            return List.copyOf(bottomLeft.getWorld().getNearbyEntities(BoundingBox.of(bottomLeft, bottomLeft.clone().add(width, height, depth))));
        }
        return List.of();
    }
}
