package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Exception.NoSuchTemplateException;
import me.redplayer_1.towerdefense.Util.MeshEditor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public final class Towers {
    private static final LinkedList<Tower> towers = new LinkedList<>();

    //TODO: tower editor (edit attrs via inventory + mesh editor)
    // -- create mesheditor class & use for this + layout editor?

    public static void add(Tower tower) {
        towers.removeIf(t -> t.name.equals(tower.name));
        towers.add(tower);
    }

    public static void remove(String name) {
        towers.removeIf(t -> t.name.equals(name));
    }

    public static LinkedList<Tower> getTowers() {
        return towers;
    }

    public static @Nullable Tower get(String name) {
        Tower template = null;
        for (Tower tower : towers) {
            if (tower.name.equals(name)) {
                template = tower;
                break;
            }
        }
        if (template == null) return null;
        return new Tower(name, template.getItem(), template.getRange(), template.getMesh());
    }

    /**
     * Gets a tower template (not a copy)
     * @param name the name of the template
     * @return the template, or null if it doesn't exist
     */
    private static @Nullable Tower getTemplate(String name) {
        for (Tower tower : towers) {
            if (tower.name.equals(name)) {
                return tower;
            }
        }
        return null;
    }

    /**
     * Opens an attribute editor inventory for the tower
     * @param player the player to open the inventory for
     * @param towerName the name of the tower to edit
     * @throws NoSuchTemplateException if a template of the provided name cannot be found
     */
    public static void openAttributeEditorInventory(Player player, String towerName) throws NoSuchTemplateException{
        // TODO: open inventory & take player input (chat/sign/anvil paper)
    }

    /**
     * Creates a new {@link me.redplayer_1.towerdefense.Util.MeshEditor MeshEditor} for a tower template
     * @param player the player editing the tower
     * @param towerName the name of the tower to edit
     * @throws NoSuchTemplateException if a template of the provided name cannot be found
     */
    public static void openMeshEditor(Player player, String towerName) throws NoSuchTemplateException {
        Tower template = getTemplate(towerName);
        if (template != null) {
            new MeshEditor(player, template.getMesh(), Material.PODZOL);
        } else {
            throw new NoSuchTemplateException();
        }
    }

    public static void serialize(ConfigurationSection section) {
        // serialize all registered towers
    }

    public static void deserialize(ConfigurationSection section) {
        // deserialize towers & load into towers list
    }
}
