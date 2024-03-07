package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Plot.NotEnoughPlotSpaceException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // register new TDPlayer
        try {
            new TDPlayer(event.getPlayer(), true);
        } catch (NotEnoughPlotSpaceException e) {
            event.getPlayer().kick(Component.text("Not enough plot space!"));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        TDPlayer tdPlayer = TDPlayer.of(event.getPlayer());
        if (tdPlayer != null) {
            tdPlayer.serialize();
        } else {
            Bukkit.getLogger().warning("Player \"" + event.getPlayer().getName() + "\" left without a TDPlayer");
        }
    }
}
