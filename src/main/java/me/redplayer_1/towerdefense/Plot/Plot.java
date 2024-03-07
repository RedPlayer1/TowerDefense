package me.redplayer_1.towerdefense.Plot;

import org.bukkit.configuration.ConfigurationSection;

public class Plot {
    private static final int PLOT_GROUP_SIZE = 10;
    private static final Plot[][] plots = new Plot[PLOT_GROUP_SIZE][PLOT_GROUP_SIZE];
    private static int usedPlots = 0;

    private Layout layout;
    private int x;
    private int y;


    public Plot() throws NotEnoughPlotSpaceException {
        if (usedPlots > PLOT_GROUP_SIZE * PLOT_GROUP_SIZE) throw new NotEnoughPlotSpaceException();

        for (int y = 0; y < PLOT_GROUP_SIZE; y++) {
            for (int x = 0; x < PLOT_GROUP_SIZE; x++) {
                if (plots[y][x] != null) {
                    this.x = x;
                    this.y = y;
                    usedPlots++;
                    plots[y][x] = this;
                }
            }
        }
    }

    public void clear() {
        plots[y][x] = null;
    }

    /**
     * Serialize the plot within a ConfigurationSection
     */
    public void serialize(ConfigurationSection section) {
        // serialize Layout
    }

    public static Plot deserialize(ConfigurationSection section) throws NotEnoughPlotSpaceException {
        Plot plot = new Plot();
        return plot;
    }
}
