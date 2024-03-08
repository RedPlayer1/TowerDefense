package me.redplayer_1.towerdefense.Plot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;

public class LayoutEditor {
    private static final Material PLATFORM_BASE = Material.STONE_BRICKS;
    private static HashMap<Player, LayoutEditor> openEditors = new HashMap<>();

    private final Player player;
    private final World world;
    private Location startLoc;

    private final Location topLeft; // includes placeable zone
    private final Location bottomRight;
    private LinkedList<Direction> path;
    private LinkedList<Entity> placedNodes; // armor stand indicators that nodes have been placed

    public LayoutEditor(Player player) {
        this.player = player;
        path = new LinkedList<>();

        // create base platform
        // assume player pos is center & facing north
        int offset = (Layout.SIZE - 1) / 2;
        world = player.getLocation().getWorld();
        topLeft = player.getLocation().toBlockLocation().add(-offset, 0, -offset);
        bottomRight = player.getLocation().toBlockLocation().add(offset, -1, offset);

        for (int y = bottomRight.getBlockY(); y <= topLeft.getBlockY(); y++) {
            for (int z = 0; z < Layout.SIZE; z++) {
                for (int x = 0; x < Layout.SIZE; x++) {
                    world.getBlockAt(x, y, z).setType(y == bottomRight.getBlockY() ? PLATFORM_BASE : Material.AIR);
                }
            }
        }
        openEditors.put(player, this);
    }

    public LayoutEditor(Player player, Layout template) {
        this(player);
        // TODO: load & place layout blocks & create node blockdisplays
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
        // TODO: save blocks
        close();
        return new Layout(name, startLoc, path.toArray(new Direction[0]));
    }

    /**
     * Closes the editor. Editing player's inventory is restored and layout blocks are removed.
     * <b>Does not save the layout</b>
     */
    public void close() {
        // remove all blocks & nodes
        for (int y = bottomRight.getBlockY(); y <= topLeft.getBlockY(); y++) {
            for (int z = 0; z < Layout.SIZE; z++) {
                for (int x = 0; x < Layout.SIZE; x++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
        for (int i = 0; i < placedNodes.size(); i++) {
            placedNodes.get(i).remove();
            i--;
        }
        openEditors.remove(player);
    }

    /**
     * @return if the location is within the editor's zone (platform & placeable)
     */
    public boolean isInEditorZone(Location loc) {
        return
                loc.getWorld() == world
                        && loc.x() >= topLeft.x() && loc.x() <= bottomRight.x()
                        && loc.y() <= topLeft.y() && loc.y() >= bottomRight.y()
                        && loc.z() >= topLeft.z() && loc.z() <= bottomRight.z();
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
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                    || event.getClickedBlock() == null
            ) return;

            // player right clicks to place node & shift+right clicks to remove last node
            Player p = event.getPlayer();
            LayoutEditor editor = openEditors.get(p);

            if (p.isSneaking()) {
                if (editor.path.isEmpty()) {
                    p.sendRichMessage("<dark_red>Err: No nodes have been placed.</dark_red>");
                } else {
                    editor.path.removeLast();
                    p.sendRichMessage("<red>Removed last node.</red>");
                }
                return;
            }

            float yaw = p.getLocation().getYaw(); // 0-360 degrees
            Direction direction;
            if (yaw > 45 && yaw < 135) {
                direction = Direction.EAST;
            } else if (yaw <= 225 && yaw >= 135)
                direction = Direction.SOUTH;
            else if (yaw > 225 && yaw < 315) {
                direction = Direction.WEST;
            } else {
                direction = Direction.NORTH;
            }

            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
            p.sendRichMessage("<green>Placed node facing " + direction.name().toLowerCase() + ".</green>");
            editor.path.add(direction);
            BlockDisplay node = (BlockDisplay) editor.world.spawnEntity(
                    event.getClickedBlock().getLocation(),
                    EntityType.BLOCK_DISPLAY
            );
            node.setBlock(Material.MAGENTA_GLAZED_TERRACOTTA.createBlockData(data -> {
                // glazed magenta terracotta's arrow points opposite to block direction
                BlockFace facing;
                switch (direction) {
                    case NORTH -> facing = BlockFace.SOUTH;
                    case SOUTH -> facing = BlockFace.NORTH;
                    case EAST -> facing = BlockFace.WEST;
                    case WEST -> facing = BlockFace.EAST;
                    default -> throw new IllegalStateException("Unexpected value: " + direction);
                }
                ((Directional) data).setFacing(facing);
            }));
            node.setGlowing(true);
            node.setDisplayHeight(.3f);
            node.setDisplayHeight(.3f);
        }
    }
}
