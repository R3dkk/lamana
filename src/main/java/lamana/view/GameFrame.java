package lamana.view;

import lamana.ai.EnemyDifficulty;
import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.factory.UnitFactory;
import lamana.model.animal.Animal;
import lamana.model.building.Base;

import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/** Single-window battle screen for Animal War. */
public class GameFrame extends JFrame {
    private static final Color BACKGROUND = new Color(17, 25, 28);
    private static final Color PANEL = new Color(22, 32, 34);
    private static final EnemyDifficulty[] DIFFICULTIES = {
            EnemyDifficulty.EASY, EnemyDifficulty.NORMAL, EnemyDifficulty.HARD
    };

    private GameController game;
    private final HUDPanel hudPanel;
    private final SpawnQueuePanel spawnQueuePanel;
    private final GamePanel gamePanel;
    private final List<UnitCard> unitCards = new ArrayList<>();
    private final JButton upgradeButton;
    private final JButton pauseButton;
    private final JButton restartButton;
    private final JButton difficultyButton;
    private final JPanel cardsGrid;
    private Timer gameTimer;
    private boolean paused;
    private int difficultyIndex = 1;
    private long lastFrameNanos = System.nanoTime();
    private boolean fastTestingMode;

    public GameFrame() {
        super("Animal War");
        game = new GameController();
        hudPanel = new HUDPanel();
        spawnQueuePanel = new SpawnQueuePanel();
        gamePanel = new GamePanel(game);
        upgradeButton = makeIconButton(UiIcon.Type.UPGRADE, "Upgrade markas");
        pauseButton = makeIconButton(UiIcon.Type.PAUSE, "Jeda");
        restartButton = makeIconButton(UiIcon.Type.RESTART, "Mulai ulang");
        difficultyButton = makeIconButton(UiIcon.Type.DIFFICULTY, "Kesulitan: Normal · klik untuk ubah");
        cardsGrid = new JPanel(new GridLayout(1, 3, 6, 0));

        setupUI();
        installTestingShortcuts();
        updateUnitCards();
        applyDifficulty();
        startGameLoop();
        refresh();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BACKGROUND);
        setContentPane(root);

