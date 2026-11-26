package lamana.model.battle;

import lamana.core.GameConfig;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;
import lamana.model.building.Base;
import lamana.model.building.Turret;
import lamana.model.economy.MoneyExp;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mengatur pergerakan unit, jarak antrean, serangan polimorfis,
 * turret firing, dan reward kill di satu jalur pertempuran horizontal.
 *
 * Menggunakan dua instance Base (tanpa Tower).
 */
public class BattleLane {

    private final Base playerBase;
    private final Base enemyBase;
    private final MoneyExp playerResources;
    private final MoneyExp enemyResources;

    private final List<Animal> playerUnits = new ArrayList<>();
    private final List<Animal> enemyUnits  = new ArrayList<>();
    private final List<TurretShot> turretShotsThisUpdate = new ArrayList<>();

    public BattleLane(
            Base playerBase,
            Base enemyBase,
            MoneyExp playerResources,
            MoneyExp enemyResources) {
        if (playerBase == null || enemyBase == null
                || playerResources == null || enemyResources == null) {
            throw new IllegalArgumentException("Base dan resource tidak boleh null.");
        }
        this.playerBase = playerBase;
        this.enemyBase = enemyBase;
        this.playerResources = playerResources;
        this.enemyResources = enemyResources;
    }

    public void addPlayerAnimal(Animal animal) {
        if (animal == null) throw new IllegalArgumentException("Animal tidak boleh null.");
        if (animal.isDefeated()) throw new IllegalArgumentException("Animal kalah tidak bisa ditambahkan.");
        animal.setPosition(0.0);
        playerUnits.add(animal);
    }

    public void addEnemyAnimal(Animal animal) {
        if (animal == null) throw new IllegalArgumentException("Animal tidak boleh null.");
        if (animal.isDefeated()) throw new IllegalArgumentException("Animal kalah tidak bisa ditambahkan.");
        animal.setPosition(GameConfig.LANE_LENGTH);
        enemyUnits.add(animal);
    }

    public void update(long deltaTimeMillis) {
        if (deltaTimeMillis < 0) {
            throw new IllegalArgumentException("Delta waktu tidak boleh negatif.");
        }
        turretShotsThisUpdate.clear();

        // 1. Advance stun & cooldown timers
        for (Animal a : playerUnits) {
            a.advanceStun(deltaTimeMillis);
            a.advanceCooldown(deltaTimeMillis);
            a.advanceAttackAnimation(deltaTimeMillis);
        }
        for (Animal a : enemyUnits) {
            a.advanceStun(deltaTimeMillis);
            a.advanceCooldown(deltaTimeMillis);
            a.advanceAttackAnimation(deltaTimeMillis);
        }

        // 2. Pergerakan unit dengan delta time & spacing
        moveAllUnits(deltaTimeMillis / 1000.0);

        // 3. Serangan unit (Polymorphism: animal.attack via dynamic dispatch)
        performUnitAttacks();

        // 4. Update Turret pertahanan masing-masing base
        updateTurrets(deltaTimeMillis);

        // 5. Bersihkan unit yang kalah & beri reward
        removeDefeated();
    }

    private void moveAllUnits(double deltaSeconds) {
        if (deltaSeconds <= 0) return;

        // Catat posisi lama sebelum dipindahkan untuk mendeteksi pergerakan nyata
        Map<Animal, Double> oldPositions = new IdentityHashMap<>();
        for (Animal a : playerUnits) {
            a.setMovedThisTick(false);
            oldPositions.put(a, a.getPosition());
        }
        for (Animal a : enemyUnits) {
            a.setMovedThisTick(false);
            oldPositions.put(a, a.getPosition());
        }

        Map<Animal, Double> proposedPlayerPositions = proposePositions(playerUnits, enemyUnits, true, deltaSeconds);
        Map<Animal, Double> proposedEnemyPositions  = proposePositions(enemyUnits, playerUnits, false, deltaSeconds);

        preventFrontUnitsFromCrossing(proposedPlayerPositions, proposedEnemyPositions);

        // Terapkan posisi baru
        applyPositionsWithSpacing(playerUnits, proposedPlayerPositions, true);
        applyPositionsWithSpacing(enemyUnits, proposedEnemyPositions, false);

        // Set movedThisTick hanya jika posisi benar-benar berubah setelah spacing
        for (Animal a : playerUnits) {
            Double oldPos = oldPositions.get(a);
            if (oldPos != null && Math.abs(oldPos - a.getPosition()) > 0.0001) {
                a.setMovedThisTick(true);
            }
        }
        for (Animal a : enemyUnits) {
            Double oldPos = oldPositions.get(a);
            if (oldPos != null && Math.abs(oldPos - a.getPosition()) > 0.0001) {
                a.setMovedThisTick(true);
            }
        }
    }

