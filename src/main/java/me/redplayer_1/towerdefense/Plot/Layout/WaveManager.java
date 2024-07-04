package me.redplayer_1.towerdefense.Plot.Layout;

/**
 * Manages which wave the layout is on. This class also calculates the different attributes of the wave.
 */
public class WaveManager {
    private int wave;
    private int enemyCount;
    private int enemyHealth;
    private int enemyCoinYield;
    private int completionCoinYield;

    /**
     * @param startWave The wave to start on. Ff the provided value is less than 1, it will be set to 1
     */
    public WaveManager(int startWave) {
        setWave(startWave);
    }

    /**
     * Advances the manager to the next wave.
     */
    public void next() {
        setWave(++wave);
    }

    /**
     * Sets the wave that the manager is on. The wave's attributes are calculated as:
     * <ul>
     *     <li>{@code enemyCount = wave^.5 + 3}</li>
     *     <li>{@code enemyHealth = wave^2 + 9}</li>
     *     <li>{@code enemyCoinYield = enemyHealth * .2 + wave}</li>
     *     <li>{@code completionCoinYield = 0}</li>
     * </ul>
     */
    public void setWave(int wave) {
        if (wave < 1) wave = 1;
        this.wave = wave;
        enemyCount = (int) Math.pow(wave, .5) + 3;
        enemyHealth = (int) Math.pow(wave, 2) + 9;
        enemyCoinYield = (int) (enemyHealth * .2 + wave);
        completionCoinYield = 0; // TODO: is such an incentive needed?
    }

    /**
     * @return the wave that the manager has computed values for
     */
    public int getWave() {
        return wave;
    }

    /**
     * @return the number of enemies to be spawned
     */
    public int getEnemyCount() {
        return enemyCount;
    }

    /**
     * @return how much health each enemy should start with
     */
    public int getEnemyHealth() {
        return enemyHealth;
    }

    /**
     * @return the number of coins the player should receive when an enemy is killed
     */
    public int getEnemyCoinYield() {
        return enemyCoinYield;
    }

    /**
     * @return the number of coins the player should receive when the wave is completed
     */
    public int getCompletionCoinYield() {
        return completionCoinYield;
    }
}