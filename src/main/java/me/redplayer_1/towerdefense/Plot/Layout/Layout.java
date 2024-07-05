package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Geometry.BlockMesh;
import me.redplayer_1.towerdefense.Geometry.Direction;
import me.redplayer_1.towerdefense.Geometry.Vector3;
import me.redplayer_1.towerdefense.Plot.Tower.Tower;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Layout {
    public static final int SIZE = 11; // in blocks (including border)
    public static String defaultLayout = null;

    private final String name;
    private final Vector3 startLoc; // relative to bottomLeft
    private final BlockMesh mesh; // the prebuilt blocks in the layout
    private final Grid grid;
    private final Direction[] path;
    private final LinkedList<Enemy> enemies;
    private final LinkedList<Tower> towers;
    private final WaveManager waveManager;
    private BukkitTask spawner = null;
    private BukkitTask attacker = null;
    private float enemyTickRate;

    /**
     * Creates a new layout.
     * @param name the name of the layout
     * @param startLoc the starting location for all enemies
     * @param mesh the mesh for the layout (<b>must be placed</b>)
     * @param path the path that enemies will take
     * @see Layouts
     */
    public Layout(String name, Vector3 startLoc, BlockMesh mesh, Direction[] path, int wave) {
        if (mesh.getBottomLeft() == null) {
            throw new IllegalArgumentException("Bottom left must not be null (mesh needs to be placed first)");
        }
        this.name = name;
        this.startLoc = startLoc;
        this.mesh = mesh;
        grid = new Grid(SIZE, SIZE);
        this.path = path;
        addPathToGrid();
        waveManager = new WaveManager(wave);
        towers = new LinkedList<>();
        enemies = new LinkedList<>();
    }

    /**
     * Adds items to the grid where the path would be to prevent towers from being placed there
     */
    private void addPathToGrid() {
        int x, y;
        x = startLoc.x;
        y = startLoc.z;
        for (int i = 0; i < path.length; i++) {
            grid.add(new PathItem(i), x, y);
            switch (path[i]) {
                case NORTH -> y++;
                case SOUTH -> y--;
                case EAST -> x++;
                case WEST -> x--;
            }
        }
    }

    public void start(TDPlayer parent) {
        AtomicInteger killed = new AtomicInteger();
        spawner = Bukkit.getScheduler().runTaskTimer(TowerDefense.INSTANCE, () -> {
            if (enemies.size() < waveManager.getEnemyCount()) {
                Enemy enemy = spawnEnemy();
                enemy.setDeathHandler((e) -> {
                    if (e.getDeathType() == Enemy.DeathType.PATH) {
                        // wave failed because enemy reached the end
                        waveManager.setWave(waveManager.getWave() - 1);
                        killAllEnemies(false);
                        killed.set(0);
                        MessageUtils.log(parent.getPlayer(), "Wave failed! Sent back to wave " + waveManager.getWave(), LogLevel.NOTICE);
                    } else {
                        killed.incrementAndGet();
                        parent.giveMoney(waveManager.getEnemyCoinYield());
                    }
                });
                enemies.add(enemy);
            } else if (killed.get() >= waveManager.getEnemyCount()) {
                MessageUtils.log(parent.getPlayer(), "Wave " + waveManager.getWave() + " completed!", LogLevel.SUCCESS);
                killAllEnemies(true);
                killed.set(0);
                waveManager.next();
            }
        }, 0, 20);

        attacker = Bukkit.getScheduler().runTaskTimer(TowerDefense.INSTANCE, () -> {
            for (Tower tower : towers) {
                if (tower.canAttack()) {
                    tower.attack(enemies, parent.getPlayer(), .15);
                }
                tower.tick();
            }
        }, 0, 1);
    }

    /**
     * Stops the spawning of entities and kills existing ones.
     * @apiNote the enemy's death handler will not be triggered when it is killed by this function
     */
    public void stop() {
        if (spawner != null) {
            spawner.cancel();
        }
        if (attacker != null) {
            attacker.cancel();
        }
        killAllEnemies(false);
    }

    private void killAllEnemies(boolean triggerHandler) {
        while (!enemies.isEmpty()) {
            Enemy toRemove = enemies.removeFirst();
            if (!triggerHandler) {
                toRemove.setDeathHandler(null);
            }
            toRemove.kill();
        }
    }

    /**
     * @return if the spawner task is running
     */
    public boolean isSpawnerEnabled() {
        return spawner != null && !spawner.isCancelled();
    }

    /**
     * Spawn a new enemy on the layout
     */
    private Enemy spawnEnemy() {
        Location bL = mesh.getBottomLeft();
        BlockDisplay display = (BlockDisplay) bL.getWorld().spawnEntity(bL, EntityType.BLOCK_DISPLAY);
        display.setBlock(Material.SMOOTH_STONE_SLAB.createBlockData());
        return new Enemy(
                display,
                .6,
                waveManager.getEnemyHealth(),
                startLoc.toLocation(mesh.getBottomLeft().getWorld()).add(mesh.getBottomLeft()).add(0, 1, 0),
                path
        );
    }

    /**
     * Removes all the blocks and towers within the layout and stops the spawner.
     * Any operations preformed on the object afterward are will have undefined behavior
     */
    public void remove() {
        stop();
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
            tower.computeAccessiblePathIndices(relLoc.x, relLoc.z, grid);
            towerMesh.place(location);
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
        assert mesh.getBottomLeft() != null;
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
            towers.remove(item.getTower());
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

    public int getWave() {
        return waveManager.getWave();
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
        // don't serialize wave because it is saved with the player
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
                section.getStringList("path").stream().map(Direction::valueOf).toArray(Direction[]::new),
                1
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

    public static class PathItem extends GridItem {
        public final int index;

        public PathItem(int index) {
            this.index = index;
        }
    }
}
