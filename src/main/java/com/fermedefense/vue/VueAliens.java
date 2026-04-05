package com.fermedefense.vue;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import com.fermedefense.modele.combat.AlienVisuel;
import com.fermedefense.modele.combat.Extraterrestre;

/**
 * Dessine les aliens visuels (vagues et boss) en utilisant les sprites
 * chargés par CacheSpritesAliens (PNG Kenney ou dessinés en fallback).
 *
 * Animations :
 *  - Bob sinusoïdal vertical sur tous les aliens (look «flottement UFO»)
 *  - Flash blanc lors d'un coup reçu
 *  - Halo pulsant rouge pour le boss
 *  - Sprite boss qui change selon les PV restants (full / damage1 / damage2)
 */
public class VueAliens {

    /** Amplitude du bob vertical (pixels). */
    private static final int BOB_AMP = 5;
    /** Fréquence du bob (radians/ms). */
    private static final double BOB_FREQ = 0.003;

    // ── Tailles d'affichage ──────────────────────────────────────────────────
    private static final int W_NORMAL = 64;
    private static final int H_NORMAL = 64;
    private static final int W_RUNNER = 52;
    private static final int H_RUNNER = 52;
    private static final int W_TANK   = 72;
    private static final int H_TANK   = 72;
    private static final int W_BOSS   = 96;
    private static final int H_BOSS   = 96;

    // ── API publique ─────────────────────────────────────────────────────────

    /** Dessine une liste d'aliens (sans caméra, coordonnées déjà écran). */
    public void dessiner(Graphics2D g2, List<AlienVisuel> aliens, boolean boss) {
        dessiner(g2, aliens, boss, 1.0, null);
    }

    /** Dessine une liste d'aliens (sans caméra). */
    public void dessiner(Graphics2D g2, List<AlienVisuel> aliens, boolean boss, double hpRatio) {
        dessiner(g2, aliens, boss, hpRatio, null);
    }

    /**
     * Dessine une liste d'aliens en traduisant les coordonnées monde via caméra.
     *
     * @param camera  caméra courante (null = coordonnées déjà en espace écran)
     * @param hpRatio ratio PV du boss
     */
    public void dessiner(Graphics2D g2, List<AlienVisuel> aliens, boolean boss,
                         double hpRatio, Camera camera) {
        if (aliens == null || aliens.isEmpty()) return;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        long now = System.currentTimeMillis();
        for (AlienVisuel av : aliens) {
            dessinerAlien(g2, av, boss, hpRatio, now, camera);
        }
    }

    // ── Rendu d'un alien ─────────────────────────────────────────────────────

    private void dessinerAlien(Graphics2D g2, AlienVisuel av, boolean boss,
                               double hpRatio, long now, Camera camera) {
        BufferedImage sprite = boss
                ? CacheSpritesAliens.getBoss(hpRatio)
                : CacheSpritesAliens.get(av.getType());

        int drawW = boss ? W_BOSS : displayWidth(av.getType());
        int drawH = boss ? H_BOSS : displayHeight(av.getType());

        int ax = (camera != null)
                ? camera.toScreenX(av.getX() + av.getOffsetCombat())
                : (int)(av.getX() + av.getOffsetCombat());
        int bob = (int)(BOB_AMP * Math.sin(now * BOB_FREQ + av.getX() * 0.05));
        int ay = (camera != null)
                ? camera.toScreenY(av.getY()) - bob
                : (int) av.getY() - bob;

        // Halo pulsant pour le boss
        if (boss) {
            float alpha = 0.20f + 0.12f * (float) Math.sin(now * 0.005);
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            // Halo plus intense quand endommagé
            Color haloColor = hpRatio < 0.33
                    ? new Color(255, 80, 0)   // orange-rouge critique
                    : new Color(255, 0, 0);
            g2.setColor(haloColor);
            g2.fillOval(ax - 12, ay - 12, drawW + 24, drawH + 24);
            g2.setComposite(old);
        }

        g2.drawImage(sprite, ax, ay, drawW, drawH, null);

        // Flash blanc au coup
        boolean hitFlash = av.getEtat() == AlienVisuel.EtatVisuel.COMBAT
                && av.getTempsEtat() % 500 < 80;
        if (hitFlash) {
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(ax, ay, drawW, drawH, 8, 8);
            g2.setComposite(old);
        }

        // Icône vache enlevée
        if (av.getEtat() == AlienVisuel.EtatVisuel.ENLEVEMENT) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, boss ? 18 : 12));
            g2.drawString("\uD83D\uDC04", ax + drawW / 2 - 6, ay + drawH + (boss ? 18 : 12));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int displayWidth(Extraterrestre.TypeAlien type) {
        return switch (type) {
            case RUNNER -> W_RUNNER;
            case TANK   -> W_TANK;
            default     -> W_NORMAL;
        };
    }

    private int displayHeight(Extraterrestre.TypeAlien type) {
        return switch (type) {
            case RUNNER -> H_RUNNER;
            case TANK   -> H_TANK;
            default     -> H_NORMAL;
        };
    }
}
