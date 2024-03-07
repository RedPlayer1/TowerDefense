package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Command.LayoutCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TowerDefense extends JavaPlugin {
    public static TowerDefense INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Commands
        Bukkit.getCommandMap().register("layout", new LayoutCommand());

        // Listeners
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
