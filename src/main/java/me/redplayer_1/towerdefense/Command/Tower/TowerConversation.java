package me.redplayer_1.towerdefense.Command.Tower;

import me.redplayer_1.towerdefense.Conversation.ExclusiveConversationCanceller;
import me.redplayer_1.towerdefense.Conversation.NumericPrompter;
import me.redplayer_1.towerdefense.Conversation.StringConversationPrefix;
import me.redplayer_1.towerdefense.Geometry.BlockMesh;
import me.redplayer_1.towerdefense.Plot.Tower.TowerFactory;
import me.redplayer_1.towerdefense.Plot.Tower.Towers;
import me.redplayer_1.towerdefense.TowerDefense;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Starts a conversation with the player to get the tower's
 * <ul>
 *     <li>name</li>
 *     <li>range</li>
 *     <li>damage</li>
 *     <li>cost</li>
 *     <li>attack cooldown</li>
 *     <li>targets</li>
 * </ul>
 */
public class TowerConversation {
    private static final Object CAN_CANCEL = new Object();
    private final TowerFactory factory = new TowerFactory();

    // Prompts (ordered last to first)
    private final ValidatingPrompt particlePrompt = new ValidatingPrompt() {
        @Override
        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
            BlockMesh mesh = factory.getMesh();
            if (input.equalsIgnoreCase("done") && context.getSessionData("player") instanceof Player player && mesh != null) {
                Block block = player.getTargetBlockExact(10, FluidCollisionMode.SOURCE_ONLY);
                if (block != null && mesh.contains(block.getLocation())) {
                    context.setSessionData("particleLocation", block.getLocation().clone());
                    return true;
                }
            }
            return false;
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            BlockMesh mesh = factory.getMesh();
            Object location = context.getSessionData("particleLocation");
            assert mesh != null && location instanceof Location;
            Object player = context.getSessionData("player");
            assert player instanceof Player;
            try {
                factory.setParticlePoint(mesh.toRelativeLocation((Location) location));
                Towers.add(factory.build());
                mesh.destroy();
                MessageUtils.log((Player) player, "Tower created", LogLevel.SUCCESS);
            } catch (IllegalStateException e) {
                MessageUtils.log((Player) player, "Something went wrong. Please notify a developer", LogLevel.ERROR);
            }
            return null;
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return "Look at the block that particles will be fired out of and type \"done\"";
        }
    };

    private final NumericPrompter targetPrompt = new NumericPrompter("Max Targets", (ctx, num) -> {
        // set max targets & place mesh for the particle prompt
        factory.setTargets(num);
        BlockMesh mesh = factory.getMesh();
        assert mesh != null;
        Location bL = mesh.getBottomLeft();
        assert bL != null; // this is set in the constructor
        mesh.place(bL);
        // don't allow the player to cancel the builder when the mesh is placed
        ctx.setSessionData(CAN_CANCEL, false);
    }, particlePrompt);

    private final NumericPrompter attackPrompt = new NumericPrompter("Attack Cooldown", (c, i) -> factory.setAttackDelay(i), targetPrompt);
    private final NumericPrompter costPrompt = new NumericPrompter("Cost", (c, i) -> factory.setCost(i), attackPrompt);
    private final NumericPrompter damagePrompt = new NumericPrompter("Damage", (c, i) -> factory.setDamage(i), costPrompt);
    private final NumericPrompter rangePrompt = new NumericPrompter("Range (blocks)", (c, i) -> factory.setRange(i), damagePrompt);

    /**
     * Start a conversation with the player to obtain all the needed information to create a tower
     * @param player the player creating the tower
     * @param mesh the tower's mesh
     * @param towerItem the tower's item
     */
    public TowerConversation(Player player, BlockMesh mesh, ItemStack towerItem) {
        mesh.destroy();
        mesh.setBottomLeft(player.getLocation().add(1, 0, 1));
        factory.setMesh(mesh);
        factory.setItem(towerItem);
        ValidatingPrompt namePrompt = new ValidatingPrompt() {

            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                return "Name (periods disallowed)";
            }

            @Override
            protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
                return !input.contains(".");
            }

            @Override
            protected Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
                context.setSessionData("player", player);
                factory.setName(input);
                return rangePrompt;
            }
        };

        new ConversationFactory(TowerDefense.INSTANCE)
                .withLocalEcho(false)
                .withPrefix(new StringConversationPrefix("§c§l[Tower Builder]§e"))
                .withEscapeSequence("cancel")
                .withConversationCanceller(new ExclusiveConversationCanceller(
                        "cancel", false, "§4§lTower creation cancelled", CAN_CANCEL, true)
                )
                .thatExcludesNonPlayersWithMessage("only players can create towers")
                .withFirstPrompt(namePrompt)
                .buildConversation(player).begin();
    }
}
