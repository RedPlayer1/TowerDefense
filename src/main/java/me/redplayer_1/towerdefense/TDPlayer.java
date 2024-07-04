package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Exception.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Exception.NotEnoughPlotSpaceException;
import me.redplayer_1.towerdefense.Plot.Plot;
import me.redplayer_1.towerdefense.Util.LogLevel;
import me.redplayer_1.towerdefense.Util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class TDPlayer {
    private static final String PLAYER_DATA_DIRECTORY = TowerDefense.INSTANCE.getDataFolder().getPath() + "/playerdata/";
    private static final HashMap<Player, TDPlayer> players = new HashMap<>();

    private final Player player;
    private boolean privileged = false;
    private Plot plot;
    private int money;
    private int prestige;
    private int multiplier;
    private int wave;

    public TDPlayer(Player player, boolean checkForExisting) throws NotEnoughPlotSpaceException, NoLayoutFoundException {
        this.player = player;
        boolean foundConfig = false;

        if (checkForExisting) {
            File config = new File(PLAYER_DATA_DIRECTORY + player.getUniqueId() + ".yml");
            if (config.exists()) {
                try {
                    TDPlayer tdPlayer = deserialize(new Config(config));
                    foundConfig = true;
                    plot = tdPlayer.plot;
                    money = tdPlayer.money;
                    prestige = tdPlayer.prestige;
                    multiplier = tdPlayer.multiplier;
                    wave = tdPlayer.wave;
                } catch (IOException | InvalidConfigurationException e) {
                    MessageUtils.logConsole(
                            "An error occurred whilst attempting to load player data for \"" + player.getName() + "\": " + e,
                            LogLevel.WARN
                    );
                }
            }
        }
        if (!foundConfig) {
            // int types (money, prestige, etc.) are 0 by default & don't need to be set
            plot = new Plot();
        }
        if (TDPlayer.isPrivileged(player)) {
            privileged = true;
        }
        players.put(player, this);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public static boolean isPrivileged(Player p) {
        // TODO: add config vals for privileged player perms/ranks
        return p.isOp();
    }

    public Plot getPlot() {
        return plot;
    }

    public int getWave() {
        return wave;
    }

    /**
     * Adds the given amount to the player's money.
     * If the amount is negative, player's balance will be decreased by that amount
     */
    public void giveMoney(int amount) {
        money += amount;
        if (money < 0) {
            money = 0;
        }
    }

    public void setMoney(int money) {
        this.money = money;
    }

    /**
     * @return the TDPlayer associated with the Bukkit Player or null if one doesn't exist
     */
    public static @Nullable TDPlayer of(Player player) {
        return players.get(player);
    }

    public void unregister() {
        players.remove(player);
    }

    public void serialize() {
        try {
            Config config = new Config(new File(PLAYER_DATA_DIRECTORY + player.getUniqueId() + ".yml"));
            FileConfiguration fConfig = config.getConfig();

            fConfig.set("money", money);
            fConfig.set("prestige", prestige);
            fConfig.set("multiplier", multiplier);
            fConfig.set("wave", plot.getLayout().getWave());
            plot.serialize(fConfig.createSection("plot"));
            config.save();
        } catch (IOException e) {
            MessageUtils.log(
                    Bukkit.getConsoleSender(),
                    "Error whilst saving TDPlayer of \"" + player.getName() + "\"",
                    LogLevel.CRITICAL
            );
        }
    }

    public static TDPlayer deserialize(Config config) throws NotEnoughPlotSpaceException, NoLayoutFoundException, InvalidConfigurationException {
        FileConfiguration fConfig = config.getConfig();
        TDPlayer player = new TDPlayer(
                Bukkit.getPlayer(UUID.fromString(config.getFile().getName().replace(".yml", ""))),
                false
        );
        player.wave = fConfig.getInt("wave", 1);
        player.money = fConfig.getInt("money");
        player.prestige = fConfig.getInt("prestige");
        player.multiplier = fConfig.getInt("multiplier");
        player.wave = fConfig.getInt("multiplier");
        player.plot = Plot.deserialize(fConfig.getConfigurationSection("plot"), player.wave);
        return player;
    }
}
