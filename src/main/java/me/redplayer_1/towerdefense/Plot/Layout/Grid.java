package me.redplayer_1.towerdefense.Plot.Layout;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Represents a 2d (x, y) grid of blocks that can be occupied with grid items
 */
public class Grid {
    private final GridItem[][] items; // [y][x]
    public final int height;
    public final int width;

    public Grid(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be greater than zero");
        }
        items = new GridItem[height][width];
        this.width = width;
        this.height = height;
    }

    /**
     * Add an item to the grid. If another item's area overlaps this item's, the operation will fail.
     * @param item the item to add
     * @param x the bottom left x-coordinate of the item
     * @param y the bottom left y-coordinate of the item
     * @return if the item could be added
     * @throws IndexOutOfBoundsException if the item doesn't fit in the grid
     */
    public boolean add(GridItem item, int x, int y) {
        if (!isWithinBounds(x, y, item.width, item.height)) {
            throw new IndexOutOfBoundsException("Item @ (" + x + ", " + y + ") w: " + item.width + ", h: " + item.height + " doesn't fit in the grid");
        }
        if (!canAdd(item, x, y)) return false;
        // fill the item's occupied area with references to it's bottom-left corner
        ItemReference ref = new ItemReference(x, y);
        forItemArea(x, y, item.width, item.height, (i) -> ref);
        items[y][x] = item;
        item.gridX = x;
        item.gridY = y;
        return true;
    }

    /**
     * Checks if the item can be added to the grid without interfering with other items
     * @param item the item to check
     * @param x the x-coordinate of the test location
     * @param y the y-coordinate of the test location
     * @return if the item can be safely added to the grid
     */
    public boolean canAdd(GridItem item, int x, int y) {
        AtomicBoolean foundOther = new AtomicBoolean();
        AtomicInteger count = new AtomicInteger();
        forItemArea(x, y, item.width, item.height, (i) -> {
            count.incrementAndGet();
            if (i != null) {
                foundOther.set(true);
            }
            return i;
        });
        // count will be less than the area if some locations were off the grid
        return !foundOther.get() && count.get() == item.width * item.height;
    }

    /**
     * Remove an item from the grid
     * @param x the x-coordinate of the item
     * @param y the y-coordinate of the item
     * @throws IndexOutOfBoundsException if the (x, y) coords are not within the grid
     */
    public void remove(int x, int y) {
        if (items[y][x] instanceof ItemReference ref) {
            x = ref.x;
            y = ref.y;
        }
        forItemArea(x, y, (i) -> null);
    }

    /**
     * Removes the first occurrence of the item from the grid
     * @param item the item to remove
     */
    public void remove(GridItem item) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (items[y][x] == item) {
                    remove(x, y);
                    return;
                }
            }
        }
    }

    /**
     * Gets the item at an (x, y) coordinate. If the item is an {@link ItemReference ItemReference}, the item it refers
     * to will be returned.
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
     * Gets all the {@link GridItem items} in the grid. Every returned item is guaranteed to not be null or an instance
     * of {@link ItemReference}.
     */
    public LinkedList<GridItem> getItems() {
        LinkedList<GridItem> gridItems = new LinkedList<>();
        for (GridItem[] row : items) {
            for (GridItem item : row) {
                if (item != null && !(item instanceof ItemReference)) {
                    gridItems.add(item);
                }
            }
        }
        return gridItems;
    }

    /**
     * @return the 2d array that contains all the grid's items (some may be null).
     * @apiNote the array is structured so that if laid out on a graph, the rows represent the y-axis and the columns
     * are on the x-axis (i.e. (x, y) == array[y][x])
     */
    public final GridItem[][] getInternalArray() {
        return items;
    }

    /**
     * Loops over all the grid cells that the item covers and sets its value to that returned by {@code iter}.
     * Fails silently if there isn't an item at the provided coordinates.
     */
    public void forItemArea(int x, int y, Function<@Nullable GridItem, @Nullable GridItem> iter) {
        GridItem item = get(x, y);
        if (item == null) return;
        forItemArea(x, y, item.width, item.height, iter);
    }

    /**
     * Loops over all the grid cells within the provided area and sets their values to that returned by {@code iter}.
     * If some of the cells are outside the grid, they will be skipped.
     * @param x the starting x-coordinate
     * @param y the starting y-coordinate
     * @param width the width of the area
     * @param height the height of the area
     * @param iter the function to run for each item
     */
    public void forItemArea(int x, int y, int width, int height, Function<@Nullable GridItem, @Nullable GridItem> iter) {
        for (int i = 0; i < height && i + y < this.height; i++) {
            for (int j = 0; j < width && j + x < this.width; j++) {
                int itemX = x + j;
                int itemY = y + i;
                // handle edge case where starting x and/or y are out of bounds
                if (isWithinBounds(itemX, itemY, 1, 1)) {
                    items[y + i][x + j] = iter.apply(items[y + i][x + j]);
                }
            }
        }
    }

    private boolean isWithinBounds(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x + width-1 <= this.width && y + height-1 <= this.height;
    }

    /**
     * Creates a string representation of the grid. Empty spaces are a {@code -}, references are {@code *} and all other
     * items are shown as an {@code i}.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Grid (" + width + ", " + height + ")\n");
        for (int y = height-1; y >= 0; y--) {
            str.append("y").append(y);
            for (int x = 0; x < width; x++) {
                str.append(' ');
                GridItem item = items[y][x];
                if (item == null) {
                    str.append('-');
                } else if (item instanceof ItemReference) {
                    str.append('*');
                } else {
                    str.append('i');
                }
            }
            str.append('\n');
        }
        return str.toString();
    }

    /**
     * Refers to a grid item
     */
    public static class ItemReference extends GridItem {
        // the referenced item's x and y coords
        public final int x, y;

        public ItemReference(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
