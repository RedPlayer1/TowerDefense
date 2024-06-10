package me.redplayer_1.towerdefense.Plot.Tower;

import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class Tower {
    private static final NamespacedKey ID_KEY = new NamespacedKey(TowerDefense.INSTANCE, "tower_id");
    public final String name;
    private @Nullable Location location;
    private ItemStack item;
    private int range; /* Range.Unlimited -1, Range.None 0*/
    private int damage;
    private BlockMesh mesh;

    public Tower(String name, ItemStack item, int range, int damage, BlockMesh mesh) {
        this.name = name;
        setItem(item);
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

    public void setItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ID_KEY, PersistentDataType.STRING, name);
        item.setItemMeta(meta);
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    /**
     * @return the location that this tower is
     */
    public @Nullable Location getLocation() {
        return location;
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

    public void setLocation(@Nullable Location location) {
        this.location = location;
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

    public static class EventListener implements Listener {
        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            PersistentDataContainer pdc = event.getItemInHand().getItemMeta().getPersistentDataContainer();
            // check if the placed block is a tower
            if (pdc.has(ID_KEY)) {
                TDPlayer tdPlayer = TDPlayer.of(event.getPlayer());
                if (tdPlayer == null) {
                    MessageUtils.log(event.getPlayer(), "You must have a plot to place towers!", LogLevel.ERROR);
                    event.setCancelled(true);
                    return;
                }
                String id = pdc.get(ID_KEY, PersistentDataType.STRING);
                Tower tower = Towers.get(id);
                if (tower != null) {
                    // cancel the event if the new tower would overlap with existing ones or isn't in the player's plot
                    event.setCancelled(
                            !tdPlayer.getPlot().getLayout().placeTower(tower, event.getBlock().getLocation())
                    );
                } else {
                    event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_ANCIENT_DEBRIS_FALL, 1, .83f);
                    MessageUtils.log(event.getPlayer(), "This item has an invalid tower id! Please report to staff", LogLevel.ERROR);
                    event.setCancelled(true);
                }
            }
        }
    }
}
