package lamana.model.animal.melee;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;

/**
 * Godzilla — hewan melee level 3.
 * Special: Atomic Breath (AoE radius 2.0).
 * POLYMORPHISM: override attack() dengan efek splash damage AoE.
 */
public class Godzilla extends MeleeAnimal implements SpecialAbility {

    public Godzilla() {
        super("Godzilla", 550, 80, 2, 2, 250, 60, 35, 3);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getEffectiveRange()) return false;

        // Serang target utama
        target.takeDamage(getDamage());

        // AoE radius 2.0
        if (context != null && target instanceof Animal) {
            double targetPos = ((Animal) target).getPosition();
            for (Animal enemy : context.getAllEnemiesInLane()) {
                if (enemy != null && enemy != target && !enemy.isDefeated()) {
                    if (Math.abs(enemy.getPosition() - targetPos) <= 2.0) {
                        enemy.takeDamage(getDamage());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getAbilityName() {
        return "Atomic Breath (AoE radius 2.0)";
    }
}
