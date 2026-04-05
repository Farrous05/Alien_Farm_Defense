package com.fermedefense.vue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Charge et met en cache les sprites des aliens.
 *
 * Priorité : sprites PNG Kenney (alien UFO pack).
 * Fallback  : sprites dessinés procéduralement si les PNGs sont absents.
 *
 * Tailles d'affichage recommandées :
 *   NORMAL 64×64 · RUNNER 52×52 · TANK 72×72 · BOSS 96×96
 */
public final class CacheSpritesAliens {

    private CacheSpritesAliens() {}

    // ── PNG sprites (Kenney alien-ufo-pack) ──────────────────────────────────
    private static final BufferedImage SHIP_NORMAL;
    private static final BufferedImage SHIP_RUNNER;
    private static final BufferedImage SHIP_TANK;
    private static final BufferedImage BOSS_FULL;   // >66% HP
    private static final BufferedImage BOSS_DMG1;   // 33–66% HP
    private static final BufferedImage BOSS_DMG2;   // <33% HP

    // ── Procedural fallbacks ─────────────────────────────────────────────────
    private static final BufferedImage PROC_NORMAL = genNormal();
    private static final BufferedImage PROC_RUNNER = genRunner();
    private static final BufferedImage PROC_TANK   = genTank();
    private static final BufferedImage PROC_BOSS   = genBoss();

    static {
        SHIP_NORMAL = load("/images/aliens/shipGreen_manned.png");
        SHIP_RUNNER = load("/images/aliens/shipYellow_manned.png");
        SHIP_TANK   = load("/images/aliens/shipBeige_manned.png");
        BOSS_FULL   = load("/images/aliens/shipPink_manned.png");
        BOSS_DMG1   = load("/images/aliens/shipPink_damage.png");
        BOSS_DMG2   = load("/images/aliens/shipPink_damage1.png");
    }

