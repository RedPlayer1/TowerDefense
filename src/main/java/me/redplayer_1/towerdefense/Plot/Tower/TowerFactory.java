package me.redplayer_1.towerdefense.Plot.Tower;

import me.redplayer_1.towerdefense.Geometry.BlockMesh;
import me.redplayer_1.towerdefense.Geometry.MeshEditor;
import me.redplayer_1.towerdefense.Geometry.Vector3;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TowerFactory {
    private @Nullable String name;
    private @Nullable ItemStack item;
    private @Nullable Vector3 particlePoint;
    private int range;
    private int damage;
    private int targets;
    private int attackDelay;
    private int cost;

    private @Nullable BlockMesh mesh;
    private @Nullable MeshEditor editor;

    /**
     * Creates a new TowerFactory
     */
    public TowerFactory() {
        // set all vars to invalid values
        name = null;
        item = null;
        range = -2;
        damage = 0;
        targets = 0;
        attackDelay = -1;
        mesh = null;
        editor = null;
    }

    /**
     * Create sa new TowerFactory with values supplied by a preexisting tower
     * @param tower the tower to get values from
     */
    public TowerFactory(Tower tower) {
        name = tower.name;
        item = tower.getItem();
        range = tower.getRange();
        mesh = tower.getMesh();
        editor = null;
    }

    public TowerFactory setName(String name) {
        this.name = name;
        return this;
    }

    public TowerFactory setItem(ItemStack item) {
        this.item = item;
        return this;
    }

    public TowerFactory setRange(int range) {
        this.range = range;
        return this;
    }

    public TowerFactory setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public TowerFactory setMesh(BlockMesh mesh) {
        this.mesh = mesh;
        return this;
    }

    public TowerFactory setEditor(MeshEditor editor) {
        this.editor = editor;
        return this;
    }

    public TowerFactory setParticlePoint(Vector3 particlePoint) {
        this.particlePoint = particlePoint;
        return this;
    }

    public TowerFactory setAttackDelay(int attackDelay) {
        this.attackDelay = attackDelay;
        return this;
    }

    public TowerFactory setTargets(int targets) {
        this.targets = targets;
        return this;
    }

    public TowerFactory setCost(int cost) {
        this.cost = cost;
        return this;
    }

    public @Nullable BlockMesh getMesh() {
        return mesh;
    }

    public @Nullable MeshEditor getEditor() {
        return editor;
    }

    /**
     * Creates a new tower. Required fields are:<ul>
     *     <li>name</li>
     *     <li>item</li>
     *     <li>range</li>
     *     <li>a BlockMesh or MeshEditor</li>
     * </ul>
     * Optional fields (or ones with a default value) are:<ul>
     *     <li>Damage</li>
     * </ul>
     * @return the new tower
     * @throws IllegalStateException if any of the required fields are not set or have invalid values
     */
    public Tower build() throws IllegalStateException {
        if (name == null || item == null || range <= -2 || targets <= 0 || attackDelay < 0
                || (mesh == null && editor == null) || particlePoint == null)
        {
            throw new IllegalStateException("Missing required factory field(s)");
        }
        return new Tower(
                name, item, mesh != null? mesh : editor.close(true),
                particlePoint, range, damage, targets, attackDelay
        );
    }
}
