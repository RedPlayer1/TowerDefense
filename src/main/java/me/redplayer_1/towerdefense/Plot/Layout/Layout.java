package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Exception.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Plot.Direction;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

// TODO: add tower list
public class Layout {
    public static final int SIZE = 11; // in blocks (including border)
    private static final LinkedList<Layout> layouts = new LinkedList<>();
    public static Layout defaultLayout = null; // TODO: make config val (name)

    private final String name;
    private final Location startLoc;
    private final BlockMesh mesh;
    private final Direction[] path;
    private LinkedList<Enemy> enemies;
    private int level;

    // creates new layout & adds it as a template (enemies & level uninitialized)
    protected Layout(String name, Location startLoc, BlockMesh mesh, Direction[] path) {
        this.name = name;
        this.startLoc = startLoc;
        this.mesh = mesh;
        this.path = path;
        layouts.add(this);
    }

    // get layout from template
    public Layout(String name, int level) throws NoLayoutFoundException {
        Layout layout = null;
        for (Layout l : layouts) {
            if (l.name.equals(name)) layout = l;
        }
        if (layout == null) throw new NoLayoutFoundException();

        this.name = layout.name;
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

    public String getName() {
        return name;
    }

    /**
     * @return a list of all the registered layouts and their names
     */
    public static LinkedList<Layout> getLayouts() {
        return layouts;
    }

    /**
     * @param name the name of the layout to find
     * @return the layout with that name, or null if it doesn't exist
     */
    public static @Nullable Layout getLayout(String name) {
        for (Layout layout : layouts) {
            if (layout.name.equals(name)) {
                return layout;
            }
        }
        return null;
    }

    public void serialize(ConfigurationSection section) {
        //FIXME
    }

    public static Layout deserialize(ConfigurationSection section) {
        //FIXME
        return null;
    }

}
