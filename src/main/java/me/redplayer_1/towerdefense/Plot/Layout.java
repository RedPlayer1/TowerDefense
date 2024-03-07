package me.redplayer_1.towerdefense.Plot;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.LinkedList;

public class Layout {
    public static final int SIZE = 11; // in blocks (including border)
    private static HashMap<String, Layout> layouts = new HashMap<>();

    private final Location startLoc;
    private final Direction[] path;
    private LinkedList<Enemy> enemies;
    private int level;

    // creates new layout & adds it as a template (enemies & level uninitialized)
    protected Layout(String name, Location startLoc, Direction[] path) {
        this.startLoc = startLoc;
        this.path = path;
        layouts.put(name, this);
    }

    // get layout from template
    public Layout(String name /* not case-sensitive */, int level) throws NoLayoutFoundException {
        Layout layout = layouts.get(name.toLowerCase());
        if (layout == null) throw new NoLayoutFoundException();

        startLoc = layout.startLoc;
        path = layout.path;
        enemies = new LinkedList<>();
        this.level = level;
    }

    public void tick() {
        // advance all enemies to the next node
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);

            if (e.pathIndex < path.length - 1) {
                switch (path[e.pathIndex]) {
                    case NORTH -> e.entity.getLocation().add(0, 0, 1);
                    case SOUTH -> e.entity.getLocation().add(0, 0, -1);
                    case EAST -> e.entity.getLocation().add(-1, 0, 0);
                    case WEST -> e.entity.getLocation().add(1, 0, 0);
                }
                e.pathIndex++;
            } else {
                enemies.remove(i);
                i--;
            }
        }
    }

    public void serialize(ConfigurationSection section) {
        //TODO
    }

    public Layout deserialize(ConfigurationSection section) {
        //TODO
        return null;
    }

    private static class Enemy {
        public int pathIndex;
        public Entity entity;

        public Enemy(int pathIndex, Entity entity) {
            this.pathIndex = pathIndex;
            this.entity = entity;
        }
    }
}
