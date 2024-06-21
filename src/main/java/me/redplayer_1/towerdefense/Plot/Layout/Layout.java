package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Plot.Tower.Tower;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;

public class Layout {
    public static final int SIZE = 11; // in blocks (including border)
    public static String defaultLayout = null;

    private final String name;
    private final Vector3 startLoc; // relative to bottomLeft
    private final BlockMesh mesh; // the prebuilt blocks in the layout
    private final Grid grid;
    private final Direction[] path;
    private LinkedList<Enemy> enemies;
    private LinkedList<Tower> towers;
    private BukkitTask spawner = null;
    private float enemyTickRate;

    /**
     * Creates a new layout.
     * @param name the name of the layout
     * @param startLoc the starting location for all enemies
     * @param mesh the mesh for the layout (<b>must be placed</b>)
     * @param path the path that enemies will take
     * @see Layouts
     */
    public Layout(String name, Vector3 startLoc, BlockMesh mesh, Direction[] path) {
        if (mesh.getBottomLeft() == null) {
            throw new IllegalArgumentException("Bottom left must not be null (mesh needs to be placed first)");
        }
        this.name = name;
        this.startLoc = startLoc;
        this.mesh = mesh;
        grid = new Grid(SIZE, SIZE);
        this.path = path;
        addPathToGrid();
        this.towers = new LinkedList<>();
    }

    /**
     * Adds items to the grid where the path would be to prevent towers from being placed there
     */
    private void addPathToGrid() {
        int x, y;
        x = startLoc.x;
        y = startLoc.z;
        final GridItem pathItem = new GridItem();
        for (Direction dir : path) {
            grid.add(pathItem, x, y);
            switch (dir) {
                case NORTH -> y++;
                case SOUTH -> y--;
                case EAST -> x++;
                case WEST -> x--;
            }
        }
    }

    public void startSpawner() {
        // TODO: handle wave spawning & pass/fail
        spawner = Bukkit.getScheduler().runTaskTimer(TowerDefense.INSTANCE, () -> {
        }, 0, 10);
    }

    public void stopSpawner() {
        if (spawner != null) {
            spawner.cancel();
        }
    }

    public void spawnEnemy() {
        Entity e = mesh.getBottomLeft().getWorld().spawnEntity(
                new Location(mesh.getBottomLeft().getWorld(), 0, 0, 0),
                EntityType.BLOCK_DISPLAY
        );
        enemies.add(new Enemy(e, 20, startLoc.add(mesh.getBottomLeft()).toLocation(), path));
    }

    /**
     * Moves the layout (blocks, enemies & towers) to a new location
     * @param bottomLeft the new bottom left location of the layout
     */
    public void move(Location bottomLeft) {
        // TODO: move towers
        stopSpawner();
        while (!enemies.isEmpty()) {
            // TODO: use wave restart method?
            enemies.removeFirst().kill();
        }
        mesh.destroy();
        mesh.place(bottomLeft);
        startSpawner();
    }

    /**
     * Removes all the blocks and towers within the layout and stops the spawner.
     * Any operations preformed on the object afterward are will have undefined behavior
     */
    public void remove() {
        stopSpawner();
        mesh.destroy();
        for (Tower tower : towers) {
            tower.getMesh().destroy();
        }
    }

    /**
     * Places the Tower at a location
     * @param tower the tower to place
     * @param location the location to place the Tower
     * @return if the tower was placed
     */
    public boolean placeTower(Tower tower, Location location) {
        BlockMesh towerMesh = tower.getMesh();
        GridItem towerItem = new Tower.Item(tower, towerMesh.width, towerMesh.depth);
        Vector3 relLoc = this.mesh.toRelativeLocation(location);
        if (towerMesh.canPlace(location) && grid.canAdd(towerItem, relLoc.x, relLoc.z)) {
            towers.add(tower);
            grid.add(towerItem, relLoc.x, relLoc.z);
            towerMesh.place(location);
            tower.setLocation(location);
            return true;
        }
        return false;
    }

