package lamana.model.animal.ranged;

import lamana.contract.Attackable;
import lamana.core.GameConfig;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;

/**
 * Abstraksi hewan penyerang jarak jauh (range configurable).
 * OOP Inheritance: RangedAnimal extends Animal.
 */
public abstract class RangedAnimal extends Animal {
    private final int requiredBaseLevel;

    protected RangedAnimal(
            String name, int hp, int damage, int range, int movementSpeed,
            int cost, int rewardMoney, int rewardExp, int requiredBaseLevel) {
        super(name, hp, damage, range, movementSpeed, cost, rewardMoney, rewardExp,
              GameConfig.RANGED_ATTACK_COOLDOWN_MS);
        if (requiredBaseLevel < 1 || requiredBaseLevel > 3) {
            throw new IllegalArgumentException("Level markas harus 1-3.");
        }
        this.requiredBaseLevel = requiredBaseLevel;
    }

    @Override
    public int getRequiredBaseLevel() { return requiredBaseLevel; }

    @Override
    public double getCollisionRadius() { return 1.35; }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getEffectiveRange()) return false;
        target.takeDamage(getDamage());
        return true;
    }
}
