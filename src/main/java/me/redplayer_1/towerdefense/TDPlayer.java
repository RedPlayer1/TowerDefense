package me.redplayer_1.towerdefense;

import me.redplayer_1.towerdefense.Plot.Layout.NoLayoutFoundException;
import me.redplayer_1.towerdefense.Plot.Layout.NotEnoughPlotSpaceException;
import me.redplayer_1.towerdefense.Plot.Plot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TDPlayer {
    private static final String PLAYER_DATA_DIRECTORY = TowerDefense.INSTANCE.getDataFolder().getPath() + "/playerdata/";
    private static final HashMap<Player, TDPlayer> players = new HashMap<>();

    private final Player player;
    private boolean privileged = false;
    private Plot plot;
    private int money;
    private int prestige;
    private int multiplier;

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
                } catch (IOException | InvalidConfigurationException ignored) {
                }
            }
        }
        if (!foundConfig) {
            money = 0;
            prestige = 0;
            multiplier = 0;
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
            Config config = new Config(player.getUniqueId().toString());
            FileConfiguration fConfig = config.getConfig();

            fConfig.set("money", money);
            fConfig.set("prestige", prestige);
            fConfig.set("multiplier", multiplier);
            plot.serialize(fConfig.createSection("plot"));

            config.save();
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().severe(
                    "Error whilst saving TDPlayer of Player " + player.getName()
            );
        }
    }

    public static TDPlayer deserialize(Config config) throws NotEnoughPlotSpaceException {
        FileConfiguration fConfig = config.getConfig();
        TDPlayer player = new TDPlayer(
                Bukkit.getPlayer(config.getFile().getName().replace(".yml", "")),
                false
        );
        try {
            player.plot = Plot.deserialize(fConfig.getConfigurationSection("plot"));
        } catch (NotEnoughPlotSpaceException e) {
            throw new NotEnoughPlotSpaceException();
        }
        player.money = fConfig.getInt("money", 0);
        player.prestige = fConfig.getInt("prestige", 0);
        player.multiplier = fConfig.getInt("multiplier", 0);
        return player;
    }
}
