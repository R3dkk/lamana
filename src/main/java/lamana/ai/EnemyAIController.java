package lamana.ai;

import lamana.core.GameConfig;
import lamana.factory.UnitFactory;
import lamana.model.animal.Animal;
import lamana.model.battle.BattleLane;
import lamana.model.building.Base;
import lamana.model.economy.MoneyExp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI controller for the enemy faction.
 * Handles passive income, spawn evaluation, and difficulty scaling.
 */
public class EnemyAIController {

    private final Base enemyBase;
    private final MoneyExp enemyResources;
    private final BattleLane lane;

    private long evalTimerMs;
    private long passiveIncomeTimerMs;
    private EnemyDifficulty difficulty;

    public EnemyAIController(Base enemyBase, MoneyExp enemyResources, BattleLane lane) {
        if (enemyBase == null) throw new IllegalArgumentException("enemyBase tidak boleh null.");
        if (enemyResources == null) throw new IllegalArgumentException("enemyResources tidak boleh null.");
        if (lane == null) throw new IllegalArgumentException("lane tidak boleh null.");
        this.enemyBase = enemyBase;
        this.enemyResources = enemyResources;
        this.lane = lane;
        this.difficulty = EnemyDifficulty.NORMAL;
        this.evalTimerMs = 0;
        this.passiveIncomeTimerMs = 0;
    }

    public void setDifficulty(EnemyDifficulty difficulty) {
        if (difficulty == null) throw new IllegalArgumentException("Difficulty tidak boleh null.");
        this.difficulty = difficulty;
    }

    public EnemyDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Called every game tick with elapsed time.
     * Handles passive income and periodic spawn evaluation.
     *
     * @param deltaMs time since last update in ms
     */
    public void update(long deltaMs) {
        if (deltaMs < 0) return;

        // Passive income every 1000ms
        passiveIncomeTimerMs += deltaMs;
        while (passiveIncomeTimerMs >= GameConfig.PASSIVE_INCOME_INTERVAL_MS) {
            passiveIncomeTimerMs -= GameConfig.PASSIVE_INCOME_INTERVAL_MS;
            enemyResources.addMoney(GameConfig.ENEMY_PASSIVE_INCOME);
        }

        // Eval interval depends on difficulty
        long evalInterval = switch (difficulty) {
            case EASY   -> 3000L;
            case NORMAL -> 2000L;
            case HARD   -> 1500L;
        };

        evalTimerMs += deltaMs;
        while (evalTimerMs >= evalInterval) {
            evalTimerMs -= evalInterval;
            spawnEnemy(lane);
        }
    }

    /**
     * Attempts to spawn an enemy animal into the lane.
     *
     * @param lane the battle lane to add the enemy to
     * @return true if an enemy was successfully spawned
     */
    public boolean spawnEnemy(BattleLane lane) {
        if (enemyBase == null || enemyBase.isDestroyed()) return false;

        List<String> availableTypes = UnitFactory.getTypesForLevel(enemyBase.getLevel());
        if (availableTypes.isEmpty()) return false;

        // Filter to affordable types
        List<String> affordable = new ArrayList<>();
        for (String type : availableTypes) {
            try {
                Animal prototype = UnitFactory.create(type);
                if (prototype.getCost() <= enemyResources.getMoney()) {
                    affordable.add(type);
                }
            } catch (IllegalArgumentException e) {
                // skip unknown types
            }
        }

        if (affordable.isEmpty()) return false;

        // Pick random affordable type
        String chosen = affordable.get(ThreadLocalRandom.current().nextInt(affordable.size()));
        Animal animal;
        try {
            animal = UnitFactory.create(chosen);
        } catch (IllegalArgumentException e) {
            return false;
        }

        int cost = animal.getCost();
        if (!enemyResources.spendMoney(cost)) return false;

        lane.addEnemyAnimal(animal);
        return true;
    }
}
