package me.redplayer_1.towerdefense.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public final class MessageUtils {
    private static final MiniMessage MMSG = MiniMessage.miniMessage();
    private MessageUtils() { }

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

    /**
     * Logs a message to the server console
     * @param msg the message to send
     * @param level the log level of the message
     */
    public static void logConsole(String msg, LogLevel level) {
        log(Bukkit.getConsoleSender(), msg, level);
    }

    /**
     * @return the String representation of the Location
     */
    public static String locationToString(Location location) {
        return "Location: " + location.x() + ", " + location.y() + ", " + location.z() +
                " in world \"" + (location.getWorld() != null? location.getWorld().getName() : "null") + "\"";
    }

    public static Component asMiniMessage(String text) {
        return MMSG.deserialize(text);
    }

    /**
     * Converts a component (MiniMessage) into its String representation
     * @param mmsg the component to serialize
     * @return the serialized component or "" if an error occurred
     */
    public static String fromMiniMessage(Component mmsg) {
        return MMSG.serializeOr(mmsg, "");
    }
}
