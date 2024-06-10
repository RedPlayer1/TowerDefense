package me.redplayer_1.towerdefense.Plot.Layout;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a 2d (x, y) grid of blocks that can be occupied with grid items
 */
public class Grid {
    private GridItem[][] items; // [y][x]

    /**
     * Add an item to the grid
     * @param item the item to add
     * @param x the bottom left x-coordinate of the item
     * @param y the bottom left y-coordinate of the item
     * @throws IndexOutOfBoundsException if the item doesn't fit in the grid
     */
    public void add(GridItem item, int x, int y) {
        if (!isWithinBounds(x, y, item.width, item.height)) {
            throw new IndexOutOfBoundsException();
        }
        // fill the item's occupied area with references to it's bottom-left corner
        ItemReference ref = new ItemReference(x, y);
        forItemArea(x, y, (i) -> ref);
        items[y][x] = item;
    }

    /**
     * Remove an item from the grid
     * @param x the x-coordinate of the item
     * @param y the y-coordinate of the item
     * @throws IndexOutOfBoundsException if the (x, y) coords are not within the grid
     */
    public void remove(int x, int y) {
        GridItem item = items[y][x];
        if (item instanceof ItemReference ref) {
            item = items[ref.y][ref.x];
        }
        forItemArea(x, y, (i) -> null);
    }

    /**
     * Removes the first occurrence of the item from the grid
     * @param item the item to remove
     */
    public void remove(GridItem item) {
        for (int y = 0; y < items.length; y++) {
            for (int x = 0; x < items[0].length; x++) {
                if (items[y][x] == item) {
                    remove(x, y);
                    return;
                }
            }
        }
    }

    /**
     * Gets the item at an (x, y) coordinate
     * @param x the item's x-coordinate
     * @param y the item's y-coordinate
     * @return the item at that location in the grid
     * @throws IndexOutOfBoundsException if x or y are not in the grid
     */
    public @Nullable GridItem get(int x, int y) {
        GridItem item = items[y][x];
        if (item instanceof ItemReference ref) {
            item = items[ref.y][ref.x];
        }
        return item;
    }

    /**
     * Loops over all the grid cells that the item covers and sets its value to that returned by {@code iter}
     */
    private void forItemArea(int x, int y, Function<@Nullable GridItem, @Nullable GridItem> iter) {
        GridItem item = items[y][x];
        for (int i = 0; i < item.height; i++) {
            for (int j = 0; j < item.width; j++) {
                items[y + i][x + j] = iter.apply(items[y + i][x + j]);
            }
        }
    }

    private boolean isWithinBounds(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x + width < items[0].length && y + height < items.length;
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < items[0].length && y < items.length;
    }

    /**
     * Refers to a grid item
     */
    private static class ItemReference extends GridItem {
        public final int x, y;

        public ItemReference(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
