package me.redplayer_1.towerdefense.Conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class NumericPrompter extends ValidatingPrompt {
    private final String prompt;
    private final BiConsumer<ConversationContext, Integer> acceptor;
    private final @Nullable Prompt next;

    public NumericPrompter(String prompt, BiConsumer<ConversationContext, Integer> acceptor, @Nullable Prompt next) {
        this.prompt = prompt;
        this.acceptor = acceptor;
        this.next = next;
    }

    @Override
    protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
        try {
            context.setSessionData(this, Integer.valueOf(input));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
        acceptor.accept(context, (Integer) context.getSessionData(this));
        return next;
    }

    @Override
    protected @Nullable String getFailedValidationText(@NotNull ConversationContext context, @NotNull String invalidInput) {
        return "Input must be a valid integer";
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return prompt;
    }
}