    private Map<Animal, Double> proposePositions(
            List<Animal> movingUnits,
            List<Animal> opposingUnits,
            boolean movingRight,
            double deltaSeconds) {
        Map<Animal, Double> positions = new IdentityHashMap<>();

        for (Animal unit : movingUnits) {
            if (unit.isStunned()) {
                positions.put(unit, unit.getPosition());
                continue;
            }

            double currentPos = unit.getPosition();
            Animal target = findNearestOpponentAhead(unit, opposingUnits, movingRight);
            double step = unit.getMovementSpeed() * deltaSeconds;
            double newPosition;

            if (target == null) {
                // Menuju base musuh
                if (movingRight) {
                    double stopPosition = GameConfig.LANE_LENGTH
                            - unit.getRange() - unit.getCollisionRadius();
                    newPosition = Math.min(stopPosition, currentPos + step);
                    newPosition = Math.max(currentPos, newPosition);
                } else {
                    double stopPosition = unit.getRange() + unit.getCollisionRadius();
                    newPosition = Math.max(stopPosition, currentPos - step);
                    newPosition = Math.min(currentPos, newPosition);
                }
            } else if (movingRight) {
                double stopPosition = target.getPosition() - unit.getRange()
                        - unit.getCollisionRadius() - target.getCollisionRadius();
                newPosition = Math.max(currentPos, Math.min(stopPosition, currentPos + step));
            } else {
                double stopPosition = target.getPosition() + unit.getRange()
                        + unit.getCollisionRadius() + target.getCollisionRadius();
                newPosition = Math.min(currentPos, Math.max(stopPosition, currentPos - step));
            }

            positions.put(unit, Math.max(0.0, Math.min(GameConfig.LANE_LENGTH, newPosition)));
        }
        return positions;
    }

    private void preventFrontUnitsFromCrossing(
            Map<Animal, Double> proposedPlayerPositions,
            Map<Animal, Double> proposedEnemyPositions) {
        if (playerUnits.isEmpty() || enemyUnits.isEmpty()) return;

        Animal playerFront = playerUnits.get(0);
        Animal enemyFront  = enemyUnits.get(0);

        double minimumGap = playerFront.getCollisionRadius() + enemyFront.getCollisionRadius();
        double currentGap = enemyFront.getPosition() - playerFront.getPosition();
        double proposedPlayer = proposedPlayerPositions.get(playerFront);
        double proposedEnemy  = proposedEnemyPositions.get(enemyFront);

        if (proposedEnemy - proposedPlayer >= minimumGap) return;

        double playerMovement = Math.max(0, proposedPlayer - playerFront.getPosition());
        double enemyMovement  = Math.max(0, enemyFront.getPosition() - proposedEnemy);
        double totalMovement  = playerMovement + enemyMovement;
        double allowedClosing = Math.max(0, currentGap - minimumGap);
        double scale = totalMovement == 0 ? 0 : Math.min(1.0, allowedClosing / totalMovement);

        proposedPlayerPositions.put(playerFront, playerFront.getPosition() + playerMovement * scale);
        proposedEnemyPositions.put(enemyFront, enemyFront.getPosition() - enemyMovement * scale);
    }