    /**
     * Place a tower using the layout's grid to provide a location
     * @param tower the tower to place
     * @param gridX the grid x-coordinate to put the tower at
     * @param gridY the grid y-coordinate to put the tower at
     * @return if the tower was placed
     */
    public boolean placeTower(Tower tower, int gridX, int gridY) {
        return placeTower(tower, mesh.fromRelativeLocation(new Vector3(gridX, 1, gridY), mesh.getBottomLeft().getWorld()));
    }

    /**
     * @return a list of all towers that are placed on the layout
     */
    public LinkedList<Tower> getTowers() {
        return towers;
    }

    /**
     * Removes the tower at the location. Fails silently if the location isn't in the layout or
     * if there isn't a tower at the location
     * @param location the location of the tower (relative to the world's 0, 0)
     * @return if the tower was removed
     */
    public @Nullable Tower removeTower(Location location) {
        Vector3 rel = mesh.toRelativeLocation(location);
        // check if the location is within the layout
        if (rel.x < 0 || rel.y < 1 || rel.z < 0 || rel.x >= mesh.width || rel.z >= mesh.depth) return null;
        if (grid.get(rel.x, rel.z) instanceof Tower.Item item) {
            grid.remove(rel.x, rel.z);
            BlockMesh towerMesh = item.getTower().getMesh();
            towerMesh.setBottomLeft(mesh.fromRelativeLocation(new Vector3(item.gridX, 1, item.gridY), location.getWorld()));
            towerMesh.destroy();
            return item.getTower();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public BlockMesh getMesh() {
        return mesh;
    }

    public Vector3 getStartLocation() {
        return startLoc;
    }

    public Direction[] getPath() {
        return path;
    }

    public Grid getGrid() {
        return grid;
    }

    /**
     * Serializes this layout into a new child section (with this layout's name) of the root section
     * @param rootSection the section to store the serialized data in
     */
    public void serialize(ConfigurationSection rootSection) {
        ConfigurationSection section = rootSection.createSection(name);
        startLoc.serialize(section.createSection("startLoc"));
        mesh.serialize(section, "blockMesh");
        section.set("path", Arrays.stream(path).map(Enum::name).toList());
        section.set("tickRate", enemyTickRate);
    }


    /**
     * Deserializes a layout from the provided config section
     * @param section the child section that the layout was serialized into (section name == layout name)
     * @param meshBottomLeft the value to set the Layout's BlockMesh's bottomLeft to if it isn't already set
     * @return  the deserialized layout
     * @throws InvalidConfigurationException if required values/sections were missing
     */
    public static Layout deserialize(ConfigurationSection section, @Nullable Location meshBottomLeft) throws InvalidConfigurationException {
        if (!section.isConfigurationSection("startLoc") || !section.isConfigurationSection("blockMesh")) {
            throw new InvalidConfigurationException("Serialized layout is missing required sections");
        }
        BlockMesh mesh = BlockMesh.deserialize(section.getConfigurationSection("blockMesh"));
        if (mesh.getBottomLeft() == null) {
            mesh.setBottomLeft(meshBottomLeft);
        }
        Layout layout = new Layout(
                section.getName(),
                Vector3.deserialize(section.getConfigurationSection("startLoc")),
                mesh,
                section.getStringList("path").stream().map(Direction::valueOf).toArray(Direction[]::new)
        );
        if (section.contains("tickRate")) {
            layout.enemyTickRate = Float.parseFloat(section.getString("tickRate", "0"));
        }
        return layout;
    }

    /**
     * Load all optional config values
     * @param section the ConfigurationSection containing the values
     */
    public static void loadConfigValues(ConfigurationSection section) {
        String defaultLayoutName = section.getString("default_layout");
        if (defaultLayoutName != null) {
            if (Layouts.isTemplate(defaultLayoutName)) {
                Layout.defaultLayout = defaultLayoutName;
            } else {
                MessageUtils.logConsole("Invalid default layout \"" + defaultLayoutName + "\" in config", LogLevel.ERROR);
            }
        }
    }

    public static void saveConfigValues(ConfigurationSection section) {
        if (defaultLayout != null) {
            section.set("default_layout", defaultLayout);
        }
    }


}
