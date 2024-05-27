package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Util.BlockMesh;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Tower {
    public final String name;
    private @Nullable Location location;
    private ItemStack item;
    private int range; /* Range.Unlimited -1, Range.None 0*/
    private int damage;
    private BlockMesh mesh;

    public Tower(String name, ItemStack item, int range, int damage, BlockMesh mesh) {
        this.name = name;
        this.item = item;
        this.range = range;
        this.damage = damage;
        this.mesh = mesh;
        location = null;
    }
    /*
    Use offensive/defensive(or support) tower subclasses?
     */
    public int attack(/* Enemy */) {
        return 0; /* damage to deal*/
    }

    /**
     * Places the Tower at a location
     * @param location the location to place the Tower
     * @return if the Tower can be placed in that location
     */
    public boolean place(Location location) {
        if (mesh.canPlace(location)) {
            mesh.place(location);
            return true;
        }
        return false;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getRange() {
        return range;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public BlockMesh getMesh() {
        return mesh;
    }

    public void setMesh(BlockMesh mesh) {
        this.mesh = mesh;
    }
}
