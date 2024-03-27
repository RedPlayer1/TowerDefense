package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Plot.Direction;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.ItemUtils;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;

public class LayoutEditor {
    private static final Material PLATFORM_BASE = Material.STONE_BRICKS;
    private static HashMap<Player, LayoutEditor> openEditors = new HashMap<>();
    private static final NamespacedKey KEY = new NamespacedKey(TowerDefense.INSTANCE, "layoutitem");
    private static final ItemStack[] toolInventory = new ItemStack[4];
    static {
        toolInventory[0] = ItemUtils.create("left", Material.STICK);
        toolInventory[1] = ItemUtils.create("forward", Material.STICK);
        toolInventory[2] = ItemUtils.create("right", Material.STICK);
        toolInventory[3] = ItemUtils.create("delete", Material.REDSTONE_BLOCK);

        for (int i = 0; i < toolInventory.length; i++) {
            toolInventory[i].getItemMeta().getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, i);
        }
    }

    private final Player player;
    private final ItemStack[] playerInventory;
    private final BlockMesh bottomPlatform;
    private final BlockMesh placementArea;
    private Location startLoc;
    private Location currentNodeLoc;
    private LinkedList<Direction> path;
    private LinkedList<Entity> placedNodes; // armor stand indicators that nodes have been placed

    /**
     * Create a layout at the player's location and put editing tools in their inventory
     * @param player the player creating the layout
     */
    public LayoutEditor(Player player) {
        this.player = player;
        playerInventory = player.getInventory().getContents();
        player.getInventory().setContents(toolInventory);
        path = new LinkedList<>();

        // create base platform
        bottomPlatform = new BlockMesh(Layout.SIZE, Layout.SIZE, 1);
        bottomPlatform.fillMesh(Material.STONE_BRICKS);
        // TODO: error handling if player isn't within build limits (-64 to 320 (overworld))
        bottomPlatform.place(player.getLocation().subtract(0, 2, 0));
        placementArea = new BlockMesh(Layout.SIZE, Layout.SIZE, 1);
        placementArea.fillMesh(Material.GRASS_BLOCK);
        placementArea.place(player.getLocation().subtract(0, 1, 0));

        openEditors.put(player, this);
    }

    public LayoutEditor(Player player, Layout template) {
        this(player);
        // TODO: load & place layout blocks & create node blockdisplays
    }

    /**
     * Add a node to the layout
     * @param direction the relative direction of the node (may only be west (left), east (right), or north (forward))
     */
    public void addNode(Direction direction) {
        if (startLoc == null) throw new IllegalStateException("Cannot add a node without a start location");
        if (!path.isEmpty()) {
            Direction prevDir = path.peekLast();
            direction = switch (direction) {
                case NORTH -> prevDir;
                case WEST -> prevDir.left();
                case EAST -> prevDir.right();
                default -> throw new IllegalStateException("Cannot add a node facing south");
            };
            currentNodeLoc = direction.getFromLocation(currentNodeLoc);
        }
        BlockDisplay node = (BlockDisplay) startLoc.getWorld().spawnEntity(
                currentNodeLoc,
                EntityType.BLOCK_DISPLAY
        );
        final Direction finalDirection = direction;
        node.setBlock(Material.MAGENTA_GLAZED_TERRACOTTA.createBlockData(data -> {
            // glazed magenta terracotta's arrow points opposite to block direction
            BlockFace facing = switch (finalDirection) {
                case NORTH -> BlockFace.SOUTH;
                case SOUTH -> BlockFace.NORTH;
                case EAST -> BlockFace.WEST;
                case WEST -> BlockFace.EAST;
            };
            ((Directional) data).setFacing(facing);
        }));
        node.setGlowing(true);
        node.setDisplayHeight(.3f);
        node.setDisplayHeight(.3f);
        placedNodes.add(node);
        path.add(direction);
    }

    /**
     * Saves and closes the editor and assigns its layout the provided name.
     *
     * @param name the name of the layout
     * @return the created layout (or null if the layout is incomplete)
     * @see LayoutEditor#close()
     */
    public Layout save(String name) {
        if (startLoc == null) return null;
        return new Layout(name, startLoc, close(), path.toArray(new Direction[0]));
    }

    /**
     * Closes the editor. Editing player's inventory is restored and layout blocks are removed.
     * <b>Does not save the layout</b>
     *
     * @return the types of blocks that made up the layout
     */
    public BlockMesh close() {
        // remove all blocks & nodes
        for (Entity placedNode : placedNodes) {
            placedNode.remove();
        }
        openEditors.remove(player);
        bottomPlatform.destroy();
        placementArea.capture(placementArea.getBottomLeft());
        placementArea.destroy();
        player.getInventory().setContents(playerInventory);
        return placementArea;
    }

    /**
     * Sets the starting location for the layout. Existing nodes will be invalidated and cleared.
     * @param startLoc the new start location
     */
    public void setStartLoc(Location startLoc) {
        this.startLoc = startLoc;
        currentNodeLoc = startLoc;
        path.clear();
        for (Entity node : placedNodes) {
            node.remove();
        }
    }

    public BlockMesh getBottomPlatform() {
        return bottomPlatform;
    }

    public BlockMesh getPlacementArea() {
        return placementArea;
    }

    /**
     * @param player the player using the editor
     * @return the LayoutEditor that the player has open
     */
    public static @Nullable LayoutEditor getEditor(Player player) {
        return openEditors.get(player);
    }

    public static final class EventListener implements Listener {
        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (!event.getAction().isRightClick()) return;
            Player p = event.getPlayer();
            LayoutEditor editor = openEditors.get(p);
            if (editor == null) return;
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();
            if (editor.path.isEmpty()) {
                // no nodes placed, set starting node to clicked block
                if (block == null) {
                    MessageUtils.sendError(p, "Click on a block to set the starting position");
                    return;
                } else {
                    Direction dir = switch (event.getPlayer().getFacing()) {
                        case NORTH, NORTH_NORTH_EAST, NORTH_NORTH_WEST, NORTH_WEST, NORTH_EAST -> Direction.NORTH;
                        case SOUTH, SOUTH_SOUTH_WEST, SOUTH_SOUTH_EAST, SOUTH_WEST, SOUTH_EAST -> Direction.SOUTH;
                        case EAST, EAST_SOUTH_EAST, EAST_NORTH_EAST -> Direction.EAST;
                        case WEST, WEST_SOUTH_WEST, WEST_NORTH_WEST -> Direction.WEST;
                        case UP, DOWN, SELF -> null; // player facing will always be an ordinal direction
                    };
                    editor.setStartLoc(block.getLocation());
                    editor.addNode(dir);
                    MessageUtils.sendSuccess(p, "Set starting location");
                    return;
                }
            }

            if (item == null || item.getType() == Material.AIR || !item.getItemMeta().getPersistentDataContainer().has(KEY)) {
                MessageUtils.sendError(p, "Use the inventory tools to modify the layout");
                return;
            }
            Direction dir = switch (item.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER)) {
                case 0 -> Direction.WEST;
                case 1 -> Direction.NORTH;
                case 2 -> Direction.EAST;
                default -> null;
            };
            if (dir == null) {
                // remove last node
                if (editor.path.isEmpty()) {
                    MessageUtils.sendError(editor.player, "No nodes have been placed.");
                } else {
                    editor.path.removeLast();
                    MessageUtils.sendSuccess(editor.player, "<red>Removed last node.</red>");
                }
                return;
            }
            editor.addNode(dir);
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
            MessageUtils.sendSuccess(p, "Placed node facing " + dir.name().toLowerCase() + ".");
        }
    }
}
