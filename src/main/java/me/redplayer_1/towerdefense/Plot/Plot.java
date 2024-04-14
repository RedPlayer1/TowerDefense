package me.redplayer_1.towerdefense.Plot;

import me.redplayer_1.towerdefense.Exception.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Exception.NotEnoughPlotSpaceException;
import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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
    private static final BlockMesh EMPTY_PLOT_MESH = new BlockMesh(Layout.SIZE, Layout.SIZE, 1);
    static {
        EMPTY_PLOT_MESH.fillMesh(Material.STONE_BRICKS); // TODO: make config value
    }
    private static final World PLOT_WORLD = Bukkit.getWorld("world"); //TODO: make config value
    private static Plot[][] plots = new Plot[plotGridSize][plotGridSize]; // [y][x]
    private static Location gridOrigin = null; // (0, 0) in relative coordinates TODO: make config value
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
     * @param layout The plot's layout. If null, the default layout will be used
     * @throws NotEnoughPlotSpaceException if the plot grid is full or the grid's origin isn't set
     * @throws NoLayoutFoundException if the {@link Layout#defaultLayout default layout} should be used
     * (layout param is null) but it isn't set
     */
    public Plot(@Nullable Layout layout) throws NotEnoughPlotSpaceException, NoLayoutFoundException {
        if (gridOrigin == null) {
            throw new NotEnoughPlotSpaceException("Grid origin not initialized");
        }
        if (usedPlots > plotGridSize * plotGridSize) {
            throw new NotEnoughPlotSpaceException("Not enough plots");
        }
        if (layout == null) {
            if (Layout.defaultLayout == null) {
                throw new NoLayoutFoundException("The default layout isn't set");
            } else {
                this.layout = Layout.defaultLayout;
            }
        } else {
            this.layout = layout;
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
     *
     * @param clearBlocks if the blocks that represented the plot should be cleared (set to air)
     */
    public void clear(boolean clearBlocks) {
        plots[y][x] = null;
        if (clearBlocks) {
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
        return new Location(PLOT_WORLD, x * Layout.SIZE, gridOrigin.getBlockY(), y * Layout.SIZE);
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
    }

    /**
     * Deserialize a plot from the ConfigurationSection
     * @param section the section containing the serialized plot
     * @return the deserialized plot
     * @throws NotEnoughPlotSpaceException if the new plot cannot be created because there isn't enough room in the grid
     * @throws NoLayoutFoundException if the plot's layout doesn't exist or if the config value is missing
     */
    public static Plot deserialize(ConfigurationSection section) throws NotEnoughPlotSpaceException, NoLayoutFoundException {
        String layoutName = section.getString("layout");
        if (layoutName == null) throw new NoLayoutFoundException("Layout name isn't in the config");
        Layout plotLayout =  Layout.getLayout(layoutName);
        if (plotLayout == null) throw new NoLayoutFoundException("No layout named \"" + layoutName + "\" exists");
        return new Plot(plotLayout);
    }

    public static void setPlotGridOrigin(Location origin) {
        gridOrigin = origin;

        // remove all player towers & plot/layout blocks, then place them according to the new origin
        for (int y = 0; y < plotGridSize; y++) {
            for (int x = 0; x < plotGridSize; x++) {
                Plot plot = plots[y][x];
                plot.layout.remove();
                plot.layout.place(plot.getBottomLeft());
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
