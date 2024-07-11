package me.redplayer_1.towerdefense.Menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.redplayer_1.towerdefense.Plot.Tower.Towers;
import me.redplayer_1.towerdefense.TDPlayer;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopMenu {
    private static ChestGui gui;

    /**
     * Opens the shop menu (inventory) for the player
     * @param player the player to open the menu for
     */
    public static void open(Player player) {
        if (gui == null) {
            MessageUtils.logConsole("Generating shop GUI. . .", LogLevel.DEBUG);
            initGui();
        }
        gui.show(player);
    }

    private static void initGui() {
        gui = new ChestGui(6, "<b><red>Shop</red></b>");
        gui.setOnTopClick(event -> event.setCancelled(true));
        // add tower items
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);
        pages.populateWithGuiItems(Towers.getTowers().stream().map(t -> new GuiItem(t.getItem(), (event) -> {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player p) {
                TDPlayer tdPlayer = TDPlayer.of(p);
                int cost = t.getCost();
                if (tdPlayer != null) {
                    if (tdPlayer.getMoney() >= cost) {
                        tdPlayer.giveMoney(-cost);
                        p.getInventory().addItem(t.getItem());
                        MessageUtils.log(p, "Bought a <color:#702900>" + t.name + "</color> for <u><yellow>" + cost + "</yellow></u> <gold>coins</gold>", LogLevel.SUCCESS);
                    } else {
                        MessageUtils.log(p, "You don't have enough <gold>coins</gold>! <i>(need <red><u>" + (cost - tdPlayer.getMoney()) + "</u></red> more)</i>", LogLevel.WARN);
                    }
                }
            }

        })).toList());
        gui.addPane(pages);

        // add menu buttons (https://github.com/stefvanschie/IF/wiki/Shop)
        OutlinePane background = new OutlinePane(0, 5, 9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(background);

        StaticPane navigation = new StaticPane(0, 5, 9, 1);
        navigation.addItem(new GuiItem(new ItemStack(Material.RED_WOOL), event -> {
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);

                gui.update();
            }
        }), 0, 0);

        navigation.addItem(new GuiItem(new ItemStack(Material.GREEN_WOOL), event -> {
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);

                gui.update();
            }
        }), 8, 0);

        navigation.addItem(new GuiItem(new ItemStack(Material.BARRIER), event ->
                event.getWhoClicked().closeInventory()), 4, 0);

        gui.addPane(navigation);
    }
}
