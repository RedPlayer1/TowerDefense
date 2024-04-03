package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Exception.NodeOutOfBoundsException;
import me.redplayer_1.towerdefense.Plot.Direction;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.ItemUtils;
import me.redplayer_1.towerdefense.Util.LogLevel;
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
        toolInventory[0] = ItemUtils.create("Left", Material.STICK);
        toolInventory[1] = ItemUtils.create("Forward", Material.STICK);
        toolInventory[2] = ItemUtils.create("Right", Material.STICK);
        toolInventory[3] = ItemUtils.create("Delete", Material.RED_DYE);
    }

    private final Player player;
    private final ItemStack[] playerInventory;
    private final BlockMesh bottomPlatform;
    private final BlockMesh placementArea;
    private Location startLoc;
    private Location currentNodeLoc; // world location of the most recently placed node
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
        placedNodes = new LinkedList<>();

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
     * @param direction the relative direction of the node (may not be south)
     */
    public void addNode(Direction direction) throws NodeOutOfBoundsException {
        if (startLoc == null) throw new IllegalStateException("Cannot add a node without a start location");
        if (!path.isEmpty()) {
            Direction prevDir = path.peekLast();
            direction = switch (direction) {
                case NORTH -> prevDir;
                case WEST -> prevDir.left();
                case EAST -> prevDir.right();
                default -> throw new IllegalStateException("Cannot add a node facing south");
            };
            currentNodeLoc = direction.getFromLocation(currentNodeLoc, 1);
        }
        if (!placementArea.contains(currentNodeLoc)) {
            currentNodeLoc = direction.getFromLocation(currentNodeLoc, -1);
            throw new NodeOutOfBoundsException();
        }
        BlockDisplay node = (BlockDisplay) startLoc.getWorld().spawnEntity(
                new Location(startLoc.getWorld(), currentNodeLoc.getBlockX(), currentNodeLoc.y() + .4, currentNodeLoc.getBlockZ()),
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
     * Removes the most recently placed node from the editor. Fails silently if there are no nodes or the only node
     * that exists is the starting one.
     */
    public void removeLastNode() {
        if (path.size() <= 1) return;
        currentNodeLoc = path.removeLast().getFromLocation(currentNodeLoc, -1);
        placedNodes.removeLast().remove();
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
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            Block block = event.getClickedBlock();

            if (editor.path.isEmpty()) {
                // no nodes placed, set starting node to clicked block
                if (block == null) {
                    MessageUtils.log(p, "Click on a block to set the starting position", LogLevel.ERROR);
                } else if (editor.placementArea.contains(block.getLocation())) {
                    Direction dir = switch (event.getPlayer().getFacing()) {
                        case NORTH, NORTH_NORTH_EAST, NORTH_NORTH_WEST, NORTH_WEST, NORTH_EAST -> Direction.NORTH;
                        case SOUTH, SOUTH_SOUTH_WEST, SOUTH_SOUTH_EAST, SOUTH_WEST, SOUTH_EAST -> Direction.SOUTH;
                        case EAST, EAST_SOUTH_EAST, EAST_NORTH_EAST -> Direction.EAST;
                        case WEST, WEST_SOUTH_WEST, WEST_NORTH_WEST -> Direction.WEST;
                        case UP, DOWN, SELF -> null; // player facing will always be an ordinal direction
                    };
                    editor.setStartLoc(block.getLocation());
                    try {
                        editor.addNode(dir);
                        MessageUtils.log(p, "Set starting location", LogLevel.SUCCESS);
                    } catch (NodeOutOfBoundsException ignored) { }
                } else {
                    MessageUtils.log(p, "Starting location must be within the layout", LogLevel.ERROR);
                }
                return;
            }
            Direction dir = null;
            boolean remove = false;
            if (item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                dir = switch (MessageUtils.fromMiniMessage(item.getItemMeta().displayName())) {
                    case "Left" -> Direction.WEST;
                    case "Forward" -> Direction.NORTH;
                    case "Right" -> Direction.EAST;
                    case "Delete" -> {
                        remove = true;
                        yield null;
                    }
                    default -> null;
                };
            }
            if (dir == null) {
                if (remove) {
                    if (editor.path.isEmpty()) {
                        MessageUtils.log(editor.player, "No nodes have been placed.", LogLevel.ERROR);
                    } else {
                        editor.removeLastNode();
                        MessageUtils.log(editor.player, "<red>Removed last node.</red>", LogLevel.SUCCESS);
                    }
                } else {
                    MessageUtils.log(p, "Use the inventory tools to modify the layout", LogLevel.ERROR);
                    return;
                }
                return;
            }
            try {
                editor.addNode(dir); // automatically handles relative locations
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                MessageUtils.log(p, "Placed node facing " + dir.name().toLowerCase() + ".", LogLevel.SUCCESS);
            } catch (NodeOutOfBoundsException e) {
                MessageUtils.log(p, "Didn't place node as it would be out of bounds", LogLevel.WARN);
            }
        }
    }
}
