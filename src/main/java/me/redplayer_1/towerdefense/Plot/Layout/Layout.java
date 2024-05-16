package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Exception.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Plot.Direction;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import me.redplayer_1.towerdefense.Util.Vector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;

public class Layout {
    public static final int SIZE = 11; // in blocks (including border)
    private static final LinkedList<Layout> layouts = new LinkedList<>();
    public static Layout defaultLayout = null; // TODO: make config val (name)

    private final boolean isTemplate;
    private final String name;
    private @Nullable World world; // null if template
    private final Vector3 startLoc; // relative to bottomLeft
    private final BlockMesh mesh;
    private final Direction[] path;
    private LinkedList<Enemy> enemies;
    private LinkedList<Tower> towers;
    private float enemyTickRate; // -1 = n/a (layout is a template)

    // creates new layout & adds it as a template (enemies, level, & towers uninitialized)
    protected Layout(String name, Vector3 startLoc, BlockMesh mesh, Direction[] path, boolean isTemplate) {
        this.name = name;
        world = null;
        this.startLoc = startLoc;
        this.mesh = mesh;
        this.path = path;
        enemyTickRate = -1;
        this.isTemplate = isTemplate;
        if (isTemplate) layouts.add(this);
    }

    // get layout from template
    public Layout(String name, int enemyTickRate, Tower... towers) throws NoLayoutFoundException {
        Layout layout = null;
        for (Layout l : layouts) {
            if (l.name.equals(name)) layout = l;
        }
        if (layout == null) throw new NoLayoutFoundException();
        world = null;
        isTemplate = false;
        this.name = layout.name;
        startLoc = layout.startLoc;
        mesh = layout.mesh;
        path = layout.path;
        enemies = new LinkedList<>();
        this.enemyTickRate = enemyTickRate;
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
        world = bottomLeft.getWorld();
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

    public BlockMesh getMesh() {
        return mesh;
    }

    public Vector3 getStartLocation() {
        return startLoc;
    }

    public Direction[] getPath() {
        return path;
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

    /**
     * Removes the first layout matching that name from the template list
     * @param name the name of the template
     * @return the Layout that was removed (or null if no matches were found)
     */
    public static @Nullable Layout removeLayout(String name) {
        for (int i = 0; i < layouts.size(); i++) {
            if (layouts.get(i).name.equals(name)) {
                return layouts.remove(i);
            }
        }
        return null;
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
     * Deserializes a layout from the provided section
     * @param section the child section that the layout was serialized into (section name == layout name)
     * @param makeTemplate if the resulting layout should be a template
     * @return  the deserialized layout
     * @throws InvalidConfigurationException if required values/sections were missing
     */
    public static Layout deserialize(ConfigurationSection section, boolean makeTemplate) throws InvalidConfigurationException {
        if (!section.isConfigurationSection("startLoc") || !section.isConfigurationSection("blockMesh")) {
            throw new InvalidConfigurationException("Serialized layout is missing required sections");
        }
        Layout layout = new Layout(
                section.getName(),
                Vector3.deserialize(section.getConfigurationSection("startLoc")),
                BlockMesh.deserialize(section.getConfigurationSection("blockMesh")),
                section.getStringList("path").stream().map(Direction::valueOf).toArray(Direction[]::new),
                makeTemplate
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
            Layout defaultLayout = Layout.getLayout(defaultLayoutName);
            if (defaultLayout != null) {
                Layout.defaultLayout = defaultLayout;
            } else {
                MessageUtils.log(Bukkit.getConsoleSender(), "Invalid default layout \"" + defaultLayoutName + "\" in config", LogLevel.ERROR);
            }
        }
    }

    public static void saveConfigValues(ConfigurationSection section) {
        if (defaultLayout != null) {
            section.set("default_layout", defaultLayout.getName());
        }
    }

    /**
     * Load a list of layout templates from a configuration section
     * @param section the section containing the values
     */
    public static void loadLayoutTemplates(ConfigurationSection section) {
        for (String name : section.getKeys(false)) {
            ConfigurationSection namedSection = section.getConfigurationSection(name);
            if (namedSection != null) {
                try {
                    Layout.deserialize(namedSection, true);
                } catch (InvalidConfigurationException e) {
                    MessageUtils.log(Bukkit.getConsoleSender(), "Invalid layout template for Layout \"" + name + "\". Skipping. . .", LogLevel.WARN);
                }
            }
        }
    }
}
