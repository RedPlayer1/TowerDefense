package me.redplayer_1.towerdefense.Plot.Tower;

import com.destroystokyo.paper.ParticleBuilder;
import me.redplayer_1.towerdefense.Geometry.BlockMesh;
import me.redplayer_1.towerdefense.Geometry.Vector3;
import me.redplayer_1.towerdefense.Plot.Layout.Enemy;
import me.redplayer_1.towerdefense.Plot.Layout.Grid;
import me.redplayer_1.towerdefense.Plot.Layout.GridItem;
import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.ItemUtils;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class Tower {
    public static final int RANGE_UNLIMITED = -1;
    public static final int RANGE_NONE = 0;
    private static final NamespacedKey ID_KEY = new NamespacedKey(TowerDefense.INSTANCE, "tower_id");
    public final String name;
    private ItemStack item;
    private BlockMesh mesh;
    private final LinkedList<Integer> accessiblePathIndices;
    private final Vector3 particlePoint;
    private final Particle particle;
    private int range;
    private int damage;
    private final int cost;
    private final int targets;
    private final int attackDelay;
    private int cooldown;

    /**
     * Create a new tower
     * @param name the name of the tower
     * @param item the placeable item that represents the tower
     * @param mesh the tower's blocks
     * @param particle the particle displayed when the tower attacks
     * @param particlePoint the relative location within the mesh where attack particles will originate
     * @param range the tower's range (range is a cube, not spherical)
     * @param damage the base damage the tower deals
     * @param cost the cost to purchase the tower
     * @param targets the maximum number of enemies that the tower should damage every time it attacks
     * @param attackDelay number of ticks before the tower can attack enemies
     */
    public Tower(@NotNull String name, @NotNull ItemStack item, BlockMesh mesh, Particle particle, Vector3 particlePoint, int range, int damage, int cost, int targets, int attackDelay) {
        this.name = name;
        setItem(item);
        this.mesh = mesh;
        this.particle = particle;
        this.particlePoint = particlePoint;
        this.range = range;
        this.damage = damage;
        this.cost = cost;
        this.targets = targets;
        this.attackDelay = attackDelay;
        cooldown = attackDelay;
        accessiblePathIndices = new LinkedList<>();
    }

    /**
     * Attack/damage the enemy and reset the attack cooldown.
     * @param enemy the enemy to attack
     * @apiNote this method does not check if the tower's cooldown has expired or if the enemy is in range
     * @throws IllegalStateException if the tower's mesh hasn't been placed yet
     * @implSpec the attacked enemy must be alive and in the same world as the tower
     */
    public void attack(Enemy enemy) {
        attack(enemy, null, 0, false);
    }

    /**
     * Attacks all enemies that are within range and shows a line of particles from the tower to the attacked enemy.
     * @param enemies the enemies to attempt to attack
     * @param owner the player that particles are shown to
     * @param particleSpacing the amount of space between each spawned particle
     * @apiNote this method does not check if the tower's cooldown has expired or if the enemy is in range
     * @throws IllegalStateException if the tower's mesh hasn't been placed yet
     * @implSpec the attacked enemy must be alive and in the same world as the tower
     */
    public void attack(List<Enemy> enemies, Player owner, double particleSpacing) {
        int attacked = 0;
        for (Enemy e : enemies) {
            if (attacked >= targets) return;
            if (accessiblePathIndices.contains(e.getPathIndex())) {
                attack(e, owner, particleSpacing, true);
                attacked++;
            }
        }
    }

    /**
     * Attacks all enemies that are within range
     * @param enemies the enemies to attempt to attack
     */
    public void attack(List<Enemy> enemies) {
        int attacked = 0;
        for (Enemy e : enemies) {
            if (attacked > targets) return;
            if (accessiblePathIndices.contains(e.getPathIndex())) {
                attack(e, null, 0, false);
                attacked++;
            }
        }
    }

    private void attack(Enemy enemy, Player owner, double particleSpacing, boolean spawnParticles) {
        Location enemyLoc = enemy.getEntity().getLocation().toCenterLocation();
        Location startLoc = mesh.fromRelativeLocation(particlePoint, enemyLoc.getWorld()).toCenterLocation();
        if (mesh.getBottomLeft() == null) {
            throw new IllegalStateException("The tower must be placed before it can attack");
        }
        cooldown = attackDelay;
        enemy.damage(getDamage());
        if (spawnParticles) {
            // https://bukkit.org/threads/tutorial-how-to-calculate-vectors.138849
            double distance = startLoc.distance(enemyLoc);
            Vector vec = enemyLoc.clone().subtract(startLoc).toVector().normalize().multiply(particleSpacing);
            ParticleBuilder spawner = new ParticleBuilder(particle)
                    .receivers(owner)
                    .location(startLoc)
                    .count(0)
                    .offset(0, 0, 0);
            for (double pos = 0; pos < distance; pos += particleSpacing) {
                // noinspection ConstantConditions
                spawner.location().add(vec);
                spawner.spawn();
            }
        }
    }

    /**
     * @return if the tower's cooldown has expired and it can attack
     */
    public boolean canAttack() {
        return cooldown <= 0;
    }

    /**
     * To be run every tick. Used to decrement the attack cooldown.
     */
    public void tick() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    /**
     * Determine which parts of the layout's path are within the range of the tower
     * @param x the tower's x-coordinate in the grid
     * @param y the tower's y-coordinate in the grid
     * @param grid the layout's grid
     */
    public void computeAccessiblePathIndices(int x, int y, Grid grid) {
        // make coords correspond to the tower's particle point
        accessiblePathIndices.clear();
        x += particlePoint.x;
        y += particlePoint.z;

        grid.forItemArea(x - range, y - range, range * 2, range * 2, (item) -> {
            if (item instanceof Layout.PathItem pathItem) {
                accessiblePathIndices.add(pathItem.index);
            }
            return item;
        });
    }

    public int getTargets() {
        return targets;
    }

    public int getAttackDelay() {
        return attackDelay;
    }

    public Vector3 getParticlePoint() {
        return particlePoint;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getCost() {
        return cost;
    }

    public void setItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.asMiniMessage(name)); // TODO: have plain & colored name
        meta.getPersistentDataContainer().set(ID_KEY, PersistentDataType.STRING, name);
        item.setItemMeta(meta);
        item.setAmount(1);
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


    /**
     * Calculates the amount of damage the tower will deal to enemies, factoring in any modifiers.
     * @return the amount of damage the tower will deal at the time of the method call.
     */
    public int getDamage() {
        return damage;
    }

    public BlockMesh getMesh() {
        return mesh;
    }

    public void setMesh(BlockMesh mesh) {
        this.mesh = mesh;
    }

    /**
     * Denotes that the item in a grid is a tower
     */
    public static class Item extends GridItem {
        private final Tower tower;
        /**
         * {@link GridItem#GridItem(int, int) GridItem(width, height)}
         * @param tower the tower that this item represents
         */
        public Item(Tower tower, int width, int height) {
            super(width, height);
            this.tower = tower;
        }

        public Tower getTower() {
            return tower;
        }
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
                    Location blockLoc = event.getBlockPlaced().getLocation();
                    blockLoc.getWorld().setType(blockLoc, Material.AIR);
                    // cancel the event if the new tower would overlap with existing ones or isn't in the player's plot
                    event.setCancelled(
                            !tdPlayer.getPlot().getLayout().placeTower(tower, blockLoc)
                    );
                } else {
                    event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_ANCIENT_DEBRIS_FALL, 1, .83f);
                    MessageUtils.log(event.getPlayer(), "This item has an invalid tower id! Please report to staff", LogLevel.ERROR);
                    event.setCancelled(true);
                }
            }
        }

        @EventHandler
        public void onBlockClick(BlockDamageEvent event) {
            TDPlayer player = TDPlayer.of(event.getPlayer());
            if (player != null) {
                Tower tower = player.getPlot().getLayout().removeTower(event.getBlock().getLocation());
                if (tower != null) {
                    ItemUtils.giveOrDrop(player.getPlayer(), tower.getItem());
                }
            }
        }
    }
}