    private static BufferedImage load(String path) {
        try {
            InputStream is = CacheSpritesAliens.class.getResourceAsStream(path);
            if (is == null) return null;
            return ImageIO.read(is);
        } catch (Exception e) {
            System.err.println("Alien sprite manquant : " + path);
            return null;
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public static BufferedImage get(com.fermedefense.modele.combat.Extraterrestre.TypeAlien type) {
        return switch (type) {
            case RUNNER -> SHIP_RUNNER != null ? SHIP_RUNNER : PROC_RUNNER;
            case TANK   -> SHIP_TANK   != null ? SHIP_TANK   : PROC_TANK;
            default     -> SHIP_NORMAL != null ? SHIP_NORMAL : PROC_NORMAL;
        };
    }

    /** Boss à HP plein (rétrocompatibilité). */
    public static BufferedImage getBoss() {
        return getBoss(1.0);
    }

    /**
     * Sprite boss en fonction du ratio de PV restants (0.0–1.0).
     * Affiche une variante endommagée quand le boss est bas en HP.
     */
    public static BufferedImage getBoss(double hpRatio) {
        if (hpRatio > 0.66) return BOSS_FULL  != null ? BOSS_FULL  : PROC_BOSS;
        if (hpRatio > 0.33) return BOSS_DMG1  != null ? BOSS_DMG1  : PROC_BOSS;
        return                     BOSS_DMG2  != null ? BOSS_DMG2  : PROC_BOSS;
    }

    // ── Procedural sprite generators (fallback) ──────────────────────────────

    private static BufferedImage genNormal() {
        int W = 32, H = 40;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = setup(img);
        g.setColor(new Color(30, 140, 40));
        g.setStroke(new BasicStroke(2));
        g.drawLine(10, 4, 6, 0);   g.fillOval(4, -2, 5, 5);
        g.drawLine(22, 4, 26, 0);  g.fillOval(23, -2, 5, 5);
        GradientPaint gp = new GradientPaint(0, 4, new Color(70, 220, 80), W, H, new Color(30, 140, 40));
        g.setPaint(gp);
        g.fillOval(2, 4, W - 4, H - 8);
        g.setColor(new Color(20, 100, 25));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(2, 4, W - 4, H - 8);
        drawEye(g, 7, 12, 7, false);
        drawEye(g, 18, 12, 7, false);
        drawLegs(g, W, H - 6, 3, new Color(30, 120, 35));
        g.dispose();
        return img;
    }

    private static BufferedImage genRunner() {
        int W = 24, H = 42;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = setup(img);
        g.setColor(new Color(180, 110, 0));
        g.setStroke(new BasicStroke(2));
        g.drawLine(W / 2, 4, W / 2, 0);
        g.fillOval(W / 2 - 3, -2, 6, 6);
        GradientPaint gp = new GradientPaint(0, 4, new Color(255, 210, 50), W, H, new Color(200, 130, 10));
        g.setPaint(gp);
        g.fillOval(2, 4, W - 4, H - 6);
        g.setColor(new Color(160, 100, 0));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(2, 4, W - 4, H - 6);
        g.setColor(Color.WHITE);
        g.fillOval(4, 11, 7, 5);
        g.fillOval(13, 11, 7, 5);
        g.setColor(Color.BLACK);
        g.fillOval(6, 12, 3, 3);
        g.fillOval(15, 12, 3, 3);
        drawLegs(g, W, H - 4, 5, new Color(180, 120, 0));
        g.dispose();
        return img;
    }

    private static BufferedImage genTank() {
        int W = 44, H = 38;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = setup(img);
        g.setColor(new Color(50, 50, 180, 100));
        g.fillRoundRect(0, 2, W, H - 2, 12, 12);
        GradientPaint gp = new GradientPaint(0, 4, new Color(100, 100, 240), W, H, new Color(40, 40, 160));
        g.setPaint(gp);
        g.fillRoundRect(3, 5, W - 6, H - 8, 10, 10);
        g.setColor(new Color(30, 30, 130));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(3, 5, W - 6, H - 8, 10, 10);
        g.setColor(new Color(30, 30, 130));
        g.setStroke(new BasicStroke(3));
        g.drawLine(12, 5, 9, 0);   g.fillOval(6, -2, 7, 7);
        g.drawLine(32, 5, 35, 0);  g.fillOval(31, -2, 7, 7);
        drawEye(g, 11, 12, 8, false);
        drawEye(g, 25, 12, 8, false);
        g.setColor(new Color(80, 80, 200, 140));
        g.fillRoundRect(16, 22, 12, 8, 4, 4);
        drawLegs(g, W, H - 3, 3, new Color(30, 30, 130));
        g.dispose();
        return img;
    }

    private static BufferedImage genBoss() {
        int W = 64, H = 80;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = setup(img);
        g.setColor(new Color(200, 0, 0, 60));
        g.fillOval(-4, 0, W + 8, H);
        g.setColor(new Color(220, 160, 0));
        int[] cx = {8, 14, W / 2, W - 14, W - 8};
        int[] cyTop = {10, 2, 8, 2, 10};
        int[] cyBot = {16, 16, 16, 16, 16};
        g.fillPolygon(cx, cyBot, 5);
        for (int i = 0; i < 5; i++) g.drawLine(cx[i], cyTop[i], cx[i], cyBot[i]);
        g.setColor(new Color(255, 200, 50));
        for (int i = 0; i < 5; i++) g.fillOval(cx[i] - 3, cyTop[i] - 3, 6, 6);
        GradientPaint gp = new GradientPaint(0, 14, new Color(210, 40, 40), W, H, new Color(100, 10, 10));
        g.setPaint(gp);
        g.fillOval(3, 14, W - 6, H - 18);
        g.setColor(new Color(80, 5, 5));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(3, 14, W - 6, H - 18);
        g.setColor(new Color(100, 5, 5));
        g.setStroke(new BasicStroke(3));
        g.drawLine(16, 16, 10, 6);  g.fillOval(7, 3, 8, 8);
        g.drawLine(48, 16, 54, 6);  g.fillOval(50, 3, 8, 8);
        drawEye(g, 16, 26, 11, true);
        drawEye(g, 37, 26, 11, true);
        g.setColor(new Color(60, 5, 5));
        g.setStroke(new BasicStroke(2));
        g.drawLine(W / 2 - 4, 46, W / 2 + 2, 56);
        g.drawLine(W / 2 - 2, 48, W / 2 + 4, 50);
        drawLegs(g, W, H - 8, 4, new Color(90, 5, 5));
        g.dispose();
        return img;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Graphics2D setup(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        return g;
    }

    private static void drawEye(Graphics2D g, int x, int y, int r, boolean glowing) {
        if (glowing) {
            g.setColor(new Color(255, 60, 60, 120));
            g.fillOval(x - 3, y - 3, r + 6, r + 6);
            g.setColor(new Color(255, 100, 100));
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillOval(x, y, r, r);
        int p = Math.max(2, r / 3);
        g.setColor(Color.BLACK);
        g.fillOval(x + (r - p) / 2, y + (r - p) / 2, p, p);
        if (glowing) {
            g.setColor(new Color(255, 200, 200, 180));
            g.fillOval(x + 1, y + 1, p - 1, p - 1);
        }
    }

    private static void drawLegs(Graphics2D g, int W, int baseY, int count, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        int step = W / (count + 1);
        for (int i = 1; i <= count; i++) {
            int lx = i * step;
            g.drawLine(lx, baseY, lx + (i % 2 == 0 ? -5 : 5), baseY + 7);
        }
    }
}
