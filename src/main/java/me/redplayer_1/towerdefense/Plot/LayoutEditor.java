package me.redplayer_1.towerdefense.Plot;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.LinkedList;

public class LayoutEditor {
    private static final Material PLATFORM_BASE = Material.STONE_BRICKS;
    private static HashMap<Player, LayoutEditor> openEditors = new HashMap<>();

    private final Player player;
    private final World world;

    private final Location topLeft; // includes placeable zone
    private final Location bottomRight;
    private Location startLoc;
    private LinkedList<Direction> path;

    public LayoutEditor(Player player) {
        this.player = player;
        path = new LinkedList<>();

        // create base platform
        // assume player pos is center & facing north
        int offset = (Layout.SIZE - 1) / 2;
        world = player.getLocation().getWorld();
        topLeft = player.getLocation().toBlockLocation().add(-offset, 0, -offset);
        bottomRight = player.getLocation().toBlockLocation().add(offset, -1, offset);

        int y = player.getLocation().getBlockY() - 1;
        for (int z = 0; z < Layout.SIZE; z++) {
            for (int x = 0; x < Layout.SIZE; x++) {
                world.getBlockAt(x, y, z).setType(PLATFORM_BASE);
            }
        }

        openEditors.put(player, this);
    }

    /**
     * Saves the editor's layout and assigns it the provided name.
     * @param name the name of the layout
     * @return the created layout (or null if the layout is incomplete)
     * @see LayoutEditor#close()
     */
    public Layout save(String name) {
        if (startLoc == null) return null;
        return new Layout(name, startLoc, path.toArray(new Direction[0]));
    }

    /**
     * Closes the editor. Editing player's inventory is restored and layout blocks are removed.
     * <b>Does not save the layout</b>
     * @see LayoutEditor#save(String)
     */
    public void close() {

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

    public static final class EventListener implements Listener {
        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                    || event.isBlockInHand()
                    || !openEditors.containsKey(event.getPlayer())
            ) return;

            // player right clicks to place node & shift+right clicks to remove last node
            Player p = event.getPlayer();
            LayoutEditor editor = openEditors.get(p);

            if (p.isSneaking()) {
                editor.path.removeLast();
                p.sendRichMessage("<red>Removed last node.</red>");
                return;
            }

            RayTraceResult trace;
            if ((trace = p.rayTraceBlocks(4, FluidCollisionMode.NEVER)) != null) {
                // determine facing
                float yaw = p.getLocation().getYaw(); // 0-360
                Direction facing;
                if (yaw > 45 && yaw < 135) {
                    facing = Direction.EAST;
                } else if (yaw <= 225 && yaw >= 135)
                    facing = Direction.SOUTH;
                else if (yaw > 225 && yaw < 315) {
                    facing = Direction.WEST;
                } else {
                    facing = Direction.NORTH;
                }

                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                if (editor.path.isEmpty()) {
                    editor.startLoc = trace.getHitBlock().getLocation();
                }
                p.sendRichMessage("<green>Placed node facing " + facing.name().toLowerCase() + ".</green>");
                editor.path.add(facing);
            }
        }
    }
}
