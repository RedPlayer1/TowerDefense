package me.redplayer_1.towerdefense.Util;

import org.bukkit.entity.Player;

public final class PlayerUtils {
    /**
     * Sends an error message to the player
     * @param player the player to send the message to
     * @param reason the reason for the error (close all MiniMessage tags)
     */
    public static void sendError(Player player, String reason) {
        player.sendRichMessage("<dark_red>Error: " + reason);
    }
}
