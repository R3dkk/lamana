package lamana.model.animal.melee;

import lamana.contract.Attackable;
import lamana.core.GameConfig;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;

/**
 * Abstraksi hewan petarung jarak dekat (default range = 1).
 * OOP Inheritance: MeleeAnimal extends Animal, konkret extends MeleeAnimal.
 */
public abstract class MeleeAnimal extends Animal {
    private final int requiredBaseLevel;

    protected MeleeAnimal(
            String name, int hp, int damage, int movementSpeed,
            int cost, int rewardMoney, int rewardExp, int requiredBaseLevel) {
        this(name, hp, damage, 0, movementSpeed, cost, rewardMoney, rewardExp, requiredBaseLevel);
    }

    protected MeleeAnimal(
            String name, int hp, int damage, int range, int movementSpeed,
            int cost, int rewardMoney, int rewardExp, int requiredBaseLevel) {
        super(name, hp, damage, range, movementSpeed, cost, rewardMoney, rewardExp,
              GameConfig.MELEE_ATTACK_COOLDOWN_MS);
        if (requiredBaseLevel < 1 || requiredBaseLevel > 3) {
            throw new IllegalArgumentException("Level markas harus 1-3.");
        }
        this.requiredBaseLevel = requiredBaseLevel;
    }

    @Override
    public int getRequiredBaseLevel() { return requiredBaseLevel; }

    @Override
    public double getCollisionRadius() { return 1.5; }

    /** Default melee attack — subclass dapat override untuk perilaku unik. */
    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getEffectiveRange()) return false;
        target.takeDamage(getDamage());
        return true;
    }
}
