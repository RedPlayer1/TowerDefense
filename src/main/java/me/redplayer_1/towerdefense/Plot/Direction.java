package me.redplayer_1.towerdefense.Plot;

import org.bukkit.Location;

public enum Direction {
    NORTH, // +z
    SOUTH, // -z
    EAST,  // -x
    WEST;   // +x

    /**
     * @return the ordinal direction to the left of this rotation
     */
    public Direction left() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
        };
    }

    /**
     * @return the ordinal direction to the right of this rotation
     */
    public Direction right() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    /**
     * Returns the location 1 block in this direction
     * @param base the starting location
     * @param distance the distance the new location is from the base (going straight in this direction)
     * @return a new location 1 block in this direction
     */
    public Location toLocation(Location base, double distance) {
        Location result = new Location(base.getWorld(), base.getX(), base.getY(), base.getZ());
        switch (this) {
            case NORTH -> result.add(0, 0, -distance);
            case SOUTH -> result.add(0, 0, distance);
            case EAST -> result.add(distance, 0, 0);
            case WEST -> result.add(-distance, 0, 0);
        }
        return result;
    }
}