        JPanel top = new JPanel(new BorderLayout(0, 0));
        top.setBackground(BACKGROUND);
        top.add(hudPanel, BorderLayout.CENTER);
        top.add(spawnQueuePanel, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);
        root.add(gamePanel, BorderLayout.CENTER);
        root.add(makeCommandPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 620));
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
        JPanel command = new JPanel(new BorderLayout(10, 0));
        command.setBackground(PANEL);
        command.setBorder(BorderFactory.createEmptyBorder(9, 14, 10, 14));

        cardsGrid.setOpaque(false);
        command.add(cardsGrid, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.setOpaque(false);
        upgradeButton.addActionListener(e -> {
            if (game.upgradePlayerBase()) updateUnitCards();
            refresh();
        });
        pauseButton.addActionListener(e -> togglePause());
        restartButton.addActionListener(e -> restartGame());
        difficultyButton.addActionListener(e -> cycleDifficulty());
        controls.add(upgradeButton);
        controls.add(difficultyButton);
        controls.add(pauseButton);
        controls.add(restartButton);
        command.add(controls, BorderLayout.EAST);
        return command;
    }

    private static JButton makeIconButton(UiIcon.Type type, String tooltip) {
        JButton button = new JButton(new UiIcon(type, new Color(207, 219, 207)));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(new Color(34, 47, 47));
        button.setBorder(BorderFactory.createLineBorder(new Color(62, 80, 75)));
        button.setPreferredSize(new Dimension(36, 36));
        button.setName(tooltip);
        button.getAccessibleContext().setAccessibleName(tooltip);
        return button;
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
            long deltaMs = Math.min(GameConfig.MAX_FRAME_DELTA_MS,
                    Math.max(0, (now - lastFrameNanos) / 1_000_000L));
            lastFrameNanos = now;
            if (!paused) {
                long simulationDeltaMs = Math.max(0,
                        Math.round(deltaMs * GameConfig.GAME_SPEED_MULTIPLIER
                                * (fastTestingMode ? 8.0 : 1.0)));
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
        pauseButton.setIcon(new UiIcon(paused ? UiIcon.Type.PLAY : UiIcon.Type.PAUSE,
                new Color(207, 219, 207)));
        pauseButton.setToolTipText(paused ? "Lanjutkan" : "Jeda");
        pauseButton.getAccessibleContext().setAccessibleName(paused ? "Lanjutkan" : "Jeda");
        gamePanel.setPaused(paused);
    }

    private void restartGame() {
        paused = false;
        fastTestingMode = false;
        game = new GameController();
        gamePanel.setGame(game);
        updateTestingTooltip();
        applyDifficulty();
        setPauseIcon(false);
        updateUnitCards();
        refresh();
    }

    private void installTestingShortcuts() {
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F2"), "grant-testing-resources");
        root.getActionMap().put("grant-testing-resources", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                if (game.grantTestingResources()) refresh();
            }
        });
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F3"), "toggle-testing-speed");
        root.getActionMap().put("toggle-testing-speed", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                if (game.getState() != GameState.PLAYING) return;
                fastTestingMode = !fastTestingMode;
                updateTestingTooltip();
            }
        });
        updateTestingTooltip();
    }

    private void updateTestingTooltip() {
        String speed = fastTestingMode ? "aktif (8x)" : "nonaktif";
        gamePanel.setToolTipText("Testing: F2 = +10.000 uang dan EXP · F3 = kecepatan tinggi " + speed);
    }

    private void cycleDifficulty() {
        difficultyIndex = (difficultyIndex + 1) % DIFFICULTIES.length;
        applyDifficulty();
    }

    private void applyDifficulty() {
        EnemyDifficulty difficulty = DIFFICULTIES[difficultyIndex];
        game.getEnemyAI().setDifficulty(difficulty);
        String tooltip = "Kesulitan: " + difficulty.name().charAt(0)
                + difficulty.name().substring(1).toLowerCase() + " · klik untuk ubah";
        difficultyButton.setIcon(new UiIcon(UiIcon.Type.DIFFICULTY,
                new Color(207, 219, 207), difficultyIndex + 1, 18));
        difficultyButton.setToolTipText(tooltip);
        difficultyButton.getAccessibleContext().setAccessibleName(tooltip);
    }

    private void setPauseIcon(boolean isPaused) {
        pauseButton.setIcon(new UiIcon(isPaused ? UiIcon.Type.PLAY : UiIcon.Type.PAUSE,
                new Color(207, 219, 207)));
        pauseButton.setToolTipText(isPaused ? "Lanjutkan" : "Jeda");
        pauseButton.getAccessibleContext().setAccessibleName(isPaused ? "Lanjutkan" : "Jeda");
    }

    private void refresh() {
        hudPanel.refresh(game);
        spawnQueuePanel.refresh(game);
        for (UnitCard card : unitCards) card.refresh(game);
        Base player = game.getPlayerBase();
        int expNeeded = player.getExpNeededForNextLevel();
        if (expNeeded == 0) {
            upgradeButton.setToolTipText("Upgrade markas · Era maksimum");
            upgradeButton.setEnabled(false);
        } else {
            upgradeButton.setToolTipText(String.format("Upgrade markas ke Era %d · butuh %d EXP",
                    player.getLevel() + 1, expNeeded));
            upgradeButton.setEnabled(game.getState() == GameState.PLAYING
                    && game.getPlayerResources().getExp() >= expNeeded);
        }
        pauseButton.setEnabled(game.getState() == GameState.PLAYING);
        restartButton.setEnabled(true);
        if (game.getState() != GameState.PLAYING && paused) {
            paused = false;
            setPauseIcon(false);
            gamePanel.setPaused(false);
        }
    }
}
