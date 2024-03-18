package me.redplayer_1.towerdefense.Plot.Placeable;

import me.redplayer_1.towerdefense.Util.BlockMesh;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Tower implements Placeable {
    private ItemStack item;
    private int range; /* Range.Unlimited, Range.None */
    private BlockMesh mesh;

    /*
    Use offensive/defensive(or support) tower subclasses?
     */
    public int attack(/* Enemy */) {
        return 0; /* damage to deal*/
    }

    @Override
    public boolean place(Location location) {
        if (mesh.canPlace(location)) {
            mesh.place(location);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack asItem() {
        return item;
    }
}
