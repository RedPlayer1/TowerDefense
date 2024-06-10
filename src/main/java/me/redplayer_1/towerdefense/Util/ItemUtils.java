package me.redplayer_1.towerdefense.Util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemUtils {
    // TODO: player head creation

    public static ItemStack create(String name, Material material, String... lore) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(MessageUtils.asMiniMessage(name));
            meta.lore(Arrays.stream(lore).map(MessageUtils::asMiniMessage).toList());
        });
        return item;
    }

    public static void giveOrDrop(Player player, ItemStack... items) {
        for (ItemStack item : player.getInventory().addItem(items).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}
