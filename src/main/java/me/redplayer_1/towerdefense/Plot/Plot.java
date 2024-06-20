package me.redplayer_1.towerdefense.Plot;

import me.redplayer_1.towerdefense.Exception.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Exception.NotEnoughPlotSpaceException;
import me.redplayer_1.towerdefense.Plot.Layout.GridItem;
import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Layout.Layouts;
import me.redplayer_1.towerdefense.Plot.Tower.Tower;
import me.redplayer_1.towerdefense.Plot.Tower.Towers;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.LocationUtils;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * A square area (width & length = Layout.SIZE, y is undefined) consisting of a bottom layer of filler blocks, then
 * the Layout design, then any Placeables (rather, their block representations).
 * Plots are positioned in a grid and have a set of relative x, y coordinates. Operations like relocating the origin
 * of such grid can be quite resource intensive, and, since they use 'for' loops, can momentarily freeze the server.
 *
 * @see Layout#SIZE
 */
public class Plot {
    private static int plotGridSize = 10;
    private static Material emptyPlotFillerType = Material.STONE_BRICKS;
    private static final BlockMesh EMPTY_PLOT_MESH = new BlockMesh(Layout.SIZE, Layout.SIZE, 1);
    static {
        EMPTY_PLOT_MESH.fillMesh(emptyPlotFillerType);
    }
    private static Plot[][] plots = new Plot[plotGridSize][plotGridSize]; // [y][x]
    private static Location gridOrigin = null; // (0, 0) in relative coordinates
    private static int usedPlots = 0;

    private Layout layout;
    private int x;
    private int y;

    /**
     * Creates a new plot using the default layout
     * @throws NotEnoughPlotSpaceException if the plot grid is full or the grid's origin isn't set
     * @throws NoLayoutFoundException if no default layout exists
     */
    public Plot() throws NotEnoughPlotSpaceException, NoLayoutFoundException {
        this(null);
    }

    /**
     * Create a new plot and add it to the plot grid
     * @param layoutName The name plot's layout. If null, the default layout will be used
     * @throws NotEnoughPlotSpaceException if the plot grid is full or the grid's origin isn't set
     * @throws NoLayoutFoundException if the {@link Layout#defaultLayout default layout} should be used
     * (layout param is null) but it isn't set
     */
    public Plot(@Nullable String layoutName) throws NotEnoughPlotSpaceException, NoLayoutFoundException {
        if (gridOrigin == null || gridOrigin.getWorld() == null) {
            throw new NotEnoughPlotSpaceException("Grid origin not initialized");
        }
        if (usedPlots > plotGridSize * plotGridSize) {
            throw new NotEnoughPlotSpaceException("Not enough plots");
        }
        for (int y = 0; y < plotGridSize; y++) {
            for (int x = 0; x < plotGridSize; x++) {
                if (plots[y][x] != null) {
                    this.x = x;
                    this.y = y;
                    usedPlots++;
                    plots[y][x] = this;
                }
            }
        }
        if (layoutName == null) {
            if (Layout.defaultLayout == null) {
                throw new NoLayoutFoundException("The default layout isn't set");
            } else {
                layoutName = Layout.defaultLayout;
            }
        }
        layout = Layouts.getLayout(layoutName, getBottomLeft());

        Bukkit.broadcast(Component.text("NEW PLOT CREATE w/ Layout " + (this.layout != null? this.layout.getName() : "null") + " @ " + MessageUtils.locationToString(getBottomLeft())));
    }

    /**
     * Creates a blank space that a plot can be put in later (only 1-y thick)
     *
     * @param bottomLeft the bottom left corner of the top of the plot space
     */
    private void placeBlankPlot(Location bottomLeft) {
        EMPTY_PLOT_MESH.place(bottomLeft);
    }

    /**
     * Clears the plot from the grid, including all player towers
     * @param clearBlocks if the blocks that represented the plot should be cleared (set to air)
     */
    public void clear(boolean clearBlocks) {
        plots[y][x] = null;
        if (clearBlocks) {
            layout.remove();
            EMPTY_PLOT_MESH.setBottomLeft(getBottomLeft());
            EMPTY_PLOT_MESH.destroy();
        }
    }

    /**
     * @return the uppermost y-level that is occupied by blocks of the plot/layout (add 1 for first valid air block)
     */
    public int getTopY() {
        return gridOrigin.getBlockY() + 1; // allow layouts that are 1+ blocks thick?
    }

    /**
     * @return the origin (0, 0) of the plot grid
     */
    public static Location getGridOrigin() {
        return gridOrigin;
    }

    /**
     * @return the bottom left coordinate of the plot
     */
    public Location getBottomLeft() {
        return new Location(
                gridOrigin.getWorld(),
                gridOrigin.getBlockX() + x * Layout.SIZE,
                gridOrigin.getBlockY(),
                gridOrigin.getBlockZ() + y * Layout.SIZE
        );
    }

    public Layout getLayout() {
        return layout;
    }

    /**
     * Teleports the player to the bottom left corner of the plot
     *
     * @param player the player to teleport
     */
    public void teleportPlayer(Player player) {
        Location loc = getBottomLeft();
        loc.setY(getTopY() + 1);
        player.teleport(loc);
    }

