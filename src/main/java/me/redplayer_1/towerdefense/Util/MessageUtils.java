package me.redplayer_1.towerdefense.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public final class MessageUtils {
    private static final MiniMessage MMSG = MiniMessage.miniMessage();
    /**
     * Sends a success message to the player.
     *
     * @param player  the player to send the message to
     * @param message the result of the successful operation (recommended to close all MiniMessage tags)
     */
    public static void sendSuccess(Player player, String message) {
        player.sendRichMessage("<dark_green>✓</dark_green> <green>" + message + "</green>");
    }

    /**
     * Sends an error message to the player.
     *
     * @param player the player to send the message to
     * @param reason the reason for the error (recommended to close all MiniMessage tags)
     */
    public static void sendError(Player player, String reason) {
        player.sendRichMessage("<dark_red>Error: " + reason + "</dark_red>");
    }

    public static Component asMiniMessage(String text) {
        return MMSG.deserialize(text);
    }
}
