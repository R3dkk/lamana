package lamana;

public class Base {
    private static final int MAX_LEVEL = 3;
    private static final int STARTING_HP = 1000;

    private int hp;
    private int level;
    private final Turret turret;

    public Base() {
        this.hp = STARTING_HP;
        this.level = 1;
        this.turret = new Turret();
    }

    public int getHp() {
        return hp;
    }

    public int getLevel() {
        return level;
    }

    public Turret getTurret() {
        return turret;
    }

    public int getExpNeededForNextLevel() {
        if (level == 1) {
            return 100;
        }
        if (level == 2) {
            return 200;
        }
        return 0;
    }

    public boolean canUpgrade(MoneyExp resources) {
        return resources != null
                && level < MAX_LEVEL
                && resources.getExp() >= getExpNeededForNextLevel();
    }

    public boolean upgrade(MoneyExp resources) {
        if (!canUpgrade(resources)) {
            return false;
        }

        if (!resources.spendExp(getExpNeededForNextLevel())) {
            return false;
        }

        level++;
        turret.setLevel(level);
        return true;
    }

    public boolean canSpawn(Animal animal, MoneyExp resources) {
        return animal != null
                && resources != null
                && animal.isUnlockedAtLevel(level)
                && resources.getMoney() >= animal.getCost();
    }

    /** Deducts the animal's cost; the caller adds it to the game lane on success. */
    public boolean spawnAnimal(Animal animal, MoneyExp resources) {
        if (!canSpawn(animal, resources)) {
            return false;
        }
        return resources.spendMoney(animal.getCost());
    }

    public void takeDamage(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Damage harus lebih dari 0.");
        }
        hp = Math.max(0, hp - amount);
    }

    public boolean isDestroyed() {
        return hp == 0;
    }
}
