package me.redplayer_1.towerdefense.Plot.Tower;

import me.redplayer_1.towerdefense.Exception.NoSuchTemplateException;
import me.redplayer_1.towerdefense.Geometry.BlockMesh;
import me.redplayer_1.towerdefense.Geometry.MeshEditor;
import me.redplayer_1.towerdefense.Geometry.Vector3;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
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
                template = new Tower(
                        name, tower.getItem(), new BlockMesh(tower.getMesh()), tower.getParticle(),
                        tower.getParticlePoint(), tower.getRange(), tower.getDamage(),
                        tower.getCost(), tower.getTargets(), tower.getAttackDelay()
                );
                break;
            }
        }
        return template;
    }

    /**
     * @param item the item containing the id of the tower to get
     * @return the item's tower, or null if it doesn't exist
     */
    public static @Nullable Tower get(ItemStack item) {
        String id = item.getItemMeta().getPersistentDataContainer().get(Tower.ID_KEY, PersistentDataType.STRING);
        return get(id);
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
     * Creates a new {@link MeshEditor MeshEditor} for a tower template
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
            tower.getMesh().serialize(towerSec, "mesh");
            tower.getParticlePoint().serialize(towerSec.createSection("particlePoint"));
            towerSec.set("particle", tower.getParticle().name());
            towerSec.set("range", tower.getRange());
            towerSec.set("damage", tower.getDamage());
            towerSec.set("cost", tower.getCost());
            towerSec.set("targets", tower.getTargets());
            towerSec.set("attackDelay", tower.getAttackDelay());
        }
    }

    /**
     * Deserialize towers and load them into the registry
     * @param section the section containing all the towers' data
     */
    public static void deserialize(ConfigurationSection section) {
        for (String towerName : section.getKeys(false)) {
            if (!section.isConfigurationSection(towerName)) continue;
            try {
                ConfigurationSection towerSec = Objects.requireNonNull(section.getConfigurationSection(towerName));
                add(new Tower(
                        towerName,
                        Objects.requireNonNull(towerSec.getItemStack("item")),
                        BlockMesh.deserialize(towerSec.getConfigurationSection("mesh")),
                        Particle.valueOf(towerSec.getString("particle")),
                        Vector3.deserialize(Objects.requireNonNull(towerSec.getConfigurationSection("particlePoint"))),
                        towerSec.getInt("range"),
                        towerSec.getInt("damage"),
                        towerSec.getInt("cost"),
                        towerSec.getInt("targets"),
                        towerSec.getInt("attackDelay")
                ));
            } catch (InvalidConfigurationException | NullPointerException | IllegalArgumentException e) {
                MessageUtils.logConsole("Invalid configuration for Tower \"" + towerName + "\"", LogLevel.WARN);
            }
        }
    }
}
