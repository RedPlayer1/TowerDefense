package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Command.LayoutCommand;
import me.redplayer_1.towerdefense.Command.PlotCommand;
import me.redplayer_1.towerdefense.Command.Tower.TowerCommand;
import me.redplayer_1.towerdefense.Geometry.MeshEditor;
import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Plot.Layout.Layouts;
import me.redplayer_1.towerdefense.Plot.Plot;
import me.redplayer_1.towerdefense.Plot.Tower.Tower;
import me.redplayer_1.towerdefense.Plot.Tower.Towers;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/*
TODO:
- Health command/inventory (run tests & return results)
 */

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
            MessageUtils.logConsole("Couldn't loading configuration files!", LogLevel.CRITICAL);
            getLogger().severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Commands
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("layout", new LayoutCommand());
        commandMap.register("plot", new PlotCommand());
        commandMap.register("tower", new TowerCommand());

        // Listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(), this);
        pluginManager.registerEvents(new LayoutEditor.EventListener(), this);
        pluginManager.registerEvents(new Tower.EventListener(), this);

        // Load apply main config values
        Plot.loadConfigValues(mainConfig.getConfig());

        // Load layout templates
        Layouts.loadLayoutTemplates(layoutTemplates.getConfig());
        Layout.loadConfigValues(mainConfig.getConfig());
        MessageUtils.logConsole("Loaded " + Layouts.getTemplates().size() + " layout templates", LogLevel.SUCCESS);

        // Load tower templates
        Towers.deserialize(towerTemplates.getConfig());
        MessageUtils.logConsole("Loaded " + Towers.getTowers().size() + " tower templates", LogLevel.SUCCESS);
    }

    @Override
    public void onDisable() {
        // Close any open editors
        LayoutEditor.closeAll();
        MeshEditor.closeAll();

        // Save configs
        Plot.saveConfigValues(mainConfig.getConfig());
        Layout.saveConfigValues(mainConfig.getConfig());
        ConfigurationSection layoutConfig = layoutTemplates.getConfig();
        for (Layout layout : Layouts.getTemplates()) {
            layout.serialize(layoutConfig);
        }
        Towers.serialize(towerTemplates.getConfig());
        mainConfig.save();
        layoutTemplates.save();
        towerTemplates.save();
    }
}
