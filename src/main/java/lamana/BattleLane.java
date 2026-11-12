package lamana;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Mengatur pergerakan unit, jarak antrean, serangan, dan reward kill di satu jalur. */
public class BattleLane {
    public static final double LANE_LENGTH = 20.0;
    private static final double UNIT_SPACING = 1.0;
    private static final long ATTACK_INTERVAL_MILLIS = 1_000L;

    private final Base playerBase;
    private final Tower enemyTower;
    private final MoneyExp playerResources;
    private final MoneyExp enemyResources;
    private final List<LaneUnit> playerUnits = new ArrayList<>();
    private final List<LaneUnit> enemyUnits = new ArrayList<>();
    private long attackTimerMillis;

    public BattleLane(
            Base playerBase,
            Tower enemyTower,
            MoneyExp playerResources,
            MoneyExp enemyResources) {
        if (playerBase == null || enemyTower == null
                || playerResources == null || enemyResources == null) {
            throw new IllegalArgumentException("Base, tower, dan resource tidak boleh null.");
        }
        this.playerBase = playerBase;
        this.enemyTower = enemyTower;
        this.playerResources = playerResources;
        this.enemyResources = enemyResources;
    }

    public void addPlayerAnimal(Animal animal) {
        addAnimal(playerUnits, animal, 0.0);
    }

    public void addEnemyAnimal(Animal animal) {
        addAnimal(enemyUnits, animal, LANE_LENGTH);
    }

