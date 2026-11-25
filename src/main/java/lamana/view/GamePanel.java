package lamana.view;

import lamana.controller.GameController;
import lamana.core.GameConfig;
import lamana.core.GameState;
import lamana.model.animal.Animal;
import lamana.model.building.Base;

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
import java.util.ArrayList;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/** Responsive Java2D battle lane with built-in art and optional resource images. */
public class GamePanel extends JPanel {
    private static final BufferedImage ARENA_IMAGE = loadArenaImage();
    private static final long ANIMATION_FRAME_MS = 110L;
    private static final long ACTION_ANIMATION_MS = ANIMATION_FRAME_MS * 4L;
    private static final long TURRET_PROJECTILE_MS = 260L;

    private GameController game;
    private boolean paused;
    private long animationTimeMs;
    private final Map<Animal, UnitVisual> unitVisuals = new IdentityHashMap<>();
    private final List<TurretProjectile> turretProjectiles = new ArrayList<>();
    private long playerMuzzleFlashUntilMs;
    private long enemyMuzzleFlashUntilMs;

    public GamePanel(GameController game) {
        this.game = game;
        setOpaque(true);
        setBackground(new Color(67, 113, 116));
        setPreferredSize(new java.awt.Dimension(1200, 540));
    }

    public void setGame(GameController game) {
        this.game = game;
        this.paused = false;
        this.animationTimeMs = 0;
        this.unitVisuals.clear();
        this.turretProjectiles.clear();
        this.playerMuzzleFlashUntilMs = 0;
        this.enemyMuzzleFlashUntilMs = 0;
        repaint();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        repaint();
    }

    public void advanceAnimation(long deltaMs) {
        if (paused || deltaMs <= 0) return;
        animationTimeMs += deltaMs;
        for (lamana.model.battle.BattleLane.TurretShot shot
                : game.getBattleLane().getTurretShotsThisUpdate()) {
            turretProjectiles.add(new TurretProjectile(shot.isPlayerSide(),
                    shot.getTargetPosition(), animationTimeMs));
            if (shot.isPlayerSide()) playerMuzzleFlashUntilMs = animationTimeMs + 90L;
            else enemyMuzzleFlashUntilMs = animationTimeMs + 90L;
        }
        Iterator<TurretProjectile> projectileIterator = turretProjectiles.iterator();
        while (projectileIterator.hasNext()) {
            if (animationTimeMs - projectileIterator.next().startedAtMs >= TURRET_PROJECTILE_MS) {
                projectileIterator.remove();
            }
        }
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
        drawBattleLane(g, width, height);
        drawBases(g, width, height);
        drawUnits(g, width, height);
        drawTurretProjectiles(g, width, height);
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

    private void drawBattleLane(Graphics2D g, int width, int height) {
        int top = (int) (height * .65);
        int laneLeft = Math.max(66, (int) (width * .075));
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

        int divisions = 6;
        for (int i = 0; i <= divisions; i++) {
            int tickX = laneLeft + i * laneWidth / divisions;
            g.setColor(new Color(242, 223, 167, 130));
            g.drawLine(tickX, top + 87, tickX, top + 95);
            String marker = Integer.toString((int) Math.round(i * GameConfig.LANE_LENGTH / divisions));
            g.setFont(new Font("Dialog", Font.BOLD, 9));
            g.setColor(new Color(227, 226, 192));
            g.drawString(marker, tickX - 4, top + 108);
        }

        // Grass lip and small stones to make the path feel part of the scene.
        g.setColor(new Color(65, 119, 69));
        g.fillRoundRect(laneLeft + 7, top + 18, laneWidth - 14, 9, 8, 8);
        g.setColor(new Color(213, 185, 128, 150));
        for (int i = 0; i < 24; i++) {
            int pebbleX = laneLeft + 20 + (i * 73) % Math.max(1, laneWidth - 40);
            int pebbleY = top + 40 + (i % 4) * 11;
            g.fillOval(pebbleX, pebbleY, 4 + i % 3, 3);
        }
    }

    private void drawBases(Graphics2D g, int width, int height) {
        int top = (int) (height * .65);
        int groundY = top + 93;
        int baseSize = Math.max(70, Math.min(103, width / 13));
        int leftX = Math.max(66, (int) (width * .055));
        int rightX = width - leftX;

        Base player = game.getPlayerBase();
        Base enemy = game.getEnemyBase();
        GameArt.drawBase(g, leftX, groundY, true, baseSize, player.getLevel(),
                animationTimeMs < playerMuzzleFlashUntilMs);
        GameArt.drawBase(g, rightX, groundY, false, baseSize, enemy.getLevel(),
                animationTimeMs < enemyMuzzleFlashUntilMs);

        int hudTopY = groundY - (int) (baseSize * 1.28) - 26;
        drawBaseStatus(g, player, leftX, hudTopY, new Color(67, 189, 225));
        drawBaseStatus(g, enemy, rightX, hudTopY, new Color(245, 109, 97));
    }

    private static void drawBaseStatus(Graphics2D g, Base base, int centerX, int y, Color barColor) {
        int barW = 82;
        int barX = centerX - barW / 2;
        int barY = y - 3;
        g.setColor(new Color(22, 34, 37, 210));
        g.fillRect(barX, barY, barW, 4);
        int fillW = (int) Math.round((double) base.getHp() / Math.max(1, base.getMaxHp()) * barW);
        g.setColor(barColor);
        g.fillRect(barX, barY, fillW, 4);

        int pipSize = 5;
        int gap = 3;
        int pipsW = pipSize * 3 + gap * 2;
        int pipX = centerX - pipsW / 2;
        for (int i = 0; i < 3; i++) {
            g.setColor(i < base.getLevel() ? barColor : new Color(33, 47, 48, 190));
            g.fillRect(pipX + i * (pipSize + gap), barY - 8, pipSize, pipSize);
        }
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
        Map<Animal, Boolean> active = new IdentityHashMap<>();
        for (Animal animal : playerUnits) registerActiveUnit(animal, true, active);
        for (Animal animal : enemyUnits) registerActiveUnit(animal, false, active);

        Iterator<Map.Entry<Animal, UnitVisual>> iterator = unitVisuals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Animal, UnitVisual> entry = iterator.next();
            UnitVisual visual = entry.getValue();
            if (!active.containsKey(entry.getKey()) && !visual.dead) {
                visual.dead = true;
                visual.action = "mati";
                visual.actionStartedMs = animationTimeMs;
            }
            if (visual.dead && animationTimeMs - visual.actionStartedMs >= ACTION_ANIMATION_MS) {
                iterator.remove();
            }
        }

        for (Animal animal : enemyUnits) {
            drawVisual(g, animal, unitVisuals.get(animal), laneLeft, laneWidth, groundY, size);
        }
        for (Animal animal : playerUnits) {
            drawVisual(g, animal, unitVisuals.get(animal), laneLeft, laneWidth, groundY - 4, size);
        }
        for (Map.Entry<Animal, UnitVisual> entry : unitVisuals.entrySet()) {
            if (entry.getValue().dead) drawVisual(g, entry.getKey(), entry.getValue(), laneLeft, laneWidth, groundY, size);
        }
    }

