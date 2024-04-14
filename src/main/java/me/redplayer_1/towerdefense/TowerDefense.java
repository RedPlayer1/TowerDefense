package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Command.LayoutCommand;
import me.redplayer_1.towerdefense.Command.PlotCommand;
import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Plot.Layout.Towers;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MeshEditor;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class TowerDefense extends JavaPlugin {
    public static TowerDefense INSTANCE;
    public Config mainConfig;
    public Config layoutTemplates;
    public Config towerTemplates;

    @Override
    public void onEnable() {
        INSTANCE = this;
        try {
            mainConfig = new Config("Config");
            layoutTemplates = new Config("LayoutTemplates");
            towerTemplates = new Config("TowerTemplates");
        } catch (IOException | InvalidConfigurationException e) {
            MessageUtils.log(Bukkit.getConsoleSender(), "Couldn't loading configuration files!", LogLevel.CRITICAL);
            Bukkit.getLogger().severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Commands
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("layout", new LayoutCommand());
        commandMap.register("plot", new PlotCommand());

        // Listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(), this);
        pluginManager.registerEvents(new LayoutEditor.EventListener(), this);

        // Load layout templates
        ConfigurationSection templateConfig = layoutTemplates.getConfig();
        int count = 0;
        if (templateConfig != null) {
            for (String name : templateConfig.getKeys(false)) {
                ConfigurationSection namedSection = templateConfig.getConfigurationSection(name);
                if (namedSection != null) {
                    try {
                        Layout.deserialize(namedSection, true);
                        count++;
                    } catch (InvalidConfigurationException e) {
                        MessageUtils.log(Bukkit.getConsoleSender(), "Invalid layout template for Layout \"" + name + "\". Skipping. . .", LogLevel.WARN);
                    }
                }
            }
        }
        String defaultLayoutName = mainConfig.getConfig().getString("default_layout");
        if (defaultLayoutName != null) {
            Layout defaultLayout = Layout.getLayout(defaultLayoutName);
            if (defaultLayout != null) {
                Layout.defaultLayout = defaultLayout;
            } else {
                MessageUtils.log(Bukkit.getConsoleSender(), "Invalid default layout \"" + defaultLayoutName + "\" in config", LogLevel.ERROR);
            }
        }
        MessageUtils.log(Bukkit.getConsoleSender(), "Loaded " + count + " layout templates", LogLevel.SUCCESS);

        // Load tower templates
        ConfigurationSection towerConfig = towerTemplates.getConfig();
        if (towerConfig != null) {
            Towers.deserialize(towerConfig);
        }
        MessageUtils.log(Bukkit.getConsoleSender(), "Loaded " + Towers.getTowers().size() + " tower templates", LogLevel.SUCCESS);
    }

    @Override
    public void onDisable() {
        // Close any open editors
        MeshEditor.closeAll();

        // Save configs
        mainConfig.save();
        ConfigurationSection layoutConfig = layoutTemplates.getConfig();
        for (Layout layout : Layout.getLayouts()) {
            layout.serialize(layoutConfig);
        }
        layoutTemplates.save();
        Towers.serialize(towerTemplates.getConfig());
        towerTemplates.save();
    }
}
