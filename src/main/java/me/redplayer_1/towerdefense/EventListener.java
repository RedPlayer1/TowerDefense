package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Plot.Layout.NotEnoughPlotSpaceException;
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
            if (!p.isOp()) {
                p.kick(Component.text("Not enough plot space!"));
            } else {
                MessageUtils.sendError(p, e.toString());
            }
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
