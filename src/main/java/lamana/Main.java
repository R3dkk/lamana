package lamana;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Main {
    private final GameController game = new GameController();
    private final List<SpawnOption> spawnOptions = new ArrayList<>();
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel playerLabel = new JLabel();
    private final JLabel enemyLabel = new JLabel();
    private final JLabel resourceLabel = new JLabel();
    private final JButton upgradeButton = new JButton();
    private final BattlefieldPanel battlefield = new BattlefieldPanel(game);
    private long lastFrameNanos = System.nanoTime();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().showWindow());
    }

    private void showWindow() {
        JFrame frame = new JFrame("Animal War - Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        JPanel header = new JPanel(new BorderLayout(6, 6));
        JPanel stats = new JPanel(new java.awt.GridLayout(1, 3, 8, 4));
        stats.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        stats.add(playerLabel);
        stats.add(resourceLabel);
        stats.add(enemyLabel);
        header.add(stats, BorderLayout.NORTH);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
        header.add(statusLabel, BorderLayout.CENTER);
        frame.add(header, BorderLayout.NORTH);

        battlefield.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        frame.add(battlefield, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(8, 8));
        JPanel spawnButtons = new JPanel(new java.awt.GridLayout(3, 3, 6, 6));
        spawnButtons.setBorder(BorderFactory.createTitledBorder("Spawn hewan"));
        addSpawnOption(spawnButtons, "Ayam - $30 (Lv 1)", Ayam::new);
        addSpawnOption(spawnButtons, "Landak - $40 (Lv 1)", Landak::new);
        addSpawnOption(spawnButtons, "Kura-kura - $50 (Lv 1)", KuraKura::new);
        addSpawnOption(spawnButtons, "Harimau - $100 (Lv 2)", Harimau::new);
        addSpawnOption(spawnButtons, "Monyet - $100 (Lv 2)", Monyet::new);
        addSpawnOption(spawnButtons, "Badak - $120 (Lv 2)", Badak::new);
        addSpawnOption(spawnButtons, "Godzilla - $250 (Lv 3)", Godzilla::new);
        addSpawnOption(spawnButtons, "Naga - $250 (Lv 3)", Naga::new);
        addSpawnOption(spawnButtons, "Centaur - $300 (Lv 3)", Centaur::new);
        controls.add(spawnButtons, BorderLayout.CENTER);

        upgradeButton.addActionListener(event -> {
            game.upgradePlayerBase();
            refreshUi();
        });
        controls.add(upgradeButton, BorderLayout.SOUTH);
        controls.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        frame.add(controls, BorderLayout.SOUTH);

        refreshUi();
        frame.pack();
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Timer timer = new Timer(16, event -> {
            long now = System.nanoTime();
            long deltaMillis = Math.max(0, (now - lastFrameNanos) / 1_000_000L);
            lastFrameNanos = now;
            game.update(deltaMillis);
            refreshUi();
            battlefield.repaint();
        });
        timer.start();
    }

    private void addSpawnOption(
            JPanel panel,
            String label,
            Supplier<? extends Animal> animalFactory) {
        JButton button = new JButton(label);
        SpawnOption option = new SpawnOption(button, animalFactory);
        spawnOptions.add(option);
        button.addActionListener(event -> {
            game.spawnPlayerAnimal(animalFactory.get());
            refreshUi();
            battlefield.repaint();
        });
        panel.add(button);
    }

    private void refreshUi() {
        Base base = game.getPlayerBase();
        Tower tower = game.getEnemyTower();
        MoneyExp resources = game.getPlayerResources();

        Turret playerTurret = base.getTurret();
        Turret enemyTurret = tower.getTurret();
        playerLabel.setText("Base - HP " + base.getHp() + " - Lv " + base.getLevel()
                + " - Turret " + playerTurret.getDamage() + " dmg / "
                + playerTurret.getAttackIntervalMillis() + " ms");
        enemyLabel.setText("Tower musuh - HP " + tower.getHp() + " - Lv " + tower.getLevel()
                + " - Turret " + enemyTurret.getDamage() + " dmg / "
                + enemyTurret.getAttackIntervalMillis() + " ms - Spawn tiap "
                + (tower.getSpawnIntervalMillis() / 1000.0) + " dtk");
        resourceLabel.setText("Money: " + resources.getMoney() + " (+$"
                + game.getPassiveIncomePerSecond() + "/s) | EXP: " + resources.getExp());

        GameController.GameStatus status = game.getStatus();
        if (status == GameController.GameStatus.PLAYER_WON) {
            statusLabel.setText("Kamu menang! Tower musuh hancur.");
        } else if (status == GameController.GameStatus.PLAYER_LOST) {
            statusLabel.setText("Kamu kalah. Base kamu hancur.");
        } else if (status == GameController.GameStatus.DRAW) {
            statusLabel.setText("Seri! Kedua markas hancur.");
        } else {
            statusLabel.setText("Klik hewan untuk spawn. Unit bergerak otomatis.");
        }

        for (SpawnOption option : spawnOptions) {
            option.button.setEnabled(game.canSpawnPlayerAnimal(option.animalFactory.get()));
        }

        int expNeeded = base.getExpNeededForNextLevel();
        if (expNeeded == 0) {
            upgradeButton.setText("Base sudah level maksimum");
            upgradeButton.setEnabled(false);
        } else {
            upgradeButton.setText("Upgrade Base - butuh " + expNeeded + " EXP");
            upgradeButton.setEnabled(status == GameController.GameStatus.RUNNING
                    && resources.getExp() >= expNeeded);
        }
        if (status != GameController.GameStatus.RUNNING) {
            upgradeButton.setEnabled(false);
        }
    }

    private static class SpawnOption {
        private final JButton button;
        private final Supplier<? extends Animal> animalFactory;

        private SpawnOption(JButton button, Supplier<? extends Animal> animalFactory) {
            this.button = button;
            this.animalFactory = animalFactory;
        }
    }

    private static class BattlefieldPanel extends JPanel {
        private final GameController game;

        private BattlefieldPanel(GameController game) {
            this.game = game;
            setPreferredSize(new Dimension(900, 360));
            setBackground(new Color(238, 242, 232));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 70;
            int right = width - 70;
            int laneY = height / 2;

            g.setColor(new Color(190, 170, 125));
            g.fillRoundRect(left, laneY - 22, right - left, 44, 18, 18);
            g.setColor(new Color(130, 110, 75));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(left, laneY - 22, right - left, 44, 18, 18);

            drawBuilding(g, 12, laneY - 47, "BASE", game.getPlayerBase().getHp(),
                    new Color(55, 105, 190));
            drawBuilding(g, width - 62, laneY - 47, "TOWER", game.getEnemyTower().getHp(),
                    new Color(190, 65, 55));

            BattleLane lane = game.getBattleLane();
            drawTeam(g, lane.getPlayerAnimals(), lane, true, left, right, laneY);
            drawTeam(g, lane.getEnemyAnimals(), lane, false, left, right, laneY);

            if (game.getStatus() != GameController.GameStatus.RUNNING) {
                g.setColor(new Color(0, 0, 0, 130));
                g.fillRoundRect(width / 2 - 160, 18, 320, 38, 16, 16);
                g.setColor(Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, 18f));
                g.drawString("GAME OVER", width / 2 - 52, 44);
            }
            g.dispose();
        }

        private void drawBuilding(
                Graphics2D g,
                int x,
                int y,
                String name,
                int hp,
                Color color) {
            g.setColor(color);
            g.fillRoundRect(x, y, 50, 94, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g.drawString(name, x + 5, y + 30);
            g.drawString("HP", x + 15, y + 48);
            g.drawString(String.valueOf(hp), x + 8, y + 65);
        }

        private void drawTeam(
                Graphics2D g,
                List<Animal> animals,
                BattleLane lane,
                boolean playerTeam,
                int left,
                int right,
                int laneY) {
            Color color = playerTeam ? new Color(40, 110, 220) : new Color(210, 65, 55);
            for (Animal animal : animals) {
                double position = lane.getPosition(animal);
                int x = left + (int) ((right - left) * position / BattleLane.LANE_LENGTH);
                int y = laneY + (playerTeam ? 34 : -34);
                int diameter = 28;

                g.setColor(color);
                g.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
                g.setColor(Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, 9f));
                String label = animal.getName().substring(0, Math.min(3, animal.getName().length()));
                g.drawString(label, x - 9, y + 3);

                int barWidth = 32;
                int barX = x - barWidth / 2;
                int barY = y + (playerTeam ? 18 : -23);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(barX, barY, barWidth, 5);
                g.setColor(new Color(75, 190, 80));
                int hpWidth = (int) (barWidth * (animal.getHp() / (double) animal.getMaxHp()));
                g.fillRect(barX, barY, hpWidth, 5);
            }
        }
    }
}
