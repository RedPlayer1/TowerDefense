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
     * @return a new location 1 block in this direction
     */
    public Location getFromLocation(Location base) {
        Location result = new Location(base.getWorld(), base.getX(), base.getY(), base.getZ());
        switch (this) {
            case NORTH -> result.add(0, 0, -1);
            case SOUTH -> result.add(0, 0, 1);
            case EAST -> result.add(1, 0, 0);
            case WEST -> result.add(-1, 0, 0);
        }
        return result;
    }
}