    /**
     * Serialize the plot within a ConfigurationSection
     */
    public void serialize(ConfigurationSection section) {
        section.set("layout", layout.getName());

        // map tower's name to its x,y location in the layout's grid
        HashMap<String, LinkedList<String>> towerData = new HashMap<>();
        System.out.println("SAVING LAYOUT GRID: " + layout.getGrid());
        final GridItem[][] gridItems = layout.getGrid().getInternalArray();
        for (int y = 0; y < gridItems.length; y++) {
            for (int x = 0; x < gridItems[0].length; x++) {
                GridItem item = gridItems[y][x];
                if (item instanceof Tower.Item towerItem) {
                    System.out.println("FOUND TOWER: " + towerItem.getTower().name + " @" + x + ", " + y);
                    String name = towerItem.getTower().name;
                    if (!towerData.containsKey(name)) {
                        towerData.put(name, new LinkedList<>());
                    }
                    towerData.get(name).add(x + "," + y);
                }
            }
        }
        section.set("towers", towerData);
    }

    /**
     * Deserialize a plot from a ConfigurationSection. Invalid tower data will be logged to the console while
     * unrecoverable errors will be thrown.
     * @param section the section containing the serialized plot
     * @return the deserialized plot
     * @throws NotEnoughPlotSpaceException if the new plot cannot be created because there isn't enough room in the grid
     * @throws NoLayoutFoundException if the plot's layout doesn't exist or if the config value is missing
     * @throws InvalidConfigurationException if a required Yaml section/value doesn't exist
     */
    public static Plot deserialize(ConfigurationSection section) throws NotEnoughPlotSpaceException, NoLayoutFoundException, InvalidConfigurationException {
        // get layout & create plot
        String layoutName = section.getString("layout");
        if (!Layouts.isTemplate(layoutName)) throw new NoLayoutFoundException("Layout name isn't in the config");
        Plot plot = new Plot(layoutName);
        // load & place towers
        ConfigurationSection towerSection = section.getConfigurationSection("towers");
        if (towerSection == null) {
            throw new InvalidConfigurationException("Config section for towers doesn't exist");
        }
        for (String key : towerSection.getKeys(false)) {
            Tower tower = Towers.get(key);
            if (tower != null) {
                for (String locStr : towerSection.getStringList(key)) {
                    String[] coords = locStr.split(",");
                    if (coords.length == 2) {
                        try {
                            if (!plot.layout.placeTower(tower, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]))) {
                                MessageUtils.logConsole("Couldn't place tower from plot config", LogLevel.ERROR);
                            }
                        } catch (NumberFormatException e) {
                            MessageUtils.logConsole(
                                    "Coordinates for tower in plot config aren't valid integers",
                                    LogLevel.WARN
                            );
                        }
                    } else {
                        MessageUtils.logConsole("Plot contains tower with bad coordinates: " + key, LogLevel.WARN);
                    }
                }
            } else {
                MessageUtils.logConsole("Plot contains non-existent tower named \"" + key + "\"", LogLevel.WARN);
            }
        }
        return plot;
    }

    public static void setPlotGridOrigin(Location origin) {
        gridOrigin = origin;

        // remove all player towers & plot/layout blocks, then place them according to the new origin
        for (int y = 0; y < plotGridSize; y++) {
            for (int x = 0; x < plotGridSize; x++) {
                Plot plot = plots[y][x];
                if (plot != null) {
                    plot.layout.move(plot.getBottomLeft());
                }
            }
        }
    }

    /**
     * @return the size (length & width) of the plot grid
     */
    public static int getPlotGridSize() {
        return plotGridSize;
    }

    /**
     * Enlarges the current plot grid to a new and copies over the old plots.
     *
     * @param size the width and length of the new grid
     */
    public static void resizePlotGrid(int size) {
        plotGridSize = size;
        Plot[][] newPlots = new Plot[plotGridSize][plotGridSize];
        for (int y = 0; y < plots.length; y++) {
            System.arraycopy(plots[y], 0, newPlots[y], 0, plots.length);
        }
        plots = newPlots;
    }

    public static void setEmptyPlotFillerType(Material type) {
        emptyPlotFillerType = type;
        EMPTY_PLOT_MESH.fillMesh(type);
    }

    public static void loadConfigValues(ConfigurationSection section) {
        String filler_type = section.getString("empty_plot_filler_type");
        int size = section.getInt("plot_grid_size");

        if (filler_type != null) {
            try {
                Material material = Material.valueOf(filler_type);
                Plot.setEmptyPlotFillerType(material);
            } catch (IllegalArgumentException e) {
                MessageUtils.logConsole("Bad material name for \"empty_plot_filler_type\" in Config.yml", LogLevel.WARN);
            }
        }
        Location origin = LocationUtils.deserialize(section.getConfigurationSection("plot_grid_origin"));
        if (origin != null) {
            gridOrigin = origin;
        } else {
            MessageUtils.logConsole("Bad location data for \"grid_origin\" in Config.yml", LogLevel.WARN);
        }
        if (size != 0) {
            resizePlotGrid(size);
        }

    }

    public static void saveConfigValues(ConfigurationSection section) {
        section.set("empty_plot_filler_type", emptyPlotFillerType.name());
        if (isPlotSpaceInitialized()) {
            LocationUtils.serialize(gridOrigin, section, "plot_grid_origin");
        }
        section.set("plot_grid_size", plotGridSize);
    }

    /**
     * @return if the plot grid has a set origin (if plots can be created)
     */
    public static boolean isPlotSpaceInitialized() {
        return gridOrigin != null;
    }

    /**
     * @return the number of plots that are currently in use
     */
    public static int getUsedPlots() {
        return usedPlots;
    }
}
