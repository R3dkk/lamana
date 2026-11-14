package lamana.model.animal.heavy;

import lamana.contract.SpecialAbility;

/**
 * KuraKura — hewan heavy level 1.
 * Special: Hard Shell — reduksi damage masuk sebesar 20%.
 * POLYMORPHISM: override takeDamage() bukan attack() — perilaku defensif unik.
 */
public class KuraKura extends HeavyAnimal implements SpecialAbility {

    public KuraKura() {
        super("Kura-kura", 200, 15, 2, 50, 18, 15, 1);
    }

    /**
     * Hard Shell: kurangi damage masuk 20% (efektif 80% dari damage asli).
     * Minimum 1 agar selalu ada damage.
     */
    @Override
    public void takeDamage(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Damage harus > 0.");
        int effectiveDmg = Math.max(1, (int)(amount * 0.80));
        super.takeDamage(effectiveDmg);
    }

    @Override
    public String getAbilityName() {
        return "Hard Shell (dmg -20%)";
    }
}
