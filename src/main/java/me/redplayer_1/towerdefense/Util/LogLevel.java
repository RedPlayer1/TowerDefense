package me.redplayer_1.towerdefense.Util;

public enum LogLevel {
    /**
     * A debug message to inform of the current state of the operation
     */
    DEBUG("<gray>DBG: <i>", "</i></gray>"),
    /**
     * An operation has been successfully completed
     */
    SUCCESS("<dark_green><b>✓</b></dark_green> <green>", "</green>"),
    /**
     * Send text; no significance
     */
    NORMAL("<gray><i>", "<i></gray>"),
    /**
     * An operation encountered a recoverable error
     */
    WARN("<gold>Warning: <yellow>", "</yellow></gold>"),
    /**
     * An operation encountered an unrecoverable error
     */
    ERROR("<dark_red>Error: <red>", "</red></dark_red>"),
    /**
     * An operation encountered a severe unrecoverable error that must be resolved
     */
    CRITICAL("<dark_red>✖ <b>CRITICAL</b> ✖", "</dark_red>");

    private final String prefix;
    private final String postfix;
    LogLevel(String prefix, String postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
    }

    /**
     * Formats the text according to the log level
     * @param text the text to format
     * @return a minimessage encoded string containing the formatted text
     */
    public String format(String text) {
        return prefix + text + postfix;
    }
}