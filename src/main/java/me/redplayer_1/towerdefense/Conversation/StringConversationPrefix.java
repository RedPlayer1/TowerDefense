package me.redplayer_1.towerdefense.Conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.jetbrains.annotations.NotNull;

public class StringConversationPrefix implements ConversationPrefix {
    private final String prefix;

    /**
     * Creates a new {@link ConversationPrefix} with the specified string
     * @param string the prefix (uses minecraft color codes)
     */
    public StringConversationPrefix(String string) {
        prefix = string + " ";
    }
    @Override
    public @NotNull String getPrefix(@NotNull ConversationContext context) {
        return prefix;
    }
}
