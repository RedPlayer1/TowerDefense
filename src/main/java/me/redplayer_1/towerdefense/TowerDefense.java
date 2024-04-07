package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Command.LayoutCommand;
import me.redplayer_1.towerdefense.Command.PlotCommand;
import me.redplayer_1.towerdefense.Plot.Layout.Layout;
import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Util.LogLevel;
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

    {
        try {
            mainConfig = new Config("Config");
            layoutTemplates = new Config("LayoutTemplates");
        } catch (IOException | InvalidConfigurationException e) {
            MessageUtils.log(Bukkit.getConsoleSender(), "Couldn't loading configuration files!", LogLevel.CRITICAL);
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

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
        for (String name : templateConfig.getKeys(false)) {
            if (templateConfig.isConfigurationSection("name")) {
                try {
                    Layout.deserialize(templateConfig.getConfigurationSection(name), true);
                    count++;
                } catch (InvalidConfigurationException e) {
                    MessageUtils.log(Bukkit.getConsoleSender(), "Invalid layout template for Layout \"" + name + "\". Skipping. . .", LogLevel.WARN);
                }
            }
        }
        String defaultLayoutName = mainConfig.getConfig().getString("defaultConfig");
        if (defaultLayoutName != null) {
            Layout defaultLayout = Layout.getLayout(defaultLayoutName);
            if (defaultLayout != null) {
                Layout.defaultLayout = defaultLayout;
            } else {
                MessageUtils.log(Bukkit.getConsoleSender(), "Invalid default layout \"" + defaultLayoutName + "\" in config", LogLevel.ERROR);
            }
        }
        MessageUtils.log(Bukkit.getConsoleSender(), "Successfully loaded " + count + " layout templates", LogLevel.SUCCESS);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