    private void addAnimal(List<LaneUnit> units, Animal animal, double spawnPosition) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal tidak boleh null.");
        }
        if (animal.isDefeated()) {
            throw new IllegalArgumentException("Animal yang sudah kalah tidak bisa masuk jalur.");
        }
        units.add(new LaneUnit(animal, spawnPosition));
    }

    /** Dipanggil berulang kali oleh game loop dengan waktu sejak update terakhir (ms). */
    public void update(long deltaTimeMillis) {
        if (deltaTimeMillis < 0) {
            throw new IllegalArgumentException("Delta waktu tidak boleh negatif.");
        }

        moveAllUnits(deltaTimeMillis / 1000.0);
        updateTurrets(deltaTimeMillis);
        if (playerUnits.isEmpty() && enemyUnits.isEmpty()) {
            attackTimerMillis = 0;
            return;
        }

        attackTimerMillis += deltaTimeMillis;
        while (attackTimerMillis >= ATTACK_INTERVAL_MILLIS
                && (!playerUnits.isEmpty() || !enemyUnits.isEmpty())) {
            attackTimerMillis -= ATTACK_INTERVAL_MILLIS;
            performSimultaneousAttacks();
        }
    }

    private void updateTurrets(long deltaTimeMillis) {
        Turret playerTurret = playerBase.getTurret();
        Turret enemyTurret = enemyTower.getTurret();
        playerTurret.advance(deltaTimeMillis);
        enemyTurret.advance(deltaTimeMillis);

        LaneUnit enemyTarget = findEnemyNearPlayerBase();
        LaneUnit playerTarget = findPlayerNearEnemyTower();
        int damageToEnemy = 0;
        int damageToPlayer = 0;

        if (enemyTarget != null
                && playerTurret.isReadyToFire()
                && enemyTarget.position <= playerTurret.getRange()) {
            damageToEnemy = playerTurret.getDamage();
            playerTurret.consumeShot();
        }
        if (playerTarget != null
                && enemyTurret.isReadyToFire()
                && LANE_LENGTH - playerTarget.position <= enemyTurret.getRange()) {
            damageToPlayer = enemyTurret.getDamage();
            enemyTurret.consumeShot();
        }

        if (damageToEnemy > 0) {
            enemyTarget.animal.takeDamage(damageToEnemy);
            if (enemyTarget.animal.isDefeated()) {
                rewardKill(playerResources, enemyTarget.animal);
                enemyUnits.remove(enemyTarget);
            }
        }
        if (damageToPlayer > 0) {
            playerTarget.animal.takeDamage(damageToPlayer);
            if (playerTarget.animal.isDefeated()) {
                rewardKill(enemyResources, playerTarget.animal);
                playerUnits.remove(playerTarget);
            }
        }
    }

    private LaneUnit findEnemyNearPlayerBase() {
        LaneUnit nearest = null;
        for (LaneUnit unit : enemyUnits) {
            if (nearest == null || unit.position < nearest.position) {
                nearest = unit;
            }
        }
        return nearest;
    }

    private LaneUnit findPlayerNearEnemyTower() {
        LaneUnit nearest = null;
        for (LaneUnit unit : playerUnits) {
            if (nearest == null || unit.position > nearest.position) {
                nearest = unit;
            }
        }
        return nearest;
    }

    private void rewardKill(MoneyExp resources, Animal animal) {
        resources.addMoney(animal.getRewardMoney());
        resources.gainExp(animal.getRewardExp());
    }

    private void moveAllUnits(double deltaSeconds) {
        if (deltaSeconds == 0) {
            return;
        }

        Map<LaneUnit, Double> proposedPlayerPositions = proposePositions(
                playerUnits, enemyUnits, true, deltaSeconds);
        Map<LaneUnit, Double> proposedEnemyPositions = proposePositions(
                enemyUnits, playerUnits, false, deltaSeconds);

        preventFrontUnitsFromCrossing(proposedPlayerPositions, proposedEnemyPositions);
        applyPositionsWithSpacing(playerUnits, proposedPlayerPositions, true);
        applyPositionsWithSpacing(enemyUnits, proposedEnemyPositions, false);
    }

    private void preventFrontUnitsFromCrossing(
            Map<LaneUnit, Double> proposedPlayerPositions,
            Map<LaneUnit, Double> proposedEnemyPositions) {
        if (playerUnits.isEmpty() || enemyUnits.isEmpty()) {
            return;
        }

        LaneUnit playerFront = playerUnits.get(0);
        LaneUnit enemyFront = enemyUnits.get(0);
        double minimumGap = Math.max(1.0,
                Math.min(playerFront.animal.getRange(), enemyFront.animal.getRange()));
        double currentGap = enemyFront.position - playerFront.position;
        double proposedPlayer = proposedPlayerPositions.get(playerFront);
        double proposedEnemy = proposedEnemyPositions.get(enemyFront);

        if (proposedEnemy - proposedPlayer >= minimumGap) {
            return;
        }

        double playerMovement = Math.max(0, proposedPlayer - playerFront.position);
        double enemyMovement = Math.max(0, enemyFront.position - proposedEnemy);
        double totalMovement = playerMovement + enemyMovement;
        double allowedClosing = Math.max(0, currentGap - minimumGap);
        double scale = totalMovement == 0
                ? 0
                : Math.min(1, allowedClosing / totalMovement);

        proposedPlayerPositions.put(
                playerFront, playerFront.position + playerMovement * scale);
        proposedEnemyPositions.put(
                enemyFront, enemyFront.position - enemyMovement * scale);
    }

    private Map<LaneUnit, Double> proposePositions(
            List<LaneUnit> movingUnits,
            List<LaneUnit> opposingUnits,
            boolean movingRight,
            double deltaSeconds) {
        Map<LaneUnit, Double> positions = new IdentityHashMap<>();

        for (LaneUnit unit : movingUnits) {
            double currentPosition = unit.position;
            LaneUnit target = findNearestOpponentAhead(unit, opposingUnits, movingRight);
            double step = unit.animal.getMovementSpeed() * deltaSeconds;
            double newPosition;

            if (target == null) {
                newPosition = movingRight
                        ? Math.min(LANE_LENGTH, currentPosition + step)
                        : Math.max(0.0, currentPosition - step);
            } else if (movingRight) {
                double stopPosition = target.position - unit.animal.getRange();
                newPosition = Math.max(currentPosition,
                        Math.min(stopPosition, currentPosition + step));
            } else {
                double stopPosition = target.position + unit.animal.getRange();
                newPosition = Math.min(currentPosition,
                        Math.max(stopPosition, currentPosition - step));
            }

            positions.put(unit, Math.max(0.0, Math.min(LANE_LENGTH, newPosition)));
        }
        return positions;
    }

    private LaneUnit findNearestOpponentAhead(
            LaneUnit unit,
            List<LaneUnit> opponents,
            boolean movingRight) {
        LaneUnit nearest = null;
        for (LaneUnit opponent : opponents) {
            if (movingRight && opponent.position >= unit.position) {
                if (nearest == null || opponent.position < nearest.position) {
                    nearest = opponent;
                }
            } else if (!movingRight && opponent.position <= unit.position) {
                if (nearest == null || opponent.position > nearest.position) {
                    nearest = opponent;
                }
            }
        }
        return nearest;
    }

    private void applyPositionsWithSpacing(
            List<LaneUnit> units,
            Map<LaneUnit, Double> proposedPositions,
            boolean movingRight) {
        double leadingPosition = 0;
        boolean isFirstUnit = true;

        for (LaneUnit unit : units) {
            double proposed = proposedPositions.get(unit);
            if (isFirstUnit) {
                unit.position = movingRight
                        ? Math.max(unit.position, proposed)
                        : Math.min(unit.position, proposed);
                isFirstUnit = false;
            } else if (movingRight) {
                unit.position = Math.max(unit.position,
                        Math.min(proposed, leadingPosition - UNIT_SPACING));
            } else {
                unit.position = Math.min(unit.position,
                        Math.max(proposed, leadingPosition + UNIT_SPACING));
            }
            unit.position = Math.max(0.0, Math.min(LANE_LENGTH, unit.position));
            leadingPosition = unit.position;
        }
    }

    private void performSimultaneousAttacks() {
        Map<LaneUnit, Integer> damageToPlayers = collectDamage(enemyUnits, playerUnits, false);
        Map<LaneUnit, Integer> damageToEnemies = collectDamage(playerUnits, enemyUnits, true);
        int damageToPlayerBase = collectBuildingDamage(enemyUnits, playerUnits, false);
        int damageToEnemyTower = collectBuildingDamage(playerUnits, enemyUnits, true);

        applyDamage(damageToPlayers);
        applyDamage(damageToEnemies);
        if (damageToPlayerBase > 0) {
            playerBase.takeDamage(damageToPlayerBase);
        }
        if (damageToEnemyTower > 0) {
            enemyTower.takeDamage(damageToEnemyTower);
        }
        awardKillRewards(playerUnits, damageToPlayers, enemyResources);
        awardKillRewards(enemyUnits, damageToEnemies, playerResources);

        removeDefeated(playerUnits);
        removeDefeated(enemyUnits);
    }

    private int collectBuildingDamage(
            List<LaneUnit> attackers,
            List<LaneUnit> opposingUnits,
            boolean attackersMoveRight) {
        int totalDamage = 0;
        for (LaneUnit attacker : attackers) {
            if (findNearestOpponentAhead(attacker, opposingUnits, attackersMoveRight) != null) {
                continue;
            }

            double distanceToBuilding = attackersMoveRight
                    ? LANE_LENGTH - attacker.position
                    : attacker.position;
            if (distanceToBuilding <= attacker.animal.getRange()) {
                totalDamage += attacker.animal.getDamage();
            }
        }
        return totalDamage;
    }

    private Map<LaneUnit, Integer> collectDamage(
            List<LaneUnit> attackers,
            List<LaneUnit> targets,
            boolean attackersMoveRight) {
        Map<LaneUnit, Integer> damageByTarget = new IdentityHashMap<>();

        for (LaneUnit attacker : attackers) {
            LaneUnit target = findNearestOpponentAhead(attacker, targets, attackersMoveRight);
            if (target == null) {
                continue;
            }

            double distance = Math.abs(target.position - attacker.position);
            if (distance <= attacker.animal.getRange()) {
                damageByTarget.merge(target, attacker.animal.getDamage(), Integer::sum);
            }
        }
        return damageByTarget;
    }

    private void applyDamage(Map<LaneUnit, Integer> damageByTarget) {
        for (Map.Entry<LaneUnit, Integer> entry : damageByTarget.entrySet()) {
            entry.getKey().animal.takeDamage(entry.getValue());
        }
    }

    private void awardKillRewards(
            List<LaneUnit> defeatedSide,
            Map<LaneUnit, Integer> incomingDamage,
            MoneyExp winnerResources) {
        for (LaneUnit unit : defeatedSide) {
            if (unit.animal.isDefeated() && incomingDamage.containsKey(unit)) {
                winnerResources.addMoney(unit.animal.getRewardMoney());
                winnerResources.gainExp(unit.animal.getRewardExp());
            }
        }
    }

    private void removeDefeated(List<LaneUnit> units) {
        units.removeIf(unit -> unit.animal.isDefeated());
    }

    public Animal getPlayerFront() {
        return playerUnits.isEmpty() ? null : playerUnits.get(0).animal;
    }

    public Animal getEnemyFront() {
        return enemyUnits.isEmpty() ? null : enemyUnits.get(0).animal;
    }

    public int getPlayerQueueSize() {
        return Math.max(0, playerUnits.size() - 1);
    }

    public int getEnemyQueueSize() {
        return Math.max(0, enemyUnits.size() - 1);
    }

    public List<Animal> getPlayerAnimals() {
        return getAnimals(playerUnits);
    }

    public List<Animal> getEnemyAnimals() {
        return getAnimals(enemyUnits);
    }

    private List<Animal> getAnimals(List<LaneUnit> units) {
        List<Animal> animals = new ArrayList<>();
        for (LaneUnit unit : units) {
            animals.add(unit.animal);
        }
        return animals;
    }

    public double getPosition(Animal animal) {
        for (LaneUnit unit : playerUnits) {
            if (unit.animal == animal) {
                return unit.position;
            }
        }
        for (LaneUnit unit : enemyUnits) {
            if (unit.animal == animal) {
                return unit.position;
            }
        }
        return -1.0;
    }

    public boolean isPlayerDefeated() {
        return playerUnits.isEmpty();
    }

    public boolean isEnemyDefeated() {
        return enemyUnits.isEmpty();
    }

    private static class LaneUnit {
        private final Animal animal;
        private double position;

        private LaneUnit(Animal animal, double position) {
            this.animal = animal;
            this.position = position;
        }
    }
}
