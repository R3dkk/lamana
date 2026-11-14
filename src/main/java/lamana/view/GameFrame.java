package lamana.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import lamana.ai.EnemyDifficulty;
import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.factory.UnitFactory;
import lamana.model.animal.Animal;
import lamana.model.building.Base;

public class GameFrame extends JFrame {
    private final GameController game;
    private final HUDPanel hudPanel;
    private final GamePanel gamePanel;
    private final List<UnitCard> unitCards = new ArrayList<>();
    private final JButton upgradeButton;
    private final JPanel cardsGrid;
    private long lastFrameNanos = System.nanoTime();

    public GameFrame() {
        super("Animal War — 2D Strategy Battle Game (OOP Final Project)");
        this.game = new GameController();
        this.hudPanel = new HUDPanel();
        this.gamePanel = new GamePanel(game);
        this.upgradeButton = new JButton("Upgrade Base");
        this.cardsGrid = new JPanel(new GridLayout(1, 3, 6, 6));

        setupUI();
        updateUnitCards(); // Panggil pertama kali saat startup
        startGameLoop();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        add(hudPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(6, 6));
        southPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        southPanel.setBackground(new Color(30, 30, 45));

        cardsGrid.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 110)),
                "SPAWN UNIT (ERA SAAT INI)",
                0, 0, new Font("Arial", Font.BOLD, 11), Color.WHITE));
        cardsGrid.setBackground(new Color(30, 30, 45));
        southPanel.add(cardsGrid, BorderLayout.CENTER);

        // Panel Bawah: Upgrade + Difficulty selector
        JPanel bottomControls = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomControls.setBackground(new Color(30, 30, 45));

        upgradeButton.setFont(new Font("Arial", Font.BOLD, 12));
        upgradeButton.setBackground(new Color(40, 140, 60));
        upgradeButton.setForeground(Color.WHITE);
        upgradeButton.setFocusPainted(false);
        upgradeButton.addActionListener(e -> {
            if (game.upgradePlayerBase()) {
                updateUnitCards(); // Refresh tombol saat naik level
            }
            refresh();
        });
        bottomControls.add(upgradeButton);

        // ComboBox Difficulty
        String[] diffs = {"Easy", "Normal", "Hard"};
        JComboBox<String> diffBox = new JComboBox<>(diffs);
        diffBox.setSelectedItem("Normal");
        diffBox.addActionListener(e -> {
            String selected = (String) diffBox.getSelectedItem();
            if (selected != null) {
                game.getEnemyAI().setDifficulty(EnemyDifficulty.valueOf(selected.toUpperCase()));
            }
        });

        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        diffPanel.setBackground(new Color(30, 30, 45));
        JLabel diffLabel = new JLabel("Enemy Difficulty: ");
        diffLabel.setForeground(Color.WHITE);
        diffLabel.setFont(new Font("Arial", Font.BOLD, 12));
        diffPanel.add(diffLabel);
        diffPanel.add(diffBox);
        bottomControls.add(diffPanel);

        southPanel.add(bottomControls, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateUnitCards() {
        cardsGrid.removeAll();
        unitCards.clear();
        
        int level = game.getPlayerBase().getLevel();
        
        // Ambil daftar hewan langsung dari UnitFactory (Single Source of Truth)
        List<String> types = UnitFactory.getTypesForLevel(level);

        for (String type : types) {
            Animal sample = UnitFactory.create(type);
            
            // Tentukan Role secara dinamis
            String role = "Melee";
            if (sample.getClass().getName().contains("Ranged")) role = "Ranged";
            else if (sample.getClass().getName().contains("Heavy")) role = "Heavy";

            String label = sample.getName() + " [" + role + "]";
            int cost = sample.getCost();

            UnitCard card = new UnitCard(type, label, cost);
            card.addActionListener(e -> {
                game.spawnPlayerAnimal(type);
                refresh();
                gamePanel.repaint();
            });
            unitCards.add(card);
            cardsGrid.add(card);
        }
        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private void startGameLoop() {
        Timer timer = new Timer(GameConfig.GAME_LOOP_MS, e -> {
            long now = System.nanoTime();
            long deltaMs = Math.max(0, (now - lastFrameNanos) / 1_000_000L);
            lastFrameNanos = now;
            game.update(deltaMs);
            refresh();
            gamePanel.repaint();
        });
        timer.start();
    }

    private void refresh() {
        hudPanel.refresh(game);
        for (UnitCard card : unitCards) {
            card.refresh(game);
        }
        Base pb = game.getPlayerBase();
        int expNeeded = pb.getExpNeededForNextLevel();
        if (expNeeded == 0) {
            upgradeButton.setText("Base Maksimum (Level 3)");
            upgradeButton.setEnabled(false);
        } else {
            upgradeButton.setText(String.format("UPGRADE KE ERA %d (Butuh %d EXP)", pb.getLevel() + 1, expNeeded));
            upgradeButton.setEnabled(game.getState() == GameState.PLAYING && game.getPlayerResources().getExp() >= expNeeded);
        }
    }
}