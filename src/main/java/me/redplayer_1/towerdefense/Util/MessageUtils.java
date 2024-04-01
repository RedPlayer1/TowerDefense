package me.redplayer_1.towerdefense.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public final class MessageUtils {
    private static final MiniMessage MMSG = MiniMessage.miniMessage();

    /**
     * Creates a help entry string (with MiniMessage components)
     * @param root the root command (ex. /command fixedArgument)
     * @param args root command arguments (surround with <>)
     * @param description a description of what the command does
     * @return the created help entry string
     */
    public static String helpEntry(String root, @Nullable String args, String description) {
        return "<white>" + root + "</white>" + (args == null? "" : " <gold>" + args + "</gold>") + " <gray>- " + description + "</gray>";
    }

    /**
     * Logs a message to the player
     * @param target the target that the message will be logged to
     * @param msg the message to send
     * @param level the log level of the message
     */
    public static void log(CommandSender target, String msg, LogLevel level) {
        target.sendRichMessage(level.format(msg));
    }

    public static Component asMiniMessage(String text) {
        return MMSG.deserialize(text);
    }
}