    private void drawTurretProjectiles(Graphics2D g, int width, int height) {
        if (turretProjectiles.isEmpty()) return;
        int top = (int) (height * .65);
        int baseGroundY = top + 93;
        int baseSize = Math.max(70, Math.min(103, width / 13));
        int baseLeftX = Math.max(66, (int) (width * .055));
        int baseRightX = width - baseLeftX;
        int baseTopY = baseGroundY - (int) (baseSize * 1.28);
        int laneLeft = Math.max(65, (int) (width * .075)) + 25;
        int laneWidth = width - laneLeft * 2;
        int unitSize = Math.max(72, Math.min(108, width / 14));
        int targetY = top + 90 - unitSize / 2;

        Graphics2D shots = (Graphics2D) g.create();
        shots.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        shots.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (TurretProjectile projectile : turretProjectiles) {
            double progress = Math.min(1.0,
                    (animationTimeMs - projectile.startedAtMs) / (double) TURRET_PROJECTILE_MS);
            int baseX = projectile.playerSide ? baseLeftX : baseRightX;
            int baseLevel = projectile.playerSide
                    ? game.getPlayerBase().getLevel() : game.getEnemyBase().getLevel();
            java.awt.Point muzzle = GameArt.turretMuzzlePoint(baseX, baseTopY, baseSize,
                    projectile.playerSide, baseLevel);
            int targetX = laneLeft + (int) Math.round(laneWidth
                    * projectile.targetPosition / GameConfig.LANE_LENGTH);
            int shotX = (int) Math.round(muzzle.x + (targetX - muzzle.x) * progress);
            int shotY = (int) Math.round(muzzle.y + (targetY - muzzle.y) * progress);
            int alpha = Math.max(75, 255 - (int) (progress * 150));

            shots.setColor(new Color(255, 178, 62, alpha));
            shots.drawLine(muzzle.x, muzzle.y, shotX, shotY);
            shots.setColor(new Color(255, 217, 118, alpha));
            shots.fillOval(shotX - 5, shotY - 5, 10, 10);
            shots.setColor(new Color(255, 247, 204, alpha));
            shots.fillOval(shotX - 2, shotY - 2, 4, 4);
        }
        shots.dispose();
    }

