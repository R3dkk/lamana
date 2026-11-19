package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameState;
import lamana.model.animal.Animal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Locale;

/** A deploy card showing a unit's artwork, cost, stats, and special ability. */
public class UnitCard extends JButton {
    private static final Color CARD = new Color(23, 39, 53);
    private final String unitType;
    private final String displayName;
    private final int cost;

    public UnitCard(String unitType, Animal sample) {
        this.unitType = unitType;
        this.displayName = sample.getName();
        this.cost = sample.getCost();
        setText(makeDescription(sample));
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setIconTextGap(13);
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 13, 10, 12));
        setToolTipText("Masukkan " + displayName + " ke antrean spawn");
        setPreferredSize(new java.awt.Dimension(310, 126));
        setIcon(new UnitBadge(sample));
    }

    private String makeDescription(Animal sample) {
        String role = sample.getClass().getSuperclass().getSimpleName();
        if (role.endsWith("Animal")) role = role.substring(0, role.length() - 6);
        String ability = GameArt.abilityFor(sample);
        String color = isEnabled() ? "#F4F7F5" : "#8F9CA4";
        return String.format(Locale.ROOT,
                "<html><div style='width:210px;color:%s'><b style='font-size:15px'>%s</b>"
                        + "<span style='color:#80D9C2'> &nbsp; %s</span>"
                        + "<br><span style='color:#B9C8CF'>HP %d &nbsp;·&nbsp; ATK %d &nbsp;·&nbsp; RANGE %d</span>"
                        + "<br><span style='color:#F4C66A;font-weight:bold'>%s gold</span>"
                        + "<br><span style='color:#94B5C5'>%s</span></div></html>",
                color, sample.getName(), role, sample.getMaxHp(), sample.getDamage(),
                sample.getEffectiveRange(), sample.getCost(), ability);
    }

    public String getUnitType() { return unitType; }
    public int getCost() { return cost; }

    public void refresh(GameController game) {
        boolean canSpawn = game != null && game.getState() == GameState.PLAYING
                && game.canSpawnPlayerAnimal(unitType);
        if (isEnabled() != canSpawn) {
            setEnabled(canSpawn);
            // The HTML uses a softer description while the button is unavailable.
            setText(makeDescription(lamana.factory.UnitFactory.create(unitType)));
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = isEnabled() ? CARD : new Color(20, 31, 42);
        if (getModel().isRollover() && isEnabled()) fill = new Color(32, 57, 69);
        g.setColor(fill);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g.setColor(isEnabled() ? new Color(66, 101, 110) : new Color(47, 63, 72));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g.dispose();
        super.paintComponent(graphics);
    }

    private static final class UnitBadge implements javax.swing.Icon {
        private final Animal sample;

        UnitBadge(Animal sample) { this.sample = sample; }
        @Override public int getIconWidth() { return 82; }
        @Override public int getIconHeight() { return 82; }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            GameArt.drawCardSprite(g, sample, x, y, 82, 82);
            g.dispose();
        }
    }
}
