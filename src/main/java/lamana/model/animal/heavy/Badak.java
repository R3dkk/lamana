package lamana.model.animal.heavy;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.core.GameConfig;
import lamana.model.animal.Animal;
import lamana.model.animal.AttackContext;

/**
 * Badak — hewan heavy level 2.
 * Special: Horn Knockback — setelah menyerang, mendorong target 1.0 unit mundur.
 * POLYMORPHISM: override attack() dengan efek knockback unik.
 */
public class Badak extends HeavyAnimal implements SpecialAbility {

    public Badak() {
        super("Badak", 330, 15, 2, 120, 45, 28, 2);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getRange()) return false;

        // Serang target
        target.takeDamage(getDamage());

        // Knockback: dorong target 1.0 unit mundur menjauh dari penyerang
        if (target instanceof Animal) {
            Animal animalTarget = (Animal) target;
            double currentPos = animalTarget.getPosition();
            double direction = (context != null && context.isAttackerPlayer()) ? 1.0 : -1.0;
            double newPos = Math.max(0.0, Math.min(GameConfig.LANE_LENGTH, currentPos + direction));
            animalTarget.setPosition(newPos);
        }

        return true;
    }

    @Override
    public String getAbilityName() {
        return "Horn Knockback (-1 slot)";
    }
}