    private void applyPositionsWithSpacing(
            List<Animal> units,
            Map<Animal, Double> proposedPositions,
            boolean movingRight) {
        double leadingPosition = 0;
        Animal leadingUnit = null;
        boolean isFirstUnit = true;

        for (Animal unit : units) {
            Double proposed = proposedPositions.get(unit);
            if (proposed == null) proposed = unit.getPosition();

            if (isFirstUnit) {
                unit.setPosition(movingRight
                        ? Math.max(unit.getPosition(), proposed)
                        : Math.min(unit.getPosition(), proposed));
                isFirstUnit = false;
            } else if (movingRight) {
                double spacing = leadingUnit.getCollisionRadius() + unit.getCollisionRadius()
                        + GameConfig.UNIT_QUEUE_MARGIN;
                unit.setPosition(Math.max(unit.getPosition(),
                        Math.min(proposed, leadingPosition - spacing)));
            } else {
                double spacing = leadingUnit.getCollisionRadius() + unit.getCollisionRadius()
                        + GameConfig.UNIT_QUEUE_MARGIN;
                unit.setPosition(Math.min(unit.getPosition(),
                        Math.max(proposed, leadingPosition + spacing)));
            }
            unit.setPosition(Math.max(0.0, Math.min(GameConfig.LANE_LENGTH, unit.getPosition())));
            leadingPosition = unit.getPosition();
            leadingUnit = unit;
        }
    }

    private Animal findNearestOpponentAhead(
            Animal unit,
            List<Animal> opponents,
            boolean movingRight) {
        Animal nearest = null;
        for (Animal opp : opponents) {
            if (opp.isDefeated()) continue;
            if (movingRight && opp.getPosition() >= unit.getPosition()) {
                if (nearest == null || opp.getPosition() < nearest.getPosition()) {
                    nearest = opp;
                }
            } else if (!movingRight && opp.getPosition() <= unit.getPosition()) {
                if (nearest == null || opp.getPosition() > nearest.getPosition()) {
                    nearest = opp;
                }
            }
        }
        return nearest;
    }

    /**
     * Mengeksekusi serangan unit menggunakan dynamic dispatch polymorphism.
     * Tidak ada "if (a instanceof Ayam)" di sini!
     */
    private void performUnitAttacks() {
        AttackContext playerCtx = new AttackContext(new ArrayList<>(enemyUnits), true);
        AttackContext enemyCtx  = new AttackContext(new ArrayList<>(playerUnits), false);

        // Player units attack
        for (Animal attacker : playerUnits) {
            // FITUR BARU: Cek hasMovedThisTick(), jika true maka skip (tidak boleh serang)
            if (attacker.isDefeated() || attacker.isStunned() || !attacker.isReadyToAttack() || attacker.hasMovedThisTick()) continue;

            Animal targetAnimal = findNearestOpponentAhead(attacker, enemyUnits, true);
            if (targetAnimal != null) {
                double dist = Math.max(0.0, Math.abs(targetAnimal.getPosition() - attacker.getPosition())
                        - attacker.getCollisionRadius() - targetAnimal.getCollisionRadius());
                if (dist <= attacker.getEffectiveRange()) {
                    if (attacker.attack(targetAnimal, dist, playerCtx)) {
                        attacker.consumeAttack();
                        attacker.triggerAttackAnimation(440L);
                    }
                }
            } else {
                double distToBase = Math.max(0.0, GameConfig.LANE_LENGTH - attacker.getPosition()
                        - attacker.getCollisionRadius());
                if (distToBase <= attacker.getEffectiveRange()) {
                    if (attacker.attack(enemyBase, distToBase, playerCtx)) {
                        attacker.consumeAttack();
                        attacker.triggerAttackAnimation(440L);
                    }
                }
            }
        }

        // Enemy units attack
        for (Animal attacker : enemyUnits) {
            // FITUR BARU: Cek hasMovedThisTick()
            if (attacker.isDefeated() || attacker.isStunned() || !attacker.isReadyToAttack() || attacker.hasMovedThisTick()) continue;

            Animal targetAnimal = findNearestOpponentAhead(attacker, playerUnits, false);
            if (targetAnimal != null) {
                double dist = Math.max(0.0, Math.abs(targetAnimal.getPosition() - attacker.getPosition())
                        - attacker.getCollisionRadius() - targetAnimal.getCollisionRadius());
                if (dist <= attacker.getEffectiveRange()) {
                    if (attacker.attack(targetAnimal, dist, enemyCtx)) {
                        attacker.consumeAttack();
                        attacker.triggerAttackAnimation(440L);
                    }
                }
            } else {
                double distToBase = Math.max(0.0, attacker.getPosition() - attacker.getCollisionRadius());
                if (distToBase <= attacker.getEffectiveRange()) {
                    if (attacker.attack(playerBase, distToBase, enemyCtx)) {
                        attacker.consumeAttack();
                        attacker.triggerAttackAnimation(440L);
                    }
                }
            }
        }
    }

