package lamana.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import lamana.ai.EnemyAIController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.factory.UnitFactory;
import lamana.model.animal.Animal;
import lamana.model.battle.BattleLane;
import lamana.model.building.Base;
import lamana.model.economy.MoneyExp;

public class GameController {
    private final Base playerBase;
    private final Base enemyBase;
    private final MoneyExp playerResources;
    private final MoneyExp enemyResources;
    private final BattleLane battleLane;
    private final EnemyAIController enemyAI;
    private final Deque<Animal> playerSpawnQueue;
    private GameState state;
    private long passiveIncomeTimerMs;
    private long playerSpawnCooldownRemainingMs;

    public GameController() {
        playerBase = new Base(0.0);
        enemyBase = new Base(GameConfig.LANE_LENGTH);
        playerResources = new MoneyExp(GameConfig.PLAYER_STARTING_MONEY, 0);
        enemyResources = new MoneyExp(GameConfig.ENEMY_STARTING_MONEY, 0);
        battleLane = new BattleLane(playerBase, enemyBase, playerResources, enemyResources);
        enemyAI = new EnemyAIController(enemyBase, enemyResources, battleLane);
        playerSpawnQueue = new ArrayDeque<>();
        state = GameState.PLAYING;
        passiveIncomeTimerMs = 0;
        playerSpawnCooldownRemainingMs = 0;
    }

    public void update(long deltaMs) {
        if (deltaMs < 0) throw new IllegalArgumentException("Delta tidak boleh negatif.");
        if (state != GameState.PLAYING) return;

        // Passive income player
        passiveIncomeTimerMs += deltaMs;
        while (passiveIncomeTimerMs >= GameConfig.PASSIVE_INCOME_INTERVAL_MS) {
            passiveIncomeTimerMs -= GameConfig.PASSIVE_INCOME_INTERVAL_MS;
            playerResources.addMoney(getPassiveIncomePerSecond());
        }

        updatePlayerSpawnQueue(deltaMs);

        // Enemy AI update (AI spawns + passive income)
        enemyAI.update(deltaMs);

        // Battle lane update (movement + combat)
        battleLane.update(deltaMs);

        // Cek kondisi menang/kalah
        updateGameState();
    }

    private void updateGameState() {
        boolean playerDead = playerBase.isDestroyed();
        boolean enemyDead = enemyBase.isDestroyed();
        if (playerDead && enemyDead) state = GameState.DRAW;
        else if (enemyDead) state = GameState.VICTORY;
        else if (playerDead) state = GameState.DEFEAT;
    }

    public int getPassiveIncomePerSecond() {
        switch (playerBase.getLevel()) {
            case 1:
                return GameConfig.PASSIVE_INCOME_LV1;
            case 2:
                return GameConfig.PASSIVE_INCOME_LV2;
            default:
                return GameConfig.PASSIVE_INCOME_LV3;
        }
    }

    public boolean spawnPlayerAnimal(String type) {
        if (state != GameState.PLAYING || playerBase.isDestroyed()) return false;
        if (playerSpawnQueue.size() >= GameConfig.PLAYER_SPAWN_QUEUE_CAPACITY) return false;
        Animal animal;
        try { animal = UnitFactory.create(type); }
        catch (IllegalArgumentException e) { return false; }
        if (!playerBase.spawnAnimal(animal, playerResources)) return false;
        boolean queueWasEmpty = playerSpawnQueue.isEmpty();
        playerSpawnQueue.addLast(animal);
        if (queueWasEmpty) {
            playerSpawnCooldownRemainingMs = GameConfig.PLAYER_SPAWN_COOLDOWN_MS;
        }
        return true;
    }

    public boolean canSpawnPlayerAnimal(String type) {
        if (state != GameState.PLAYING) return false;
        if (playerSpawnQueue.size() >= GameConfig.PLAYER_SPAWN_QUEUE_CAPACITY) return false;
        try {
            Animal test = UnitFactory.create(type);
            return playerBase.canSpawn(test, playerResources);
        } catch (IllegalArgumentException e) { return false; }
    }

    private void updatePlayerSpawnQueue(long deltaMs) {
        long elapsedMs = deltaMs;
        while (!playerSpawnQueue.isEmpty() && elapsedMs >= playerSpawnCooldownRemainingMs) {
            elapsedMs -= playerSpawnCooldownRemainingMs;
            battleLane.addPlayerAnimal(playerSpawnQueue.removeFirst());
            if (playerSpawnQueue.isEmpty()) {
                playerSpawnCooldownRemainingMs = 0;
                return;
            }
            playerSpawnCooldownRemainingMs = GameConfig.PLAYER_SPAWN_COOLDOWN_MS;
        }
        if (!playerSpawnQueue.isEmpty()) {
            playerSpawnCooldownRemainingMs -= elapsedMs;
        }
    }

    public List<Animal> getPlayerSpawnQueue() {
        return Collections.unmodifiableList(new ArrayList<>(playerSpawnQueue));
    }

    public int getPlayerSpawnQueueCapacity() {
        return GameConfig.PLAYER_SPAWN_QUEUE_CAPACITY;
    }

    public long getPlayerSpawnCooldownRemainingMs() {
        return playerSpawnCooldownRemainingMs;
    }

    public double getPlayerSpawnCooldownProgress() {
        if (playerSpawnQueue.isEmpty()) return 0.0;
        return 1.0 - (double) playerSpawnCooldownRemainingMs / GameConfig.PLAYER_SPAWN_COOLDOWN_MS;
    }

    public boolean upgradePlayerBase() {
        if (state != GameState.PLAYING) return false;
        boolean success = playerBase.upgrade(playerResources);
        if (success) {
            enemyBase.syncLevel(playerBase.getLevel());
        }
        return success;
    }

    // Getters
    public EnemyAIController getEnemyAI() { return enemyAI; }
    public Base getPlayerBase() { return playerBase; }
    public Base getEnemyBase() { return enemyBase; }
    public MoneyExp getPlayerResources() { return playerResources; }
    public MoneyExp getEnemyResources() { return enemyResources; }
    public BattleLane getBattleLane() { return battleLane; }
    public GameState getState() { return state; }
}