    private void registerActiveUnit(Animal animal, boolean playerSide, Map<Animal, Boolean> active) {
        active.put(animal, Boolean.TRUE);
        UnitVisual visual = unitVisuals.get(animal);
        if (visual == null) {
            visual = new UnitVisual(playerSide, animal.getPosition(), "spawn", animationTimeMs);
            unitVisuals.put(animal, visual);
        } else {
            visual.position = animal.getPosition();
            visual.playerSide = playerSide;
            String nextAction;
            if (visual.action.equals("spawn") && animationTimeMs - visual.actionStartedMs < ACTION_ANIMATION_MS) {
                nextAction = "spawn";
            } else if (animal.isAttackingAnimation()) {
                nextAction = "serang";
            } else if (animal.hasMovedThisTick()) {
                nextAction = "jalan";
            } else {
                nextAction = "idle";
            }
            if (!nextAction.equals(visual.action)) {
                visual.action = nextAction;
                visual.actionStartedMs = animationTimeMs;
            }
        }
    }

    private void drawVisual(Graphics2D g, Animal animal, UnitVisual visual,
                            int laneLeft, int laneWidth, int groundY, int size) {
        if (visual == null) return;
        int x = positionToScreenX(visual.position, laneLeft, laneWidth);
        int frame;
        if (visual.action.equals("idle")) {
            frame = 0;
        } else if (visual.action.equals("jalan")) {
            frame = (int) ((animationTimeMs / ANIMATION_FRAME_MS) % 4L);
        } else {
            frame = (int) Math.min(3, Math.max(0, (animationTimeMs - visual.actionStartedMs) / ANIMATION_FRAME_MS));
        }
        drawUnit(g, animal, x, groundY, size, visual.playerSide, visual.action, frame);
    }

    private static int positionToScreenX(double position, int laneLeft, int laneWidth) {
        return laneLeft + (int) (laneWidth * position / GameConfig.LANE_LENGTH);
    }

    private void drawUnit(Graphics2D g, Animal animal, int x, int groundY, int size,
                          boolean playerSide, String action, int animationFrame) {
        int barWidth = size + 7;
        GameArt.drawAnimal(g, animal, x, groundY, size, playerSide, action, animationFrame);
        if (action.equals("mati")) return;
        g.setColor(new Color(15, 31, 35, 220));
        g.fillRoundRect(x - barWidth / 2, groundY - size - 9, barWidth, 5, 4, 4);
        int fillWidth = (int) Math.round((double) animal.getHp() / animal.getMaxHp() * barWidth);
        g.setColor(playerSide ? new Color(109, 224, 191) : new Color(255, 133, 112));
        g.fillRoundRect(x - barWidth / 2, groundY - size - 9, fillWidth, 5, 4, 4);
    }

    private static final class UnitVisual {
        private boolean playerSide;
        private double position;
        private String action;
        private long actionStartedMs;
        private boolean dead;

        UnitVisual(boolean playerSide, double position, String action, long actionStartedMs) {
            this.playerSide = playerSide;
            this.position = position;
            this.action = action;
            this.actionStartedMs = actionStartedMs;
        }
    }

    private static final class TurretProjectile {
        private final boolean playerSide;
        private final double targetPosition;
        private final long startedAtMs;

        TurretProjectile(boolean playerSide, double targetPosition, long startedAtMs) {
            this.playerSide = playerSide;
            this.targetPosition = targetPosition;
            this.startedAtMs = startedAtMs;
        }
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
        g.setFont(new Font("Dialog", Font.BOLD, Math.min(34, Math.max(24, width / 32))));
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(accent);
        g.drawString(message, (width - metrics.stringWidth(message)) / 2, height / 2 + 10);
    }

    private static BufferedImage loadArenaImage() {
        try (InputStream input = GamePanel.class.getResourceAsStream("/assets/background/bg real.png")) {
            return input == null ? null : ImageIO.read(input);
        } catch (IOException ignored) {
            return null;
        }
    }
}
