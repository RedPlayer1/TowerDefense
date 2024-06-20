package me.redplayer_1.towerdefense.Plot.Tower;

import me.redplayer_1.towerdefense.Exception.NoSuchTemplateException;
import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MeshEditor;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Objects;

public final class Towers {
    // tower registry
    private static final LinkedList<Tower> towers = new LinkedList<>();

    //TODO: tower editor (edit attrs via inventory + mesh editor)

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

    /**
     * @param name name of the tower to get
     * @return a copy of the specified tower template or null if it doesn't exist
     */
    public static @Nullable Tower get(String name) {
        Tower template = null;
        for (Tower tower : towers) {
            if (tower.name.equals(name)) {
                template = tower;
                break;
            }
        }
        if (template == null) return null;
        return new Tower(name, template.getItem(), template.getRange(), template.getDamage(), new BlockMesh(template.getMesh()));
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
     * Opens an attribute editor inventory for the tower. Any changes made are automatically applied to the tower
     * @param player the player to open the inventory for
     * @param tower the tower to edit
     */
    public static void openAttributeEditorInventory(Player player, Tower tower) {
        // TODO: open inventory & take player input (chat/sign/anvil paper)
    }

    /**
     * Creates a new {@link me.redplayer_1.towerdefense.Util.MeshEditor MeshEditor} for a tower template
     * @param player the player editing the tower
     * @param towerName the name of the tower to edit
     * @return the created editor
     * @throws NoSuchTemplateException if a template of the provided name cannot be found
     */
    public static MeshEditor openMeshEditor(Player player, String towerName) throws NoSuchTemplateException {
        Tower template = getTemplate(towerName);
        if (template != null) {
            return new MeshEditor(player, template.getMesh(), Material.PODZOL);
        } else {
            throw new NoSuchTemplateException();
        }
    }

    /**
     * Serialize all registered towers
     * @param section the section to put the towers' data in
     */
    public static void serialize(ConfigurationSection section) {
        for (Tower tower : towers) {
            ConfigurationSection towerSec = section.createSection(tower.name);
            towerSec.set("item", tower.getItem());
            towerSec.set("range", tower.getRange());
            towerSec.set("damage", tower.getDamage());
            tower.getMesh().serialize(towerSec, "mesh");
        }
    }

    /**
     * Deserialize towers and load them into the registry
     * @param section the section containing all the towers' data
     */
    public static void deserialize(ConfigurationSection section) {
        for (String towerName : section.getKeys(false)) {
            try {
                add(new Tower(
                        towerName,
                        Objects.requireNonNull(section.getItemStack(towerName + ".item")),
                        section.getInt(towerName + ".range"),
                        section.getInt(towerName + ".damage"),
                        BlockMesh.deserialize(section.getConfigurationSection(towerName + ".mesh"))
                ));
            } catch (InvalidConfigurationException | NullPointerException e) {
                MessageUtils.logConsole("Invalid configuration for Tower \"" + towerName + "\"", LogLevel.WARN);
            }
        }
    }
}
