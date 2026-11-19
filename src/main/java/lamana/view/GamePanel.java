package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.model.animal.Animal;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Responsive Java2D battle lane with built-in art and optional resource images. */
public class GamePanel extends JPanel {
    private static final BufferedImage ARENA_IMAGE = loadArenaImage();

    private GameController game;
    private boolean paused;

    public GamePanel(GameController game) {
        this.game = game;
        setOpaque(true);
        setBackground(new Color(67, 113, 116));
        setPreferredSize(new java.awt.Dimension(1100, 480));
    }

    public void setGame(GameController game) {
        this.game = game;
        this.paused = false;
        repaint();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (game == null) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int width = getWidth();
        int height = getHeight();
        drawBackdrop(g, width, height);
        drawArenaLabel(g);
        drawBattleLane(g, width, height);
        drawBases(g, width, height);
        drawUnits(g, width, height);
        if (game.getState() != GameState.PLAYING) drawGameOverOverlay(g, width, height);
        g.dispose();
    }

    private void drawBackdrop(Graphics2D g, int width, int height) {
        if (ARENA_IMAGE != null) {
            g.drawImage(ARENA_IMAGE, 0, 0, width, height, null);
            g.setColor(new Color(12, 29, 35, 32));
            g.fillRect(0, 0, width, height);
            return;
        }

        g.setPaint(new GradientPaint(0, 0, new Color(143, 196, 185),
                0, height, new Color(49, 91, 75)));
        g.fillRect(0, 0, width, height);

        // Soft afternoon sun and clouds.
        g.setColor(new Color(255, 228, 156, 130));
        g.fill(new Ellipse2D.Double(width * .76, height * .08, 100, 100));
        drawCloud(g, (int) (width * .14), (int) (height * .16), 1f);
        drawCloud(g, (int) (width * .48), (int) (height * .22), .72f);

        // Far hills and layered Indonesian forest silhouettes.
        g.setColor(new Color(102, 156, 125, 190));
        int[] farX = {0, (int) (width * .14), (int) (width * .27), (int) (width * .42),
                (int) (width * .58), (int) (width * .74), width};
        int[] farY = {(int) (height * .56), (int) (height * .42), (int) (height * .57),
                (int) (height * .43), (int) (height * .58), (int) (height * .45), (int) (height * .56)};
        g.fillPolygon(farX, farY, farX.length);
        g.setColor(new Color(52, 111, 85, 220));
        int[] nearX = {0, (int) (width * .11), (int) (width * .22), (int) (width * .36),
                (int) (width * .51), (int) (width * .67), (int) (width * .83), width};
        int[] nearY = {(int) (height * .65), (int) (height * .49), (int) (height * .63),
                (int) (height * .50), (int) (height * .66), (int) (height * .51),
                (int) (height * .62), (int) (height * .50)};
        g.fillPolygon(nearX, nearY, nearX.length);

        // Ground fades into the horizon so units and lane stay legible.
        int horizon = (int) (height * .56);
        g.setPaint(new GradientPaint(0, horizon, new Color(78, 137, 95, 185),
                0, height, new Color(37, 81, 59, 250)));
        g.fillRect(0, horizon, width, height - horizon);
        drawFoliage(g, width, height);
    }

    private static void drawCloud(Graphics2D g, int x, int y, float scale) {
        Graphics2D cloud = (Graphics2D) g.create();
        cloud.translate(x, y);
        cloud.scale(scale, scale);
        cloud.setColor(new Color(237, 246, 225, 105));
        cloud.fillRoundRect(0, 13, 104, 28, 20, 20);
        cloud.fillOval(19, 0, 36, 36);
        cloud.fillOval(43, 5, 39, 34);
        cloud.dispose();
    }

    private static void drawFoliage(Graphics2D g, int width, int height) {
        int baseY = (int) (height * .68);
        for (int side = 0; side < 2; side++) {
            int x = side == 0 ? 0 : width - 115;
            g.setColor(new Color(25, 77, 56, 180));
            for (int i = 0; i < 6; i++) {
                int leafX = x + (side == 0 ? i * 18 : i * 18);
                int leafY = baseY - (i % 3) * 15;
                g.fillOval(leafX - 13, leafY - 55, 64, 88);
            }
            g.setColor(new Color(155, 185, 112, 130));
            for (int i = 0; i < 4; i++) {
                int leafX = x + 11 + i * 20;
                g.drawArc(leafX - 10, baseY - 74 - (i % 2) * 10, 55, 82, 10, 145);
            }
        }
    }

