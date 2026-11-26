package lamana.view;

import lamana.contract.SpecialAbility;
import lamana.model.animal.Animal;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Shared art helpers. PNGs in resources/assets override the built-in vector art. */
final class GameArt {
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();
    private static final Map<String, BufferedImage[]> FRAME_CACHE = new HashMap<>();
    private static final Map<Integer, BufferedImage> TURRET_IMAGE_CACHE = new HashMap<>();
    private GameArt() {}

    static String assetKey(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    static String abilityFor(Animal animal) {
        return animal instanceof SpecialAbility
                ? ((SpecialAbility) animal).getAbilityName()
                : "Serangan standar";
    }

    static void drawAnimal(Graphics2D source, Animal animal, int centerX, int groundY,
                           int height, boolean playerSide, String action, int animationFrame) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String key = assetKey(animal.getName());
        String folder = key.equals("kurakura") ? "kura-kura" : key;
        boolean idle = "idle".equals(action);
        String spriteAction = idle ? "jalan" : action;
        BufferedImage image = spriteFrame("/assets/units/" + folder + "/" + spriteAction + ".png",
                idle ? 0 : animationFrame, 4);
        if (image == null) image = image("/assets/units/" + key + ".png");

        int drawW;
        int drawH;
        if (image != null) {
            int maxW = (int) (height * 1.65);
            double scale = Math.min((double) maxW / image.getWidth(), (double) height / image.getHeight());
            drawW = Math.max(16, (int) Math.round(image.getWidth() * scale));
            drawH = Math.max(16, (int) Math.round(image.getHeight() * scale));
        } else {
            drawW = (int) (height * 1.2);
            drawH = height;
        }
        Color faction = playerSide ? new Color(92, 203, 233) : new Color(248, 127, 105);

        g.setColor(new Color(11, 25, 27, 95));
        g.fillOval(centerX - drawW / 3, groundY - 5, drawW * 2 / 3, 9);
        if (image != null) {
            int x = centerX - drawW / 2;
            if (playerSide) g.drawImage(image, x, groundY - drawH, drawW, drawH, null);
            else g.drawImage(image, x + drawW, groundY - drawH, -drawW, drawH, null);
        } else {
            drawFallbackCreature(g, centerX, groundY, height, playerSide, faction);
        }
        g.dispose();
    }

