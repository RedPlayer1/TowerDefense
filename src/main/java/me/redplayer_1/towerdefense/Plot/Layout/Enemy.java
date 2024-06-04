package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.Direction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

class Enemy {
    private boolean alive;
    private int health;
    private int pathIndex;
    private Entity entity;
    private Location currentBlock;
    private Direction currentDirection;

    /**
     * Spawns a new enemy
     * @param entity the spawned entity that will represent the enemy
     * @param health the starting amount of health the enemy should have
     * @param start  the enemy's starting location
     * @param path   the path that the enemy will follow
     */
    public Enemy(Entity entity, int health, Location start, Direction[] path) {
        alive = true;
        this.health = health;
        pathIndex = 0;
        this.entity = entity;
        currentBlock = start.clone();
        currentDirection = path[pathIndex];
        entity.teleport(start);
        Bukkit.getScheduler().runTaskTimer(TowerDefense.INSTANCE, (task) -> {
            // move until the entity is on a different block, then get the next direction
            if (!entity.teleport(currentDirection.toLocation(entity.getLocation(), .1))) {
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
                try {
                    currentDirection = path[pathIndex];
                } catch (IndexOutOfBoundsException e) {
                    kill();
                    task.cancel();
                }
            }
        }, 0, 1 /* when a mob is teleported every tick, it plays the walking animation */);
    }

    public void damage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            kill();
        }
    }

    public void heal(int amount) {
        health += amount;
    }

    /**
     * Kill the enemy and remove its entity
     */
    public void kill() {
        alive = false;
        entity.remove();
    }

    /**
     * Checks if the enemy is alive. If dead, both the {@link #damage(int) damage} and {@link #heal(int) heal}
     * operations will do nothing.
     * @return if the enemy is alive
     */
    public boolean isAlive() {
        return alive;
    }
}
