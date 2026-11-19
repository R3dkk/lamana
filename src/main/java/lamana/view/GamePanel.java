package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.model.animal.Animal;
import lamana.model.building.Base;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Canvas Swing 900x360 untuk render arena permainan.
 * Menampilkan lane, base, unit player, dan unit musuh.
 */
public class GamePanel extends JPanel {

    private static final int PANEL_WIDTH  = GameConfig.WINDOW_WIDTH;
    private static final int PANEL_HEIGHT = GameConfig.WINDOW_HEIGHT - 260; // sisakan ruang HUD + south panel

    // Arena lane bounds
    private static final int LANE_LEFT   = 120;
    private static final int LANE_TOP    = 320;
    private static final int LANE_WIDTH  = 1680;
    private static final int LANE_HEIGHT = 120;

    // Unit drawing
    private static final int UNIT_RADIUS = 18;

    private final GameController game;

    public GamePanel(GameController game) {
        this.game = game;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(173, 216, 230)); // light blue
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawLane(g2);
        drawBases(g2);
        drawUnits(g2);

        if (game.getState() != GameState.PLAYING) {
            drawGameOverOverlay(g2);
        }
    }

    private void drawLane(Graphics2D g) {
        g.setColor(new Color(139, 90, 43)); // brown
        g.fillRoundRect(LANE_LEFT, LANE_TOP, LANE_WIDTH, LANE_HEIGHT, 20, 20);
        g.setColor(new Color(101, 67, 33));
        g.drawRoundRect(LANE_LEFT, LANE_TOP, LANE_WIDTH, LANE_HEIGHT, 20, 20);
    }

    private void drawBases(Graphics2D g) {
        Base pb = game.getPlayerBase();
        Base eb = game.getEnemyBase();
        int laneMidY = LANE_TOP + LANE_HEIGHT / 2;

        // Player base (left, blue)
        drawBuilding(g, LANE_LEFT - 60, laneMidY, "BASE",
                pb.getHp(), pb.getMaxHp(), pb.getLevel(), new Color(30, 100, 220));

        // Enemy base (right, red)
        drawBuilding(g, LANE_LEFT + LANE_WIDTH + 60, laneMidY, "MUSUH",
                eb.getHp(), eb.getMaxHp(), eb.getLevel(), new Color(200, 40, 40));
    }

    private void drawBuilding(Graphics2D g, int cx, int cy, String label,
                               int hp, int maxHp, int level, Color color) {
        int w = 60, h = 70;
        int x = cx - w / 2, y = cy - h / 2;

        g.setColor(color);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(label, x + 4, y + 14);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.drawString("HP:" + hp, x + 4, y + 27);
        g.drawString("Lv:" + level, x + 4, y + 39);

        // HP bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y + h + 4, w, 6);
        int barW = (int)((double) hp / maxHp * w);
        g.setColor(hp > maxHp / 2 ? Color.GREEN : Color.ORANGE);
        g.fillRect(x, y + h + 4, barW, 6);
    }

    private void drawUnits(Graphics2D g) {
        List<Animal> playerUnits = game.getBattleLane().getPlayerAnimals();
        List<Animal> enemyUnits  = game.getBattleLane().getEnemyAnimals();

        int laneMidY = LANE_TOP + LANE_HEIGHT / 2;

        for (Animal a : playerUnits) {
            int px = worldToPixelX(a.getPosition());
            // Player units above lane
            drawUnit(g, a, px, laneMidY - UNIT_RADIUS - 10, true);
        }

        for (Animal a : enemyUnits) {
            int px = worldToPixelX(a.getPosition());
            // Enemy units below lane
            drawUnit(g, a, px, laneMidY + UNIT_RADIUS + 10, false);
        }
    }

    private void drawUnit(Graphics2D g, Animal animal, int cx, int cy, boolean isPlayer) {
        Color fill = isPlayer ? new Color(50, 120, 240) : new Color(220, 50, 50);
        Color outline = isPlayer ? new Color(20, 70, 180) : new Color(140, 20, 20);

        g.setColor(fill);
        g.fillOval(cx - UNIT_RADIUS, cy - UNIT_RADIUS, UNIT_RADIUS * 2, UNIT_RADIUS * 2);
        g.setColor(outline);
        g.drawOval(cx - UNIT_RADIUS, cy - UNIT_RADIUS, UNIT_RADIUS * 2, UNIT_RADIUS * 2);

        // Label: first 3 chars of name
        String label = animal.getName().length() > 3
                ? animal.getName().substring(0, 3).toUpperCase()
                : animal.getName().toUpperCase();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        int lx = cx - fm.stringWidth(label) / 2;
        g.drawString(label, lx, cy + 4);

        // HP bar above/below oval
        int barW = UNIT_RADIUS * 2;
        int barX = cx - UNIT_RADIUS;
        int barY = isPlayer ? cy - UNIT_RADIUS - 8 : cy + UNIT_RADIUS + 2;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, 4);
        int hpW = (int)((double) animal.getHp() / animal.getMaxHp() * barW);
        g.setColor(Color.GREEN);
        g.fillRect(barX, barY, hpW, 4);
    }

    private int worldToPixelX(double worldPos) {
        return LANE_LEFT + (int)(LANE_WIDTH * worldPos / GameConfig.LANE_LENGTH);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());

        String msg;
        switch (game.getState()) {
            case VICTORY:
                msg = "KEMENANGAN!";
                break;
            case DEFEAT:
                msg = "KEKALAHAN!";
                break;
            case DRAW:
                msg = "SERI!";
                break;
            default:
                msg = "";
                break;
        }

        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(game.getState() == GameState.VICTORY ? Color.YELLOW : Color.RED);
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}
