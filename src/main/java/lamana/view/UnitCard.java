package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameState;
import lamana.factory.UnitFactory;
import lamana.model.animal.Animal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Locale;

/** Compact unit button: sprite and cost, with details available on hover. */
public class UnitCard extends JButton {
    private static final Color CARD = new Color(29, 41, 44);
    private final String unitType;
    private final String displayName;
    private final int cost;

    public UnitCard(String unitType, Animal sample) {
        this.unitType = unitType;
        this.displayName = sample.getName();
        this.cost = sample.getCost();
        setText(Integer.toString(cost));
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(BOTTOM);
        setIconTextGap(0);
        setFont(new Font("Dialog", Font.BOLD, 11));
        setForeground(new Color(230, 196, 117));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        setToolTipText(makeTooltip(sample));
        setPreferredSize(new Dimension(86, 84));
        setIcon(new UnitBadge(sample));
        getAccessibleContext().setAccessibleName(displayName + ", " + cost + " gold");
    }

    private static String makeTooltip(Animal sample) {
        String role = sample.getClass().getSuperclass().getSimpleName();
        if (role.endsWith("Animal")) role = role.substring(0, role.length() - 6);
        return String.format(Locale.ROOT, "%s · %s · %d gold · HP %d · ATK %d · Range %d · %s",
                sample.getName(), role, sample.getCost(), sample.getMaxHp(), sample.getDamage(),
                sample.getEffectiveRange(), GameArt.abilityFor(sample));
    }

    public String getUnitType() { return unitType; }
    public int getCost() { return cost; }

    public void refresh(GameController game) {
        boolean canSpawn = game != null && game.getState() == GameState.PLAYING
                && game.canSpawnPlayerAnimal(unitType);
        setEnabled(canSpawn);
        setToolTipText(makeTooltip(UnitFactory.create(unitType)));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = isEnabled() ? CARD : new Color(26, 34, 36);
        if (getModel().isRollover() && isEnabled()) fill = new Color(42, 57, 55);
        g.setColor(fill);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(isEnabled() ? new Color(77, 100, 91) : new Color(51, 62, 61));
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.dispose();
        setForeground(isEnabled() ? new Color(230, 196, 117) : new Color(119, 124, 116));
        super.paintComponent(graphics);
    }

    private static final class UnitBadge implements javax.swing.Icon {
        private final Animal sample;

        UnitBadge(Animal sample) { this.sample = sample; }
        @Override public int getIconWidth() { return 58; }
        @Override public int getIconHeight() { return 58; }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            GameArt.drawCardSprite(g, sample, x, y, 58, 58);
            g.dispose();
        }
    }
}
