package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.model.animal.Animal;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/** Icon-only unit queue with a thin spawn progress strip. */
public class SpawnQueuePanel extends JPanel {
    private final List<JLabel> slots = new ArrayList<>();
    private final JProgressBar cooldownBar;

    public SpawnQueuePanel() {
        super(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 14, 8, 14));
        setPreferredSize(new Dimension(900, 36));

        JPanel slotRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        slotRow.setOpaque(false);
        for (int i = 0; i < GameConfig.PLAYER_SPAWN_QUEUE_CAPACITY; i++) {
            JLabel slot = new JLabel();
            slot.setPreferredSize(new Dimension(30, 26));
            slot.setHorizontalAlignment(JLabel.CENTER);
            slot.setToolTipText("Slot antrean kosong");
            slots.add(slot);
            slotRow.add(slot);
        }
        add(slotRow, BorderLayout.CENTER);

        cooldownBar = new JProgressBar(0, 1000);
        cooldownBar.setBorderPainted(false);
        cooldownBar.setStringPainted(false);
        cooldownBar.setForeground(new Color(114, 191, 154));
        cooldownBar.setBackground(new Color(70, 86, 85));
        cooldownBar.setPreferredSize(new Dimension(120, 4));
        cooldownBar.setToolTipText("Progres unit berikutnya");
        add(cooldownBar, BorderLayout.EAST);
    }

    public void refresh(GameController game) {
        List<Animal> queue = game.getPlayerSpawnQueue();
        for (int i = 0; i < slots.size(); i++) {
            JLabel slot = slots.get(i);
            if (i < queue.size()) {
                Animal animal = queue.get(i);
                slot.setIcon(new QueueIcon(animal));
                slot.setToolTipText((i == 0 ? "Berikutnya: " : "Antrean: ") + animal.getName());
            } else {
                slot.setIcon(null);
                slot.setToolTipText("Slot antrean kosong");
            }
        }

        cooldownBar.setValue(queue.isEmpty() ? 0
                : (int) Math.round(game.getPlayerSpawnCooldownProgress() * 1000));
    }

    private static final class QueueIcon implements javax.swing.Icon {
        private final Animal animal;

        QueueIcon(Animal animal) { this.animal = animal; }
        @Override public int getIconWidth() { return 26; }
        @Override public int getIconHeight() { return 26; }

        @Override
        public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int x, int y) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            GameArt.drawCardSprite(g, animal, x, y, 26, 26);
            g.dispose();
        }
    }
}