    private void updateTurrets(long deltaTimeMillis) {
        Turret pt = playerBase.getTurret();
        Turret et = enemyBase.getTurret();
        pt.advance(deltaTimeMillis);
        et.advance(deltaTimeMillis);

        // Player turret fires at nearest enemy in range
        if (pt.isReadyToFire()) {
            Animal target = findNearestEnemyToPlayerBase();
            if (target != null && target.getPosition() <= pt.getRange()) {
                turretShotsThisUpdate.add(new TurretShot(true, target.getPosition()));
                target.takeDamage(pt.getDamage());
                pt.consumeShot();
            }
        }

        // Enemy turret fires at nearest player unit in range
        if (et.isReadyToFire()) {
            Animal target = findNearestPlayerToEnemyBase();
            if (target != null && (GameConfig.LANE_LENGTH - target.getPosition()) <= et.getRange()) {
                turretShotsThisUpdate.add(new TurretShot(false, target.getPosition()));
                target.takeDamage(et.getDamage());
                et.consumeShot();
            }
        }
    }

    private Animal findNearestEnemyToPlayerBase() {
        Animal nearest = null;
        for (Animal a : enemyUnits) {
            if (a.isDefeated()) continue;
            if (nearest == null || a.getPosition() < nearest.getPosition()) {
                nearest = a;
            }
        }
        return nearest;
    }

    private Animal findNearestPlayerToEnemyBase() {
        Animal nearest = null;
        for (Animal a : playerUnits) {
            if (a.isDefeated()) continue;
            if (nearest == null || a.getPosition() > nearest.getPosition()) {
                nearest = a;
            }
        }
        return nearest;
    }

    public void removeDefeated() {
        // Player defeated -> reward enemy
        for (Animal a : playerUnits) {
            if (a.isDefeated()) {
                enemyResources.addMoney(a.getRewardMoney());
                enemyResources.gainExp(a.getRewardExp());
            }
        }
        playerUnits.removeIf(Animal::isDefeated);

        // Enemy defeated -> reward player
        for (Animal a : enemyUnits) {
            if (a.isDefeated()) {
                playerResources.addMoney(a.getRewardMoney());
                playerResources.gainExp(a.getRewardExp());
            }
        }
        enemyUnits.removeIf(Animal::isDefeated);
    }

    // Getters
    public List<Animal> getPlayerAnimals() {
        return new ArrayList<>(playerUnits);
    }

    public List<Animal> getEnemyAnimals() {
        return new ArrayList<>(enemyUnits);
    }

    public Base getPlayerBase() {
        return playerBase;
    }

    public Base getEnemyBase() {
        return enemyBase;
    }

    public Animal getPlayerFront() {
        return playerUnits.isEmpty() ? null : playerUnits.get(0);
    }

    public Animal getEnemyFront() {
        return enemyUnits.isEmpty() ? null : enemyUnits.get(0);
    }

    public List<TurretShot> getTurretShotsThisUpdate() {
        return new ArrayList<>(turretShotsThisUpdate);
    }

    public boolean isPlayerDefeated() {
        return playerUnits.isEmpty();
    }

    public boolean isEnemyDefeated() {
        return enemyUnits.isEmpty();
    }

    public static final class TurretShot {
        private final boolean playerSide;
        private final double targetPosition;

        private TurretShot(boolean playerSide, double targetPosition) {
            this.playerSide = playerSide;
            this.targetPosition = targetPosition;
        }

        public boolean isPlayerSide() { return playerSide; }
        public double getTargetPosition() { return targetPosition; }
    }
}
