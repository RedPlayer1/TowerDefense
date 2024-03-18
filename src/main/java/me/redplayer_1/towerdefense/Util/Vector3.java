package me.redplayer_1.towerdefense.Util;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * A 3-dimensional vector
 */
public class Vector3 {
    public int x;
    public int y;
    public int z;

    /**
     * Create a new vector
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Adds the x, y, z coordinates to the Vector
     */
    public void add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    /**
     * Adds the Vector's coordinates to this Vector
     *
     * @param vec the vector to add values from (not modified)
     */
    public void add(Vector3 vec) {
        add(vec.x, vec.y, vec.z);
    }

    /**
     * @return a copy of this vector
     */
    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    /**
     * @return the Bukkit location equal to this vector (with no world set)
     */
    public Location toLocation() {
        return toLocation(null);
    }

    /**
     * @param world the returned Location's world
     * @return the Bukkit location equal to this vector
     */
    public Location toLocation(@Nullable World world) {
        return new Location(world, x, y, z);
    }
}
