package lamana.model.animal.melee;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.AttackContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Harimau — hewan melee level 2.
 * Special: 30% chance Critical Claws (damage * 2).
 * POLYMORPHISM: override attack() dengan perilaku unik.
 */
public class Harimau extends MeleeAnimal implements SpecialAbility {

    public Harimau() {
        super("Harimau", 180, 30, 2, 100, 30, 20, 2);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getRange()) return false;

        int dmg = getDamage();
        // 30% chance critical: damage * 2
        if (ThreadLocalRandom.current().nextDouble() < 0.30) {
            dmg = dmg * 2;
        }
        target.takeDamage(dmg);
        return true;
    }

    @Override
    public String getAbilityName() {
        return "Critical Claws (30%)";
    }
}
