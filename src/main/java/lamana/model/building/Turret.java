package lamana.model.building;

import lamana.core.GameConfig;

/**
 * Turret milik Base — menyerang unit musuh yang masuk dalam jangkauan.
 * Level 1-3 meningkatkan damage dan kecepatan tembak.
 */
public class Turret {

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
        switch (level) {
            case 1: return GameConfig.TURRET_DMG_LV1;
            case 2: return GameConfig.TURRET_DMG_LV2;
            default: return GameConfig.TURRET_DMG_LV3;
        }
    }

    public double getRange() {
        return GameConfig.TURRET_RANGE;
    }

    public long getAttackIntervalMillis() {
        switch (level) {
            case 1: return GameConfig.TURRET_INTERVAL_LV1;
            case 2: return GameConfig.TURRET_INTERVAL_LV2;
            default: return GameConfig.TURRET_INTERVAL_LV3;
        }
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
