package me.redplayer_1.towerdefense.Plot.Layout;

import org.bukkit.entity.Entity;

class Enemy {
    public int pathIndex;
    public Entity entity;

    public Enemy(int pathIndex, Entity entity) {
        this.pathIndex = pathIndex;
        this.entity = entity;
    }
}
