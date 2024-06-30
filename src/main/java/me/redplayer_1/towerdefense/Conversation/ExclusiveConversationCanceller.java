package me.redplayer_1.towerdefense.Conversation;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.jetbrains.annotations.NotNull;

public class ExclusiveConversationCanceller implements ConversationCanceller {
    private final String cancelInputString;
    private final Object shouldCancelKey;
    private final boolean caseSensitive;
    private final String cancelledMessage;
    private final boolean initialValue;
    private Conversable conversable;

    /**
     * @param cancelInputString The input String that will start the cancellation process
     * @param caseSensitive If the cancelInputString must match the input exactly
     * @param shouldCancelKey The context key that is set to a boolean value. If the cancel string is inputted and this
     *                        key's value is true, the conversation is cancelled
     * @param initialValue the default value for {@link #shouldCancelKey} when this canceller is added to a conversation
     */
    public ExclusiveConversationCanceller(String cancelInputString, boolean caseSensitive, String cancelledMessage, Object shouldCancelKey, boolean initialValue) {
        this.cancelInputString = cancelInputString;
        this.shouldCancelKey = shouldCancelKey;
        this.caseSensitive = caseSensitive;
        this.cancelledMessage = cancelledMessage;
        this.initialValue = initialValue;
    }

    @Override
    public void setConversation(@NotNull Conversation conversation) {
        conversation.getContext().setSessionData(shouldCancelKey, initialValue);
        conversable = conversation.getForWhom();
    }

    @Override
    public boolean cancelBasedOnInput(@NotNull ConversationContext context, @NotNull String input) {
        if ((caseSensitive? input.equals(cancelInputString) : input.equalsIgnoreCase(cancelInputString))
                && context.getSessionData(shouldCancelKey) instanceof Boolean shouldCancel
                && shouldCancel
        ) {
            assert conversable != null;
            conversable.sendRawMessage(cancelledMessage);
            return true;
        }
        return false;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull ConversationCanceller clone() {
        return new ExclusiveConversationCanceller(cancelInputString, caseSensitive, cancelledMessage, shouldCancelKey, initialValue);
    }
}
