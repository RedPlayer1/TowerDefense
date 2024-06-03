package me.redplayer_1.towerdefense.Util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * A 3-dimensional vector of integers
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
     * @return the same (modified) vector
     */
    public Vector3 add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Adds the Vector's coordinates to this Vector
     * @param vec the vector to add values from (not modified)
     * @return the modified vector
     */
    public Vector3 add(Vector3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    /**
     * Adds the Location's block coordinates to this Vector
     * @param loc the location to add
     * @return the modified vector
     */
    public Vector3 add(Location loc) {
        return add(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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

    @Override
    public String toString() {
        return "Vector3: (" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Creates a vector representation of the location
     * @param location the location to convert
     * @return the vector representation of the location
     */
    public static Vector3 of(Location location) {
        return new Vector3(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public void serialize(ConfigurationSection section) {
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
    }

    public static Vector3 deserialize(ConfigurationSection section) {
        return new Vector3(
                section.getInt("x"),
                section.getInt("y"),
                section.getInt("z")
        );
    }
}
