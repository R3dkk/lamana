package lamana;

import java.util.concurrent.ThreadLocalRandom;

public class Tower {
    private static final int STARTING_HP = 1000;
    private static final long DEFAULT_LEVEL_2_TIME = 120_000L;
    private static final long DEFAULT_LEVEL_3_TIME = 300_000L;
    private static final long LEVEL_1_SPAWN_INTERVAL = 5_000L;
    private static final long LEVEL_2_SPAWN_INTERVAL = 4_000L;
    private static final long LEVEL_3_SPAWN_INTERVAL = 3_000L;

    private final int maxHp;
    private int hp;
    private final long level2AtMillis;
    private final long level3AtMillis;
    private long elapsedMillis;
    private long spawnTimerMillis;
    private int level;
    private final Turret turret;

    public Tower() {
        this(STARTING_HP, DEFAULT_LEVEL_2_TIME, DEFAULT_LEVEL_3_TIME);
    }

    public Tower(int hp) {
        this(hp, DEFAULT_LEVEL_2_TIME, DEFAULT_LEVEL_3_TIME);
    }

    public Tower(int hp, long level2AtMillis, long level3AtMillis) {
        if (hp <= 0) {
            throw new IllegalArgumentException("HP tower harus lebih dari 0.");
        }
        if (level2AtMillis <= 0 || level3AtMillis <= level2AtMillis) {
            throw new IllegalArgumentException("Waktu level harus berurutan dan lebih dari 0.");
        }
        this.maxHp = hp;
        this.hp = hp;
        this.level2AtMillis = level2AtMillis;
        this.level3AtMillis = level3AtMillis;
        this.elapsedMillis = 0;
        this.spawnTimerMillis = 0;
        this.level = 1;
        this.turret = new Turret();
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getLevel() {
        return level;
    }

    public Turret getTurret() {
        return turret;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public long getTimeUntilNextUpgradeMillis() {
        if (level == 1) {
            return Math.max(0, level2AtMillis - elapsedMillis);
        }
        if (level == 2) {
            return Math.max(0, level3AtMillis - elapsedMillis);
        }
        return 0;
    }

    public Animal update(long deltaTimeMillis) {
        if (deltaTimeMillis < 0) {
            throw new IllegalArgumentException("Delta waktu tidak boleh negatif.");
        }

        elapsedMillis += deltaTimeMillis;
        if (level == 1 && elapsedMillis >= level2AtMillis) {
            level = 2;
            turret.setLevel(level);
        }
        if (level == 2 && elapsedMillis >= level3AtMillis) {
            level = 3;
            turret.setLevel(level);
        }

        if (isDestroyed()) {
            return null;
        }

        spawnTimerMillis += deltaTimeMillis;
        if (spawnTimerMillis < getSpawnIntervalMillis()) {
            return null;
        }

        // Spawn maksimal satu unit per update agar lonjakan delta tidak menumpuk spawn.
        spawnTimerMillis = 0;
        return createRandomUnlockedAnimal();
    }

    public long getSpawnIntervalMillis() {
        if (level == 1) {
            return LEVEL_1_SPAWN_INTERVAL;
        }
        if (level == 2) {
            return LEVEL_2_SPAWN_INTERVAL;
        }
        return LEVEL_3_SPAWN_INTERVAL;
    }

    private Animal createRandomUnlockedAnimal() {
        Animal[] availableAnimals;
        if (level == 1) {
            availableAnimals = new Animal[] {new Ayam(), new Landak(), new KuraKura()};
        } else if (level == 2) {
            availableAnimals = new Animal[] {
                new Ayam(), new Landak(), new KuraKura(),
                new Harimau(), new Monyet(), new Badak()
            };
        } else {
            availableAnimals = new Animal[] {
                new Ayam(), new Landak(), new KuraKura(),
                new Harimau(), new Monyet(), new Badak(),
                new Godzilla(), new Naga(), new Centaur()
            };
        }

        int index = ThreadLocalRandom.current().nextInt(availableAnimals.length);
        return availableAnimals[index];
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
