package lamana.model.building;

import lamana.contract.Attackable;
import lamana.contract.Upgradable;
import lamana.core.GameConfig;
import lamana.model.animal.Animal;
import lamana.model.economy.MoneyExp;

/**
 * Base (markas) milik pemain atau musuh.
 * Implements Attackable (bisa diserang) dan Upgradable (bisa di-upgrade).
 *
 * OOP Pillars:
 * - Encapsulation: semua field private
 * - Polymorphism: Base berlaku sebagai Attackable ketika diserang unit
 * - Abstraction: via interface Attackable & Upgradable
 */
public class Base implements Attackable, Upgradable {

    private int hp;
    private final int maxHp;
    private int level;
    private final Turret turret;
    private final double position;

    /**
     * @param position koordinat Base di lane [0, LANE_LENGTH]
     */
    public Base(double position) {
        if (position < 0 || position > GameConfig.LANE_LENGTH) {
            throw new IllegalArgumentException(
                    "Posisi Base harus dalam rentang [0, " + GameConfig.LANE_LENGTH + "].");
        }
        this.position = position;
        this.maxHp = GameConfig.BASE_HP;
        this.hp = GameConfig.BASE_HP;
        this.level = 1;
        this.turret = new Turret();
    }

    // ── Attackable ────────────────────────────────────────────────────────────

    @Override
    public void takeDamage(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Damage harus > 0.");
        }
        hp = Math.max(0, hp - amount);
    }

    @Override
    public boolean isDefeated() {
        return hp == 0;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public double getPosition() {
        return position;
    }

    // ── Upgradable ────────────────────────────────────────────────────────────

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public boolean canUpgrade(MoneyExp resources) {
        if (resources == null) return false;
        if (level >= GameConfig.BASE_MAX_LEVEL) return false;
        int expNeeded = getExpNeededForNextLevel();
        return resources.getExp() >= expNeeded;
    }

    @Override
    public boolean upgrade(MoneyExp resources) {
        if (!canUpgrade(resources)) return false;
        int expNeeded = getExpNeededForNextLevel();
        resources.spendExp(expNeeded);
        level++;
        turret.setLevel(level); // sync turret level
        return true;
    }

    public void syncLevel(int targetLevel) {
        if (targetLevel >= 1 && targetLevel <= lamana.core.GameConfig.BASE_MAX_LEVEL) {
            this.level = targetLevel;
            this.turret.setLevel(targetLevel);
        }
    }

    // ── Building-specific ────────────────────────────────────────────────────

    public Turret getTurret() {
        return turret;
    }

    /**
     * @return EXP yang dibutuhkan untuk naik level, 0 jika sudah max.
     */
    public int getExpNeededForNextLevel() {
        switch (level) {
            case 1: return GameConfig.EXP_FOR_LEVEL_2;   // 100
            case 2: return GameConfig.EXP_FOR_LEVEL_3;   // 200
            default: return 0;
        }
    }

    /**
     * Cek apakah Base bisa spawn Animal dengan resource yang ada.
     */
    public boolean canSpawn(Animal animal, MoneyExp resources) {
        if (animal == null || resources == null) return false;
        if (isDefeated()) return false;
        if (animal.getRequiredBaseLevel() > level) return false;
        return resources.getMoney() >= animal.getCost();
    }

    /**
     * Spawn Animal — potong gold dari resources.
     * @return false jika gagal (tidak cukup gold / level tidak memenuhi / Base hancur)
     */
    public boolean spawnAnimal(Animal animal, MoneyExp resources) {
        if (!canSpawn(animal, resources)) return false;
        return resources.spendMoney(animal.getCost());
    }

    /** Alias untuk isDefeated(), dipakai oleh controller. */
    public boolean isDestroyed() {
        return isDefeated();
    }
}
