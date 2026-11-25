package lamana.view;

import lamana.ai.EnemyDifficulty;
import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.factory.UnitFactory;
import lamana.model.animal.Animal;
import lamana.model.building.Base;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/** Main battle screen for Animal War. */
public class GameFrame extends JFrame {
    private static final Color BACKGROUND = new Color(12, 25, 36);
    private static final Color PANEL = new Color(17, 32, 45);

    private GameController game;
    private final HUDPanel hudPanel;
    private final SpawnQueuePanel spawnQueuePanel;
    private final GamePanel gamePanel;
    private final List<UnitCard> unitCards = new ArrayList<>();
    private final JButton upgradeButton;
    private final JButton pauseButton;
    private final JButton restartButton;
    private final JComboBox<String> difficultyBox;
    private final JPanel cardsGrid;
    private Timer gameTimer;
    private boolean paused;
    private long lastFrameNanos = System.nanoTime();

    public GameFrame() {
        super("Animal War | Nusantara Frontline");
        game = new GameController();
        hudPanel = new HUDPanel();
        spawnQueuePanel = new SpawnQueuePanel();
        gamePanel = new GamePanel(game);
        upgradeButton = new JButton();
        pauseButton = new JButton("Jeda");
        restartButton = new JButton("Mulai ulang");
        difficultyBox = new JComboBox<>(new String[]{"Easy", "Normal", "Hard"});
        cardsGrid = new JPanel(new GridLayout(1, 3, 10, 0));

        setupUI();
        updateUnitCards();
        startGameLoop();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BACKGROUND);
        setContentPane(root);

        JPanel top = new JPanel(new BorderLayout(0, 2));
        top.setBackground(BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        top.add(hudPanel, BorderLayout.CENTER);
        top.add(spawnQueuePanel, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);
        root.add(gamePanel, BorderLayout.CENTER);
        root.add(makeCommandPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(1030, 720));
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (gameTimer != null) gameTimer.stop();
            }
        });
    }

    private JPanel makeCommandPanel() {
        JPanel command = new JPanel(new BorderLayout(0, 8));
        command.setBackground(PANEL);
        command.setBorder(BorderFactory.createEmptyBorder(10, 18, 14, 18));

        JPanel rosterHeader = new JPanel(new BorderLayout());
        rosterHeader.setOpaque(false);
        JLabel title = new JLabel("PASUKAN ERA SAAT INI");
        title.setForeground(new Color(235, 241, 230));
        title.setFont(new Font("Dialog", Font.BOLD, 11));
        JLabel tip = new JLabel("Biaya, statistik, dan kemampuan khusus", SwingConstants.RIGHT);
        tip.setForeground(new Color(139, 166, 175));
        tip.setFont(new Font("Dialog", Font.PLAIN, 11));
        rosterHeader.add(title, BorderLayout.WEST);
        rosterHeader.add(tip, BorderLayout.EAST);
        command.add(rosterHeader, BorderLayout.NORTH);

        cardsGrid.setOpaque(false);
        command.add(cardsGrid, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        styleActionButton(upgradeButton, new Color(42, 110, 84), Color.WHITE);
        upgradeButton.setHorizontalAlignment(SwingConstants.LEFT);
        upgradeButton.setPreferredSize(new Dimension(280, 38));
        upgradeButton.addActionListener(e -> {
            if (game.upgradePlayerBase()) updateUnitCards();
            refresh();
        });
        controls.add(upgradeButton, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);
        JLabel difficultyLabel = new JLabel("KESULITAN AI");
        difficultyLabel.setForeground(new Color(155, 183, 188));
        difficultyLabel.setFont(new Font("Dialog", Font.BOLD, 9));
        difficultyBox.setSelectedItem("Normal");
        difficultyBox.addActionListener(e -> applyDifficulty());
        pauseButton.addActionListener(e -> togglePause());
        restartButton.addActionListener(e -> restartGame());
        styleActionButton(pauseButton, new Color(42, 65, 78), Color.WHITE);
        styleActionButton(restartButton, new Color(42, 65, 78), Color.WHITE);
        pauseButton.setPreferredSize(new Dimension(104, 36));
        restartButton.setPreferredSize(new Dimension(126, 36));
        rightControls.add(difficultyLabel);
        rightControls.add(difficultyBox);
        rightControls.add(pauseButton);
        rightControls.add(restartButton);
        controls.add(rightControls, BorderLayout.EAST);
        command.add(controls, BorderLayout.SOUTH);
        return command;
    }

    private static void styleActionButton(JButton button, Color background, Color foreground) {
        button.setFont(new Font("Dialog", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(87, 132, 128)),
                BorderFactory.createEmptyBorder(5, 11, 5, 11)));
    }

    private void updateUnitCards() {
        cardsGrid.removeAll();
        unitCards.clear();
        List<String> types = UnitFactory.getTypesForLevel(game.getPlayerBase().getLevel());
        for (String type : types) {
            Animal sample = UnitFactory.create(type);
            UnitCard card = new UnitCard(type, sample);
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
        lastFrameNanos = System.nanoTime();
        gameTimer = new Timer(GameConfig.GAME_LOOP_MS, e -> {
            long now = System.nanoTime();
            long deltaMs = Math.max(0, (now - lastFrameNanos) / 1_000_000L);
            lastFrameNanos = now;
            if (!paused) {
                long simulationDeltaMs = Math.max(0,
                        Math.round(deltaMs * GameConfig.GAME_SPEED_MULTIPLIER));
                game.update(simulationDeltaMs);
                gamePanel.advanceAnimation(simulationDeltaMs);
            }
            refresh();
            gamePanel.repaint();
        });
        gameTimer.start();
    }

    private void togglePause() {
        if (game.getState() != GameState.PLAYING) return;
        paused = !paused;
        pauseButton.setText(paused ? "Lanjutkan" : "Jeda");
        gamePanel.setPaused(paused);
    }

    private void restartGame() {
        paused = false;
        game = new GameController();
        gamePanel.setGame(game);
        applyDifficulty();
        pauseButton.setText("Jeda");
        updateUnitCards();
        refresh();
    }

    private void applyDifficulty() {
        Object selected = difficultyBox.getSelectedItem();
        if (selected != null) {
            game.getEnemyAI().setDifficulty(EnemyDifficulty.valueOf(selected.toString().toUpperCase()));
        }
    }

    private void refresh() {
        hudPanel.refresh(game);
        spawnQueuePanel.refresh(game);
        for (UnitCard card : unitCards) card.refresh(game);
        Base player = game.getPlayerBase();
        int expNeeded = player.getExpNeededForNextLevel();
        if (expNeeded == 0) {
            upgradeButton.setText("BASE MAKSIMUM  ·  ERA 3");
            upgradeButton.setEnabled(false);
        } else {
            upgradeButton.setText(String.format("UPGRADE KE ERA %d  ·  BUTUH %d EXP",
                    player.getLevel() + 1, expNeeded));
            upgradeButton.setEnabled(game.getState() == GameState.PLAYING
                    && game.getPlayerResources().getExp() >= expNeeded);
        }
        pauseButton.setEnabled(game.getState() == GameState.PLAYING);
        restartButton.setEnabled(true);
        if (game.getState() != GameState.PLAYING && paused) {
            paused = false;
            pauseButton.setText("Jeda");
            gamePanel.setPaused(false);
        }
    }
}
