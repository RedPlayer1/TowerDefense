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
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TDPlayer {
    private static final String PLAYER_DATA_DIRECTORY = TowerDefense.INSTANCE.getDataFolder().getPath() + "/playerdata/";
    private static final HashMap<Player, TDPlayer> players = new HashMap<>();

    private final Player player;
    private final Scoreboard scoreboard;
    private boolean privileged = false;
    private Plot plot;
    private int money;
    private int prestige;
    private int multiplier;
    private int wave;

    public TDPlayer(Player player, boolean checkForExisting) throws NotEnoughPlotSpaceException, NoLayoutFoundException {
        MessageUtils.logConsole("TDPLAYER CONSTRUCTOR CALLED", LogLevel.DEBUG);
        this.player = player;
        boolean foundConfig = false;

        if (checkForExisting) {
            File config = new File(PLAYER_DATA_DIRECTORY + player.getUniqueId() + ".yml");
            if (config.exists()) {
                try {
                    deserialize(this, new Config(config));
                    foundConfig = true;
                    MessageUtils.logConsole("FOUND & LOADED CONFIG FOR PLAYER", LogLevel.DEBUG);
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
            MessageUtils.logConsole("PLAYER CONFIG NOT FOUND", LogLevel.DEBUG);
        }
        if (TDPlayer.isPrivileged(player)) {
            privileged = true;
        }

        // create new scoreboard to display player stats
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("stats", Criteria.DUMMY, MessageUtils.asMiniMessage("Statistics"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard();
        player.setScoreboard(scoreboard);
        players.put(player, this);
    }

    /**
     * Updates all the player's scoreboard values like the current wave and coins.
     */
    public void updateScoreboard() {
        Objective objective = scoreboard.getObjective("stats");
        assert objective != null;

        objective.getScore("Wave").setScore(plot.getLayout().getWave());
        objective.getScore("Coins").setScore(money);
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
        updateScoreboard();
    }

    public void setMoney(int money) {
        this.money = money;
        updateScoreboard();
    }

    public int getMoney() {
        return money;
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
            MessageUtils.logConsole("PLAYER WAVE #" + plot.getLayout().getWave(), LogLevel.DEBUG);
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

    /**
     * Deserializes a TDPlayer
     * @param player the TDPlayer to put the deserialized data in
     * @param config the configuration containing the serialized data
     */
    public static void deserialize(TDPlayer player, Config config) throws NotEnoughPlotSpaceException, NoLayoutFoundException, InvalidConfigurationException {
        FileConfiguration fConfig = config.getConfig();
        MessageUtils.logConsole("GOT PLAYER WAVE #" + fConfig.getInt("wave", -1) + " from config", LogLevel.DEBUG);
        player.wave = fConfig.getInt("wave", 1);
        player.money = fConfig.getInt("money");
        player.prestige = fConfig.getInt("prestige");
        player.multiplier = fConfig.getInt("multiplier");
        player.plot = Plot.deserialize(fConfig.getConfigurationSection("plot"), player.wave);
    }
}
