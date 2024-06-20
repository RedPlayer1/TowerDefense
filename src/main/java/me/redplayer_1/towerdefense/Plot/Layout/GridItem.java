package me.redplayer_1.towerdefense.Plot.Layout;

/**
 * Represents something that can be added to a Layout's grid (i.e. added to the layout)
 */
public class GridItem {
    public final int width; // x length
    public final int height; // y length
    /** The item's x index in the grid */
    public int gridX;
    /** The item's y index in the grid */
    public int gridY;

    /**
     * Creates a new GridItem with a width and height of 1.
     */
    public GridItem() {
        this(1, 1);
    }

    /**
     * Creates a new GridItem. Every item has a width and height that must be greater than zero.
     * @param width the number of blocks in the x-direction the item will occupy
     * @param height the number of blocks in the y-direction the item will occupy
     */
    public GridItem(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be greater than zero");
        }
        this.width = width;
        this.height = height;
    }
}