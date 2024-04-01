package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Plot.Direction;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.LinkedList;

// TODO: add tower list
public class Layout {
    public static final int SIZE = 11; // in blocks (including border)
    private static HashMap<String, Layout> layouts = new HashMap<>();
    public static Layout defaultLayout = null; // TODO: make config val (name)

    private final Location startLoc;
    private final BlockMesh mesh;
    private final Direction[] path;
    private LinkedList<Enemy> enemies;
    private int level;

    // creates new layout & adds it as a template (enemies & level uninitialized)
    protected Layout(String name, Location startLoc, BlockMesh mesh, Direction[] path) {
        this.startLoc = startLoc;
        this.mesh = mesh;
        this.path = path;
        layouts.put(name, this);
    }

    // get layout from template
    public Layout(String name /* not case-sensitive */, int level) throws NoLayoutFoundException {
        Layout layout = layouts.get(name.toLowerCase());
        if (layout == null) throw new NoLayoutFoundException();

        startLoc = layout.startLoc;
        mesh = layout.mesh;
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

    /**
     * Place all the blocks for the plot in the world, using the bottomLeft coordinate and layout size to determine
     * relative locations
     *
     * @param bottomLeft the bottom-left coordinate of the plot placement (-x, +z)
     */
    public void place(Location bottomLeft) {
        mesh.place(bottomLeft);
    }

    /**
     * Removes all the blocks and towers within the layout
     */
    public void remove() {
        mesh.destroy();
    }

    public void serialize(ConfigurationSection section) {
        //TODO
    }

    public Layout deserialize(ConfigurationSection section) {
        //TODO
        return null;
    }

}
