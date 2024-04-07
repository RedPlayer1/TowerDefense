package me.redplayer_1.towerdefense.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class LocationUtils {
    /**
     * Serializes this location into values within a new child section
     * @param location the location to serialize (only worldUUID, x, y, and z are included)
     * @param rootSection the parent section for the serialized location's section
     * @param sectionName the name of the child section to be created
     */
    public static void serialize(Location location, ConfigurationSection rootSection, String sectionName) {
        ConfigurationSection section = rootSection.createSection(sectionName);
        section.set("world", location.getWorld().getUID().toString());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
    }

    /**
     * Deserializes a location from a {@link ConfigurationSection}
     * @param locationSection the section containing the location's data (the child section created in
     * {@link LocationUtils#serialize(Location, ConfigurationSection, String) serialize()})
     * @return the deserialized location or null if an error occurred
     */
    public static @Nullable Location deserialize(ConfigurationSection locationSection) {
        try {
            return new Location(
                    Bukkit.getWorld(locationSection.getString("world", "")),
                    locationSection.getDouble("x"),
                    locationSection.getDouble("y"),
                    locationSection.getDouble("z")
            );
        } catch (NullPointerException e) {
            return null;
        }
    }
}
