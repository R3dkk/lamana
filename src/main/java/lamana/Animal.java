package lamana;

public abstract class Animal {
    private final String name;
    private final int maxHp;
    private int hp;
    private final int damage;
    private final int range;
    private final int movementSpeed;
    private final int cost;
    private final int rewardMoney;
    private final int rewardExp;

    protected Animal(
            String name,
            int hp,
            int damage,
            int range,
            int movementSpeed,
            int cost,
            int rewardMoney,
            int rewardExp) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama hewan tidak boleh kosong.");
        }
        if (hp <= 0 || damage <= 0 || range < 0 || movementSpeed <= 0
                || cost <= 0 || rewardMoney <= 0 || rewardExp <= 0) {
            throw new IllegalArgumentException("Statistik hewan tidak valid.");
        }

        this.name = name.trim();
        this.maxHp = hp;
        this.hp = hp;
        this.damage = damage;
        this.range = range;
        this.movementSpeed = movementSpeed;
        this.cost = cost;
        this.rewardMoney = rewardMoney;
        this.rewardExp = rewardExp;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getDamage() {
        return damage;
    }

    public int getRange() {
        return range;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public int getCost() {
        return cost;
    }

    public abstract int getRequiredBaseLevel();

    public boolean isUnlockedAtLevel(int baseLevel) {
        return baseLevel >= getRequiredBaseLevel();
    }

    public int getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public void takeDamage(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Damage harus lebih dari 0.");
        }
        hp = Math.max(0, hp - amount);
    }

    public boolean isDefeated() {
        return hp == 0;
    }

    public boolean attack(Animal target, int distance) {
        if (target == null || target == this || target.isDefeated()) {
            return false;
        }
        if (distance < 0 || distance > range || isDefeated()) {
            return false;
        }

        target.takeDamage(damage);
        return true;
    }
}
