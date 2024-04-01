package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Command.LayoutCommand;
import me.redplayer_1.towerdefense.Command.PlotCommand;
import me.redplayer_1.towerdefense.Plot.Layout.LayoutEditor;
import me.redplayer_1.towerdefense.Plot.Plot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TowerDefense extends JavaPlugin {
    public static TowerDefense INSTANCE;

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
