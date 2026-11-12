package lamana;

public abstract class RangedAnimal extends Animal {
    private final int requiredBaseLevel;

    protected RangedAnimal(
            String name,
            int hp,
            int damage,
            int range,
            int movementSpeed,
            int cost,
            int rewardMoney,
            int rewardExp,
            int requiredBaseLevel) {
        super(name, hp, damage, range, movementSpeed, cost, rewardMoney, rewardExp);

        if (requiredBaseLevel < 1 || requiredBaseLevel > 3) {
            throw new IllegalArgumentException("Level markas harus antara 1 dan 3.");
        }
        this.requiredBaseLevel = requiredBaseLevel;
    }

    public int getRequiredBaseLevel() {
        return requiredBaseLevel;
    }

    public boolean isUnlocked(Base base) {
        return base != null && base.getLevel() >= requiredBaseLevel;
    }

    public boolean isUnlocked(Tower tower) {
        return tower != null && tower.getLevel() >= requiredBaseLevel;
    }
}