    private void drawArenaLabel(Graphics2D g) {
        g.setColor(new Color(12, 35, 40, 155));
        g.fillRoundRect(20, 17, 200, 45, 13, 13);
        g.setColor(new Color(206, 235, 216));
        g.setFont(new Font("Dialog", Font.BOLD, 10));
        g.drawString("NUSANTARA  /  FRONTLINE", 33, 35);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        g.drawString("BATTLE LANE", 33, 53);

        g.setFont(new Font("Dialog", Font.BOLD, 10));
        g.setColor(new Color(221, 241, 237));
        String status = game.getState() != GameState.PLAYING ? "PERTANDINGAN SELESAI"
                : (paused ? "PERTEMPURAN DIJEDA" : "PERTEMPURAN BERLANGSUNG");
        FontMetrics metrics = g.getFontMetrics();
        int x = getWidth() - metrics.stringWidth(status) - 27;
        g.drawString(status, x, 32);
        g.setColor(new Color(9, 39, 39, 80));
        g.fillOval(x - 14, 24, 7, 7);
        g.setColor(paused ? new Color(246, 199, 104) : new Color(139, 227, 154));
        g.fillOval(x - 14, 24, 7, 7);
    }

    private void drawBattleLane(Graphics2D g, int width, int height) {
        int top = (int) (height * .65);
        int laneLeft = Math.max(65, (int) (width * .075));
        int laneRight = width - laneLeft;
        int laneWidth = laneRight - laneLeft;

        g.setColor(new Color(10, 37, 32, 60));
        g.fillOval(laneLeft - 34, top + 67, laneWidth + 68, 51);
        g.setPaint(new GradientPaint(0, top + 24, new Color(128, 93, 61),
                0, top + 88, new Color(79, 54, 42)));
        g.fillRoundRect(laneLeft, top + 21, laneWidth, 62, 25, 25);
        g.setColor(new Color(62, 48, 38));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(laneLeft, top + 21, laneWidth, 62, 25, 25);
        g.setColor(new Color(166, 145, 97));
        g.fillRoundRect(laneLeft + 3, top + 20, laneWidth - 6, 13, 12, 12);
        g.setColor(new Color(188, 196, 132));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                new float[]{3f, 10f}, 0f));
        g.drawLine(laneLeft + 20, top + 52, laneRight - 20, top + 52);
        g.setStroke(new BasicStroke(1f));

        for (int i = 0; i <= 4; i++) {
            int tickX = laneLeft + i * laneWidth / 4;
            g.setColor(new Color(242, 223, 167, 130));
            g.drawLine(tickX, top + 87, tickX, top + 95);
            String marker = Integer.toString(i * (int) (GameConfig.LANE_LENGTH / 4));
            g.setFont(new Font("Dialog", Font.BOLD, 9));
            g.setColor(new Color(227, 226, 192));
            g.drawString(marker, tickX - 3, top + 108);
        }

        // Grass lip and small stones to make the path feel part of the scene.
        g.setColor(new Color(65, 119, 69));
        g.fillRoundRect(laneLeft + 7, top + 18, laneWidth - 14, 9, 8, 8);
        g.setColor(new Color(213, 185, 128, 150));
        for (int i = 0; i < 18; i++) {
            int pebbleX = laneLeft + 20 + (i * 73) % Math.max(1, laneWidth - 40);
            int pebbleY = top + 40 + (i % 4) * 11;
            g.fillOval(pebbleX, pebbleY, 4 + i % 3, 3);
        }
    }

    private void drawBases(Graphics2D g, int width, int height) {
        int top = (int) (height * .65);
        int groundY = top + 93;
        int baseSize = Math.max(70, Math.min(103, width / 13));
        int leftX = Math.max(48, (int) (width * .055));
        int rightX = width - leftX;

        GameArt.drawBase(g, leftX, groundY, true, baseSize);
        GameArt.drawBase(g, rightX, groundY, false, baseSize);
        drawBaseName(g, "PLAYER", leftX, groundY - (int) (baseSize * 1.28) - 44,
                new Color(190, 236, 244));
        drawBaseName(g, "MUSUH", rightX, groundY - (int) (baseSize * 1.28) - 44,
                new Color(255, 206, 196));
    }

    private static void drawBaseName(Graphics2D g, String name, int centerX, int y, Color color) {
        g.setFont(new Font("Dialog", Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(name) + 17;
        g.setColor(new Color(8, 28, 33, 172));
        g.fillRoundRect(centerX - width / 2, y - 12, width, 21, 8, 8);
        g.setColor(color);
        g.drawString(name, centerX - metrics.stringWidth(name) / 2, y + 2);
    }

    private void drawUnits(Graphics2D g, int width, int height) {
        int top = (int) (height * .65);
        int laneLeft = Math.max(65, (int) (width * .075)) + 25;
        int laneRight = width - laneLeft;
        int laneWidth = laneRight - laneLeft;
        int size = Math.max(72, Math.min(108, width / 14));
        List<Animal> playerUnits = game.getBattleLane().getPlayerAnimals();
        List<Animal> enemyUnits = game.getBattleLane().getEnemyAnimals();
        int groundY = top + 90;
        for (Animal animal : enemyUnits) {
            int x = laneLeft + (int) (laneWidth * animal.getPosition() / GameConfig.LANE_LENGTH);
            drawUnit(g, animal, x, groundY, size, false);
        }
        for (Animal animal : playerUnits) {
            int x = laneLeft + (int) (laneWidth * animal.getPosition() / GameConfig.LANE_LENGTH);
            drawUnit(g, animal, x, groundY - 4, size, true);
        }
    }

    private static void drawUnit(Graphics2D g, Animal animal, int x, int groundY, int size, boolean playerSide) {
        int barWidth = size + 7;
        GameArt.drawAnimal(g, animal, x, groundY, size, playerSide);
        g.setColor(new Color(15, 31, 35, 220));
        g.fillRoundRect(x - barWidth / 2, groundY - size - 9, barWidth, 5, 4, 4);
        int fillWidth = (int) Math.round((double) animal.getHp() / animal.getMaxHp() * barWidth);
        g.setColor(playerSide ? new Color(109, 224, 191) : new Color(255, 133, 112));
        g.fillRoundRect(x - barWidth / 2, groundY - size - 9, fillWidth, 5, 4, 4);
        g.setFont(new Font("Dialog", Font.BOLD, 9));
        FontMetrics metrics = g.getFontMetrics();
        String name = animal.getName().toUpperCase();
        int textWidth = metrics.stringWidth(name) + 10;
        g.setColor(new Color(10, 30, 35, 195));
        g.fillRoundRect(x - textWidth / 2, groundY + 3, textWidth, 14, 6, 6);
        g.setColor(Color.WHITE);
        g.drawString(name, x - metrics.stringWidth(name) / 2, groundY + 13);
    }

    private void drawGameOverOverlay(Graphics2D g, int width, int height) {
        g.setColor(new Color(6, 18, 25, 185));
        g.fillRect(0, 0, width, height);
        String message;
        Color accent;
        switch (game.getState()) {
            case VICTORY:
                message = "KEMENANGAN!";
                accent = new Color(255, 216, 119);
                break;
            case DEFEAT:
                message = "PERTAHANAN RUNTUH";
                accent = new Color(255, 151, 131);
                break;
            case DRAW:
                message = "PERTEMPURAN SERI";
                accent = Color.WHITE;
                break;
            default:
                return;
        }
        g.setFont(new Font("Dialog", Font.BOLD, Math.min(48, Math.max(30, width / 24))));
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(new Color(7, 23, 31, 220));
        g.fillRoundRect((width - metrics.stringWidth(message) - 60) / 2,
                height / 2 - 49, metrics.stringWidth(message) + 60, 90, 24, 24);
        g.setColor(accent);
        g.drawString(message, (width - metrics.stringWidth(message)) / 2, height / 2 + 10);
    }

    private static BufferedImage loadArenaImage() {
        try (InputStream input = GamePanel.class.getResourceAsStream("/assets/background/arena.png")) {
            return input == null ? null : ImageIO.read(input);
        } catch (IOException ignored) {
            return null;
        }
    }
}
