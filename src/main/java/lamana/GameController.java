package lamana;

public class GameController {
    private static final long PASSIVE_INCOME_INTERVAL_MILLIS = 1_000L;

    public enum GameStatus {
        RUNNING,
        PLAYER_WON,
        PLAYER_LOST,
        DRAW
    }

    private final Base playerBase;
    private final Tower enemyTower;
    private final MoneyExp playerResources;
    private final MoneyExp enemyResources;
    private final BattleLane battleLane;
    private GameStatus status;
    private long passiveIncomeTimerMillis;

    public GameController() {
        playerBase = new Base();
        enemyTower = new Tower();
        playerResources = new MoneyExp();
        enemyResources = new MoneyExp();
        playerResources.addMoney(120);
        battleLane = new BattleLane(
                playerBase, enemyTower, playerResources, enemyResources);
        status = GameStatus.RUNNING;
        passiveIncomeTimerMillis = 0;
    }

    public void update(long deltaTimeMillis) {
        if (deltaTimeMillis < 0) {
            throw new IllegalArgumentException("Delta waktu tidak boleh negatif.");
        }
        if (status != GameStatus.RUNNING) {
            return;
        }

        addPassiveIncome(deltaTimeMillis);

        Animal enemyAnimal = enemyTower.update(deltaTimeMillis);
        if (enemyAnimal != null) {
            battleLane.addEnemyAnimal(enemyAnimal);
        }

        battleLane.update(deltaTimeMillis);
        updateGameStatus();
    }

    private void addPassiveIncome(long deltaTimeMillis) {
        passiveIncomeTimerMillis += deltaTimeMillis;
        while (passiveIncomeTimerMillis >= PASSIVE_INCOME_INTERVAL_MILLIS) {
            passiveIncomeTimerMillis -= PASSIVE_INCOME_INTERVAL_MILLIS;
            playerResources.addMoney(getPassiveIncomePerSecond());
        }
    }

    public int getPassiveIncomePerSecond() {
        if (playerBase.getLevel() == 1) {
            return 5;
        }
        if (playerBase.getLevel() == 2) {
            return 8;
        }
        return 12;
    }

    public boolean spawnPlayerAnimal(Animal animal) {
        if (status != GameStatus.RUNNING || !playerBase.spawnAnimal(animal, playerResources)) {
            return false;
        }
        battleLane.addPlayerAnimal(animal);
        return true;
    }

    public boolean canSpawnPlayerAnimal(Animal animal) {
        return status == GameStatus.RUNNING
                && playerBase.canSpawn(animal, playerResources);
    }

    public boolean upgradePlayerBase() {
        return status == GameStatus.RUNNING && playerBase.upgrade(playerResources);
    }

    private void updateGameStatus() {
        boolean playerDestroyed = playerBase.isDestroyed();
        boolean enemyDestroyed = enemyTower.isDestroyed();

        if (playerDestroyed && enemyDestroyed) {
            status = GameStatus.DRAW;
        } else if (enemyDestroyed) {
            status = GameStatus.PLAYER_WON;
        } else if (playerDestroyed) {
            status = GameStatus.PLAYER_LOST;
        }
    }

    public Base getPlayerBase() {
        return playerBase;
    }

    public Tower getEnemyTower() {
        return enemyTower;
    }

    public MoneyExp getPlayerResources() {
        return playerResources;
    }

    public MoneyExp getEnemyResources() {
        return enemyResources;
    }

    public BattleLane getBattleLane() {
        return battleLane;
    }

    public GameStatus getStatus() {
        return status;
    }
}
