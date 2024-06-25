package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.Direction;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

class Enemy {
    private boolean alive;
    private int health;
    private int pathIndex;
    private final Entity entity;
    private Location currentBlock;
    private Direction currentDirection;
    private @Nullable DeathType deathType;
    private @Nullable Consumer<Enemy> deathEventHandler;

    /**
     * Spawns a new enemy
     * @param entityType the type of entity that will represent the enemy
     * @param health the starting amount of health the enemy should have
     * @param start  the enemy's starting location
     * @param path   the path that the enemy will follow
     */
    public Enemy(EntityType entityType, int health, Location start, Direction[] path) {
        alive = true;
        this.health = health;
        pathIndex = 0;
        entity = start.getWorld().spawnEntity(start, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        entity.setGravity(false);
        entity.setInvulnerable(true);
        entity.setGlowing(true);
        entity.setVisibleByDefault(true);
        entity.setVisualFire(false);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setAI(false);
            livingEntity.setCanPickupItems(false);
        }
        currentBlock = start;
        currentDirection = path[pathIndex];
        Bukkit.getScheduler().runTaskTimer(TowerDefense.INSTANCE, (task) -> {
            // move until the entity is on a different block, then get the next direction
            if (!entity.teleport(currentDirection.toLocation(entity.getLocation(), .1))) {
                Bukkit.broadcast(Component.text("E: kill because of bad teleport"));
                deathType = DeathType.PATH;
                kill();
                task.cancel();
                return;
            }
            Location loc = entity.getLocation();
            if (
                    loc.getBlockX() != currentBlock.getBlockX()
                    || loc.getBlockZ() != currentBlock.getBlockZ()
            ) {
                pathIndex++;
                currentBlock = loc.clone();
                Bukkit.broadcast(Component.text("E: currentBlock now " + MessageUtils.locationToString(currentBlock)));
                try {
                    currentDirection = path[pathIndex];
                } catch (IndexOutOfBoundsException e) {
                    Bukkit.broadcast(Component.text("E: kill because end of path was reached"));
                    kill();
                    task.cancel();
                }
            }
        }, 0, 1 /* when a mob is teleported every tick, it plays the walking animation */);
    }

    /**
     * Damages the enemy. If health goes below zero, the enemy will be {@link #kill() killed}.
     * @param amount the amount of health to take away
     */
    public void damage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            kill();
        }
    }

    /**
     * Increases the enemy's health. Increasing the health of a dead enemy will have no effect.
     * @param amount the amount of health to restore
     */
    public void heal(int amount) {
        if (alive) {
            health += amount;
        }
    }

    /**
     * Kill the enemy and remove its entity
     * @apiNote death type for the enemy will be {@link DeathType#HEALTH HEALTH}
     */
    public void kill() {
        deathType = DeathType.HEALTH;
        alive = false;
        entity.remove();
        if (deathEventHandler != null) {
            deathEventHandler.accept(this);
        }
    }

    /**
     * Sets the function to run when the enemy is killed. When this function is run, the enemy will already be dead.
     * @param handler the function
     */
    public void onDeath(Consumer<Enemy> handler) {
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
