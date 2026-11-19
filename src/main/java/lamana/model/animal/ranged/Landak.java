package lamana.model.animal.ranged;

import lamana.contract.Attackable;
import lamana.contract.SpecialAbility;
import lamana.model.animal.AttackContext;

/**
 * Landak — hewan ranged level 1.
 * Special: Quill Range +1 (jangkauan efektif +1 melebihi range normal).
 * POLYMORPHISM: override attack() dengan perilaku unik.
 */
public class Landak extends RangedAnimal implements SpecialAbility {

    public Landak() {
        super("Landak", 90, 15, 3, 2, 40, 12, 12, 1);
    }

    @Override
    public int getEffectiveRange() {
        return getRange() + 1;
    }

    @Override
    public boolean attack(Attackable target, double distance, AttackContext context) {
        if (target == null || target.isDefeated() || isDefeated()) return false;
        // Quill Range +1: cek jangkauan efektif = getEffectiveRange()
        if (distance < 0 || distance > getEffectiveRange()) return false;

        target.takeDamage(getDamage());
        return true;
    }

    @Override
    public String getAbilityName() {
        return "Quill Range +1";
    }
}
