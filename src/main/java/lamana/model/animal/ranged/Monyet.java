package lamana.model.animal.ranged;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;

/**
 * Monyet — hewan ranged level 2.
 * Special: Banana Splash — serang target utama + enemy kedua di belakang dengan damage/2.
 * POLYMORPHISM: override attack() dengan perilaku unik (AoE chaining).
 */
public class Monyet extends RangedAnimal implements SpecialAbility {

    public Monyet() {
        super("Monyet", 113, 21, 5, 2, 100, 35, 22, 2);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getRange()) return false;

        // Serang target utama
        target.takeDamage(getDamage());

        // Banana Splash: cari enemy kedua di belakang target
        if (context != null && target instanceof Animal) {
            Animal primaryTarget = (Animal) target;
            double primaryPos = primaryTarget.getPosition();
            Animal secondTarget = null;
            double bestDist = Double.MAX_VALUE;

            for (Animal enemy : context.getAllEnemiesInLane()) {
                if (enemy == null || enemy == primaryTarget || enemy.isDefeated()) continue;

                double ePos = enemy.getPosition();
                // "Di belakang target" — jika attacker player: enemy posisi > target posisi
                // jika attacker enemy: enemy posisi < target posisi
                boolean isBehind;
                if (context.isAttackerPlayer()) {
                    isBehind = ePos > primaryPos;
                } else {
                    isBehind = ePos < primaryPos;
                }

                if (isBehind) {
                    double d = Math.abs(ePos - primaryPos);
                    if (d < bestDist) {
                        bestDist = d;
                        secondTarget = enemy;
                    }
                }
            }

            if (secondTarget != null && !secondTarget.isDefeated()) {
                secondTarget.takeDamage(Math.max(1, getDamage() / 2));
            }
        }

        return true;
    }

    @Override
    public String getAbilityName() {
        return "Banana Splash (AoE)";
    }
}
