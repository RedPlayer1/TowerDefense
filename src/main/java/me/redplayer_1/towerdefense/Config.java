package me.redplayer_1.towerdefense;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Config {
    private final YamlConfiguration fileConfig;
    private final File file;

    /**
     * Attempts to load the specified file. If it doesn't exist, the file is created.
     * However, if a file exists in resources, that is copied to the directory.
     *
     * @param fileName the name of the file (without the postfix)
     */
    public Config(String fileName) throws IOException, InvalidConfigurationException {
        this(new File(TowerDefense.INSTANCE.getDataFolder().getPath() + "/" + fileName + ".yml"));
    }

    public Config(File configFile) throws IOException {
        file = configFile;
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs()) throw new IOException("Couldn't create parent directories");
            InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(file.getName());
            if (inputStream != null) {
                Files.copy(inputStream, file.toPath());
            } else {
                if (!file.createNewFile()) throw new IOException("Couldn't create file");
            }
        }

        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * @return The {@link FileConfiguration} for this Config
     */
    public YamlConfiguration getConfig() {
        return fileConfig;
    }

    public File getFile() {
        return file;
    }

    /**
     * Attempts to save the FileConfiguration to disk
     * @throws IOException if an error occurred whilst saving the file
     */
    public void save() throws IOException {
        fileConfig.save(file);
    }
}
