package lamana.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.model.animal.Animal;

/** Shows queued units and progress until the next unit is deployed. */
public class SpawnQueuePanel extends JPanel {
    private static final Color BACKGROUND = new Color(30, 30, 45);
    private static final Color EMPTY_SLOT = new Color(55, 55, 72);
    private static final Color FILLED_SLOT = new Color(125, 97, 31);

    private final List<JLabel> slots = new ArrayList<>();
    private final JProgressBar cooldownBar;

    public SpawnQueuePanel() {
        super(new BorderLayout(10, 0));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(5, 16, 6, 16));
        setPreferredSize(new Dimension(900, 52));

        JLabel title = new JLabel("ANTREAN SPAWN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 12));
        add(title, BorderLayout.WEST);

        JPanel slotRow = new JPanel(new GridLayout(1, GameConfig.PLAYER_SPAWN_QUEUE_CAPACITY, 4, 0));
        slotRow.setOpaque(false);
        for (int i = 0; i < GameConfig.PLAYER_SPAWN_QUEUE_CAPACITY; i++) {
            JLabel slot = new JLabel("—", SwingConstants.CENTER);
            slot.setPreferredSize(new Dimension(44, 32));
            slot.setOpaque(true);
            slot.setBackground(EMPTY_SLOT);
            slot.setForeground(Color.LIGHT_GRAY);
            slot.setFont(new Font("Arial", Font.BOLD, 11));
            slot.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120)));
            slot.setToolTipText("Slot antrean " + (i + 1) + " — kosong");
            slots.add(slot);
            slotRow.add(slot);
        }
        add(slotRow, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout(0, 2));
        progressPanel.setOpaque(false);
        progressPanel.setPreferredSize(new Dimension(145, 38));
        JLabel cooldownLabel = new JLabel("PROGRES SPAWN", SwingConstants.CENTER);
        cooldownLabel.setForeground(new Color(205, 205, 215));
        cooldownLabel.setFont(new Font("Arial", Font.BOLD, 9));
        progressPanel.add(cooldownLabel, BorderLayout.NORTH);

        cooldownBar = new JProgressBar(0, 1000);
        cooldownBar.setStringPainted(false);
        cooldownBar.setForeground(new Color(50, 160, 220));
        cooldownBar.setBackground(new Color(55, 55, 72));
        progressPanel.add(cooldownBar, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.EAST);
    }

    public void refresh(GameController game) {
        List<Animal> queue = game.getPlayerSpawnQueue();
        for (int i = 0; i < slots.size(); i++) {
            JLabel slot = slots.get(i);
            if (i < queue.size()) {
                Animal animal = queue.get(i);
                String name = animal.getName();
                slot.setText(name.length() > 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase());
                slot.setBackground(FILLED_SLOT);
                slot.setForeground(Color.WHITE);
                slot.setToolTipText((i == 0 ? "Berikutnya: " : "Antrean: ") + name);
            } else {
                slot.setText("—");
                slot.setBackground(EMPTY_SLOT);
                slot.setForeground(Color.LIGHT_GRAY);
                slot.setToolTipText("Slot antrean " + (i + 1) + " — kosong");
            }
        }

        if (queue.isEmpty()) {
            cooldownBar.setValue(0);
        } else {
            cooldownBar.setValue((int) Math.round(game.getPlayerSpawnCooldownProgress() * 1000));
        }
    }
}
