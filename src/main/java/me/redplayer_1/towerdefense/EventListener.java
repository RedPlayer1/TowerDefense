package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Exception.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Exception.NotEnoughPlotSpaceException;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // register new TDPlayer
        Player p = event.getPlayer();
        try {
            new TDPlayer(p, true);
        } catch (NotEnoughPlotSpaceException e) {
            if (TDPlayer.isPrivileged(p)) {
                MessageUtils.log(p, e.toString(), LogLevel.CRITICAL);
            } else {
                p.kick(Component.text("Not enough plot space!"));
            }
        } catch (NoLayoutFoundException e) {
            if (TDPlayer.isPrivileged(p)) {
                MessageUtils.log(p, "No default plot exists; unprivileged players will be kicked.", LogLevel.CRITICAL);
            } else {
                p.kick(Component.text("No default plot exists. Please inform staff"));
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        TDPlayer tdPlayer = TDPlayer.of(event.getPlayer());
        if (tdPlayer != null) {
            tdPlayer.serialize();
        } else if (!TDPlayer.isPrivileged(event.getPlayer())){
            MessageUtils.log(Bukkit.getConsoleSender(), "player left  without tdplayer (test consolesender)", LogLevel.WARN);
            Bukkit.getLogger().warning("Player \"" + event.getPlayer().getName() + "\" left without a TDPlayer");
        }
    }
}
