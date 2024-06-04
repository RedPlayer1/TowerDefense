package me.redplayer_1.towerdefense.Plot.Layout;

import me.redplayer_1.towerdefense.Util.BlockMesh;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class Layouts {
    private static final LinkedList<Layout> templates = new LinkedList<>();

    /**
     * Gets the layout with the provided name or null if it doesn't exist.
     * If a layout is returned, it will have a placed mesh with the bottomLeft
     * set accordingly.
     * @param name the name of the layout
     * @param bottomLeft the bottom left coordinate of the layout's placement
     * @return a usable copy of the layout template
     */
    public static @Nullable Layout getLayout(String name, Location bottomLeft) {
        for (Layout layout : templates) {
            if (layout.getName().equals(name)) {
                BlockMesh mesh = new BlockMesh(layout.getMesh());
                mesh.place(bottomLeft);
                return new Layout(name, layout.getStartLocation(), mesh, layout.getPath());
            }
        }
        return null;
    }

    /**
     * Gets the template with the provided name or null if it doesn't exist.
     * If a layout is a template, its mesh will not have a bottomLeft,
     * For a placeable version of a template, see {@link #getLayout(String, Location) getLayout}
     * @param name the name of the template
     * @return the corresponding template or null if it didn't exist
     */
    public static @Nullable Layout getTemplate(String name) {
        for (Layout layout : templates) {
            if (layout.getName().equals(name)) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Add a layout template
     * @param template the template to add
     */
    public static void addTemplate(Layout template) {
        templates.add(template);
    }

    /**
     * Remove a template
     * @param name the name of the template to remove
     * @return if the template was found and removed
     */
    public static boolean removeTemplate(String name) {
        return templates.removeIf((l) -> l.getName().equals(name));
    }

    /**
     * @param name the name of the template/layout
     * @return if the layout is a template
     */
    public static boolean isTemplate(String name) {
        for (Layout l : templates) {
            if (l.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return all the layout templates
     */
    public static LinkedList<Layout> getTemplates() {
        return templates;
    }

    /**
     * Load a list of layout templates from a configuration section
     * @param section the section containing the values
     */
    public static void loadLayoutTemplates(ConfigurationSection section) {
        for (String name : section.getKeys(false)) {
            ConfigurationSection namedSection = section.getConfigurationSection(name);
            if (namedSection != null) {
                try {
                    templates.add(Layout.deserialize(namedSection));
                } catch (InvalidConfigurationException e) {
                    MessageUtils.log(Bukkit.getConsoleSender(), "Invalid layout template for Layout \"" + name + "\". Skipping. . .", LogLevel.WARN);
                }
            }
        }
    }
}
