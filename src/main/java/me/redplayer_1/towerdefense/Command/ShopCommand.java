package me.redplayer_1.towerdefense.Command;

import me.redplayer_1.towerdefense.Menus.ShopMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand extends Command {

    public ShopCommand() {
        super("shop");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        ShopMenu.open(player);
        return true;
    }
}
