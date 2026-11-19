package lamana.core;

public final class GameConfig {
    private GameConfig() {}

    public static final double LANE_LENGTH = 20.0;
    public static final double UNIT_SPACING = 1.0;

    public static final int BASE_HP = 1000;
    public static final int BASE_MAX_LEVEL = 3;

    public static final double TURRET_RANGE = 8.0;
    public static final int TURRET_DMG_LV1 = 10;
    public static final int TURRET_DMG_LV2 = 20;
    public static final int TURRET_DMG_LV3 = 35;

    public static final long TURRET_INTERVAL_LV1 = 2000L;
    public static final long TURRET_INTERVAL_LV2 = 1500L;
    public static final long TURRET_INTERVAL_LV3 = 1000L;

    public static final int PLAYER_STARTING_MONEY = 120;
    public static final int ENEMY_STARTING_MONEY = 120;

    public static final int PASSIVE_INCOME_LV1 = 5;
    public static final int PASSIVE_INCOME_LV2 = 8;
    public static final int PASSIVE_INCOME_LV3 = 12;

    public static final long PASSIVE_INCOME_INTERVAL_MS = 1000L;

    public static final int ENEMY_PASSIVE_INCOME = 4;

    public static final int EXP_FOR_LEVEL_2 = 100;
    public static final int EXP_FOR_LEVEL_3 = 200;

    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 620;
    public static final int GAME_LOOP_MS = 16;

    public static final long MELEE_ATTACK_COOLDOWN_MS = 1000L;
    public static final long RANGED_ATTACK_COOLDOWN_MS = 1200L;
    public static final long HEAVY_ATTACK_COOLDOWN_MS = 1500L;
}
