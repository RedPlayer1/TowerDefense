package me.redplayer_1.towerdefense.Plot.Placeable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A block or structure that can be placed on a Plot
 */
public interface Placeable {
    /**
     * Place the placeable on the plot
     *
     * @param location the location the player clicked
     * @return if the placeable was placed (ex. return false if there wasn't enough room)
     */
    boolean place(Location location);

    default ItemStack asItem() {
        return new ItemStack(Material.AIR);
    }
}
