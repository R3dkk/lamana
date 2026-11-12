package lamana;

public class Turret {
    private static final double RANGE = 8.0;

    private int level;
    private long attackTimerMillis;

    public Turret() {
        this.level = 1;
        this.attackTimerMillis = 0;
    }

    public void setLevel(int level) {
        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("Level turret harus antara 1 dan 3.");
        }
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public int getDamage() {
        if (level == 1) {
            return 10;
        }
        if (level == 2) {
            return 20;
        }
        return 35;
    }

    public double getRange() {
        return RANGE;
    }

    public long getAttackIntervalMillis() {
        if (level == 1) {
            return 2_000L;
        }
        if (level == 2) {
            return 1_500L;
        }
        return 1_000L;
    }

    public double getAttacksPerSecond() {
        return 1_000.0 / getAttackIntervalMillis();
    }

    public void advance(long deltaTimeMillis) {
        if (deltaTimeMillis < 0) {
            throw new IllegalArgumentException("Delta waktu tidak boleh negatif.");
        }
        attackTimerMillis = Math.min(
                getAttackIntervalMillis(), attackTimerMillis + deltaTimeMillis);
    }

    public boolean isReadyToFire() {
        return attackTimerMillis >= getAttackIntervalMillis();
    }

    public void consumeShot() {
        if (isReadyToFire()) {
            attackTimerMillis -= getAttackIntervalMillis();
        }
    }
}
