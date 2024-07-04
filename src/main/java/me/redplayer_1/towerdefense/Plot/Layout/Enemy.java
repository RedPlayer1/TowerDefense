package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Geometry.Direction;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Enemy {
    private static final NamespacedKey KEY = new NamespacedKey(TowerDefense.INSTANCE, "enemy");
    private boolean alive;
    private int health;
    private int pathIndex;
    private final Entity entity;
    private final TextDisplay healthDisplay;
    private Location currentBlock;
    private Direction currentDirection;
    private @Nullable DeathType deathType;
    private @Nullable Consumer<Enemy> deathEventHandler;

    /**
     * Spawns a new enemy
     * @param entity the  entity that will represent the enemy
     * @param entityHeight the height of the entity in blocks
     * @param health the starting amount of health the enemy should have
     * @param start  the enemy's starting location
     * @param path   the path that the enemy will follow
     */
    public Enemy(Entity entity, double entityHeight, int health, Location start, Direction[] path) {
        alive = true;
        this.health = health;
        pathIndex = 0;
        currentBlock = start;
        currentDirection = path[pathIndex];

        // ensure entity is set up correctly
        entity.teleport(start);
        entity.setGravity(false);
        entity.setInvulnerable(true);
        entity.setGlowing(true);
        entity.setVisibleByDefault(true);
        entity.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setAI(false);
            livingEntity.setCanPickupItems(false);
        }
        this.entity = entity;

        // initialize health display
        healthDisplay = (TextDisplay) start.getWorld().spawnEntity(start, EntityType.TEXT_DISPLAY);
        healthDisplay.setSeeThrough(false);
        healthDisplay.setBackgroundColor(Color.BLACK);
        Transformation t = healthDisplay.getTransformation();
        t.getTranslation().set(.5, 0, .5);
        t.getRightRotation().setAngleAxis(1.5, -1, 0, 0);
        healthDisplay.setTransformation(t);
        updateHealthDisplay();

        // start movement along path
        Bukkit.getScheduler().runTaskTimer(TowerDefense.INSTANCE, (task) -> {
            // move until the entity is on a different block, then get the next direction
            Location destination = currentDirection.toLocation(entity.getLocation(), .1);
            if (!entity.teleport(destination)) {
                // if the entity doesn't exist, it must have been killed by a tower
                kill(DeathType.HEALTH);
                task.cancel();
                return;
            }
            healthDisplay.teleport(destination.add(0, entityHeight, 0));
            Location loc = entity.getLocation();
            if (
                    loc.getBlockX() != currentBlock.getBlockX()
                    || loc.getBlockZ() != currentBlock.getBlockZ()
            ) {
                pathIndex++;
                currentBlock = loc.clone();
                try {
                    currentDirection = path[pathIndex];
                } catch (IndexOutOfBoundsException e) {
                    kill(DeathType.PATH);
                    task.cancel();
                }
            }
        }, 0, 1 /* when a mob is teleported every tick, it plays the walking animation */);
    }

    private void updateHealthDisplay() {
        healthDisplay.text(MessageUtils.asMiniMessage("<red>" + health + "</red><dark_red>❤</dark_red>"));
    }

    /**
     * @return the entity that represents this enemy
     */
    public Entity getEntity() {
        return entity;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    /**
     * Damages the enemy. If health goes below zero, the enemy will be {@link #kill() killed}.
     * @param amount the amount of health to take away
     */
    public void damage(int amount) {
        if (alive) {
            health -= amount;
            if (health <= 0) {
                health = 0;
                kill();
            } else {
                updateHealthDisplay();
            }
        }
    }

    /**
     * Increases the enemy's health. Increasing the health of a dead enemy will have no effect.
     * @param amount the amount of health to restore
     */
    public void heal(int amount) {
        if (alive) {
            health += amount;
            updateHealthDisplay();
        }
    }

    /**
     * Kill the enemy and remove its entity. Does nothing if the enemy is already dead.
     * @apiNote death type for the enemy will be {@link DeathType#HEALTH HEALTH}
     */
    public void kill() {
        kill(DeathType.HEALTH);
    }

    private void kill(DeathType deathType) {
        this.deathType = deathType;
        if (alive) {
            alive = false;
            entity.remove();
            healthDisplay.remove();
            if (deathEventHandler != null) {
                deathEventHandler.accept(this);
            }
        }
    }

    /**
     * Sets the function to run when the enemy is killed. When this function is run, the enemy will already be dead.
     * @param handler the function
     */
    public void setDeathHandler(@Nullable Consumer<Enemy> handler) {
        deathEventHandler = handler;
    }

    /**
     * Checks if the enemy is alive. If dead, both the {@link #damage(int) damage} and {@link #heal(int) heal}
     * operations will do nothing.
     * @return if the enemy is alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * @return How the enemy died, or null if it is still alive
     */
    public @Nullable DeathType getDeathType() {
        return deathType;
    }


    public enum DeathType {
        /**
         * Death because health was at or below zero
         */
        HEALTH,
        /**
         * Death because the end of the path was reached
         */
        PATH
    }
}