    private static BufferedImage spriteFrame(String path, int frame, int frameCount) {
        BufferedImage sheet = image(path);
        if (sheet == null) return null;
        BufferedImage[] frames = FRAME_CACHE.get(path);
        if (frames == null) {
            frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                int left = i * sheet.getWidth() / frameCount;
                int right = (i + 1) * sheet.getWidth() / frameCount;
                frames[i] = trimTransparent(sheet.getSubimage(left, 0, right - left, sheet.getHeight()));
            }
            FRAME_CACHE.put(path, frames);
        }
        return frames[Math.floorMod(frame, frames.length)];
    }

    private static BufferedImage trimTransparent(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((src.getRGB(x, y) >>> 24) & 0xFF) > 10) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return src;
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    static void drawCardSprite(Graphics2D source, Animal animal, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String key = assetKey(animal.getName());
        String folder = key.equals("kurakura") ? "kura-kura" : key;
        BufferedImage image = spriteFrame("/assets/units/" + folder + "/jalan.png", 0, 4);
        if (image == null) image = image("/assets/units/" + key + ".png");

        g.setColor(new Color(10, 22, 28, 75));
        g.fillOval(x + width / 4, y + height - 5, width / 2, 7);
        if (image != null) {
            double scale = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
            int drawW = Math.max(12, (int) Math.round(image.getWidth() * scale));
            int drawH = Math.max(12, (int) Math.round(image.getHeight() * scale));
            int drawX = x + (width - drawW) / 2;
            int drawY = y + (height - drawH) / 2;
            g.drawImage(image, drawX, drawY, drawW, drawH, null);
        } else {
            drawFallbackCreature(g, x + width / 2, y + height, height, true,
                    new Color(117, 190, 155));
        }
        g.dispose();
    }

    private static void drawFallbackCreature(Graphics2D g, int centerX, int groundY, int height,
                                              boolean facingRight, Color color) {
        int direction = facingRight ? 1 : -1;
        int bodyWidth = (int) (height * .64);
        int bodyHeight = (int) (height * .40);
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = groundY - (int) (height * .57);
        g.setColor(color);
        g.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, height / 5, height / 5);
        g.fillOval(centerX + direction * bodyWidth / 4 - height / 7,
                groundY - (int) (height * .78), height / 3, height / 3);
        g.setStroke(new BasicStroke(Math.max(3, height / 13), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int leg = 0; leg < 4; leg++) {
            int legX = bodyX + bodyWidth * (leg + 1) / 5;
            g.drawLine(legX, bodyY + bodyHeight - 3, legX, groundY - 3);
        }
        g.drawLine(centerX - direction * bodyWidth / 2, bodyY + bodyHeight / 2,
                centerX - direction * (int) (height * .48), bodyY + bodyHeight / 3);
        g.setColor(new Color(37, 49, 42));
        g.fillOval(centerX + direction * (int) (height * .25), groundY - (int) (height * .71),
                Math.max(3, height / 18), Math.max(3, height / 18));
    }

    static void drawBase(Graphics2D source, int centerX, int groundY, boolean playerSide,
                         int size, int level, boolean firing) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = size;
        int height = (int) (size * 1.28);
        int x = centerX - width / 2;
        int y = groundY - height;
        String assetName = playerSide ? "player-base" : "enemy-base";
        BufferedImage image = image("/assets/buildings/" + assetName + "-level-" + level + ".png");
        if (image == null) image = image("/assets/buildings/" + assetName + ".png");

        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            Color stone = playerSide ? new Color(72, 137, 167) : new Color(179, 91, 78);
            Color shadow = playerSide ? new Color(37, 86, 111) : new Color(119, 54, 49);
            g.setColor(new Color(7, 18, 27, 60));
            g.fillOval(x - 8, groundY - 8, width + 16, 22);

            // Main keep and side towers.
            g.setColor(shadow);
            g.fillRoundRect(x + 8, y + 34, width - 16, height - 34, 8, 8);
            g.fillRoundRect(x - 1, y + 18, width / 4, height - 18, 7, 7);
            g.fillRoundRect(x + width - width / 4 + 1, y + 18, width / 4, height - 18, 7, 7);
            g.setColor(stone);
            g.fillRoundRect(x + 12, y + 30, width - 24, height - 38, 7, 7);
            g.fillRoundRect(x + 3, y + 14, width / 4 - 4, height - 20, 6, 6);
            g.fillRoundRect(x + width - width / 4 + 1, y + 14, width / 4 - 4, height - 20, 6, 6);

            // Battlements and central gate.
            g.setColor(stone.brighter());
            int towerWidth = width / 4 - 4;
            for (int towerX : new int[]{x + 3, x + width - width / 4 + 1}) {
                for (int i = 0; i < 3; i++) {
                    g.fillRect(towerX + 3 + i * (towerWidth - 6) / 3, y + 7, 6, 12);
                }
            }
            g.fillRect(x + width / 2 - 5, groundY - 33, 10, 29);
            g.setColor(new Color(25, 38, 48));
            g.fillRoundRect(x + width / 2 - 5, groundY - 25, 10, 21, 5, 5);

            // Stone seams.
            g.setColor(new Color(235, 241, 226, 75));
            g.drawLine(x + 15, y + 51, x + width - 15, y + 51);
            g.drawLine(x + 15, y + 68, x + width - 15, y + 68);
        }

        // Mounted Turret / Meriam pertahanan di atas benteng (sesuai komposisi Base -> Turret)
        drawMountedTurret(g, centerX, y, width, playerSide, level, firing);
        g.dispose();
    }

    private static void drawMountedTurret(Graphics2D g, int centerX, int baseTopY, int size,
                                          boolean playerSide, int level, boolean firing) {
        int dir = playerSide ? 1 : -1;
        BufferedImage image = turretImage(level);
        if (image == null) return;

        int drawWidth = Math.max(48, (int) Math.round(size * 0.92));
        int drawHeight = Math.max(32, (int) Math.round(
                drawWidth * (double) image.getHeight() / image.getWidth()));
        int drawX = centerX - drawWidth / 2;
        int mountY = baseTopY + Math.max(8, size / 6);
        int drawY = mountY - drawHeight;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (playerSide) {
            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        } else {
            g.drawImage(image, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
        }

        if (firing) {
            Point muzzle = turretMuzzlePoint(centerX, baseTopY, size, playerSide, level);
            Path2D flash = new Path2D.Double();
            flash.moveTo(muzzle.x, muzzle.y - 5);
            flash.lineTo(muzzle.x + dir * 14, muzzle.y);
            flash.lineTo(muzzle.x, muzzle.y + 5);
            flash.closePath();
            g.setColor(new Color(255, 183, 63, 230));
            g.fill(flash);
            g.setColor(new Color(255, 239, 164));
            g.fillOval(muzzle.x - 2, muzzle.y - 2, 4, 4);
        }
    }

    static Point turretMuzzlePoint(int centerX, int baseTopY, int size, boolean playerSide, int level) {
        BufferedImage image = turretImage(level);
        if (image == null) return new Point(centerX, baseTopY);

        int drawWidth = Math.max(48, (int) Math.round(size * 0.92));
        int drawHeight = Math.max(32, (int) Math.round(
                drawWidth * (double) image.getHeight() / image.getWidth()));
        int mountY = baseTopY + Math.max(8, size / 6);
        int drawX = centerX - drawWidth / 2;
        int drawY = mountY - drawHeight;
        int muzzleX = drawX + (int) Math.round(drawWidth * (playerSide ? 0.985 : 0.015));
        int muzzleY = drawY + (int) Math.round(drawHeight * 0.38);
        return new Point(muzzleX, muzzleY);
    }

    private static BufferedImage turretImage(int level) {
        if (TURRET_IMAGE_CACHE.containsKey(level)) return TURRET_IMAGE_CACHE.get(level);

        BufferedImage source = image("/assets/buildings/turret-level-" + level + ".png");
        if (source == null) source = image("/assets/buildings/turret.png");
        BufferedImage trimmed = source == null ? null : trimTransparent(source);
        TURRET_IMAGE_CACHE.put(level, trimmed);
        return trimmed;
    }

    private static BufferedImage image(String resourcePath) {
        if (IMAGE_CACHE.containsKey(resourcePath)) return IMAGE_CACHE.get(resourcePath);
        BufferedImage loaded = null;
        try (InputStream stream = GameArt.class.getResourceAsStream(resourcePath)) {
            if (stream != null) loaded = ImageIO.read(stream);
        } catch (IOException ignored) {
            // Keep the vector fallback if an optional art file cannot be read.
        }
        IMAGE_CACHE.put(resourcePath, loaded);
        return loaded;
    }
}
