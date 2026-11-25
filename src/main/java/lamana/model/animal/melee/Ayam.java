package lamana.model.animal.melee;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.AttackContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ayam — hewan melee level 1.
 * Special: 20% chance Double Strike (serangan 2x).
 * POLYMORPHISM: override attack() dengan perilaku unik.
 */
public class Ayam extends MeleeAnimal implements SpecialAbility {

    public Ayam() {
        super("Ayam", 100, 18, 2, 30, 15, 10, 1);
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        if (distance < 0 || distance > getRange()) return false;

        target.takeDamage(getDamage());

        // 20% chance Double Strike
        if (ThreadLocalRandom.current().nextDouble() < 0.20) {
            if (!target.isDefeated()) {
                target.takeDamage(getDamage());
            }
        }
        return true;
    }

    @Override
    public String getAbilityName() {
        return "Double Strike (20%)";
    }
}
