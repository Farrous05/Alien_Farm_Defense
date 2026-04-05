package com.fermedefense.vue;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Affiche des effets visuels de coup (hit-effect) à une position donnée.
 *
 * Deux types :
 *  - Coup joueur → alien : burst vert (laserGreen_burst)
 *  - Coup alien → joueur : burst bleu (laserBlue_burst)
 *
 * Les effets durent {@value #DUREE_MS} ms et s'effacent progressivement.
 */
public class VueEffetHit {

    private static final long DUREE_MS = 400;
    /** Taille d'affichage du burst (pixels). */
    private static final int TAILLE = 72;

    private static final BufferedImage BURST_ALIEN  = load("/images/effects/laserGreen_burst.png");
    private static final BufferedImage BURST_JOUEUR = load("/images/effects/laserBlue_burst.png");

    private static BufferedImage load(String path) {
        try {
            InputStream is = VueEffetHit.class.getResourceAsStream(path);
            return is != null ? ImageIO.read(is) : null;
        } catch (Exception e) {
            System.err.println("Effet hit manquant : " + path);
            return null;
        }
    }

    private record HitEffect(int x, int y, long startMs, boolean isAlienHit) {}

    private final List<HitEffect> effets = new ArrayList<>();

    /**
     * Déclenche un effet à la position (cx, cy) — centre de l'effet.
     *
     * @param cx         coordonnée X du centre cible
     * @param cy         coordonnée Y du centre cible
     * @param isAlienHit true si c'est le joueur qui frappe l'alien,
     *                   false si c'est l'alien qui frappe le joueur
     */
    public void trigger(int cx, int cy, boolean isAlienHit) {
        effets.add(new HitEffect(cx - TAILLE / 2, cy - TAILLE / 2,
                System.currentTimeMillis(), isAlienHit));
    }

    /**
     * Dessine tous les effets actifs et supprime les expirés.
     */
    public void draw(Graphics2D g2) {
        long now = System.currentTimeMillis();
        Iterator<HitEffect> it = effets.iterator();
        while (it.hasNext()) {
            HitEffect e = it.next();
            long elapsed = now - e.startMs();
            if (elapsed >= DUREE_MS) {
                it.remove();
                continue;
            }
            float ratio = (float) elapsed / DUREE_MS;          // 0 → 1
            float alpha = 1.0f - ratio;                         // fade out
            // Scale up slightly as it plays
            int taille = (int)(TAILLE * (0.8f + 0.4f * ratio));
            int dx = e.x() - (taille - TAILLE) / 2;
            int dy = e.y() - (taille - TAILLE) / 2;

            BufferedImage img = e.isAlienHit() ? BURST_ALIEN : BURST_JOUEUR;
            if (img != null) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.drawImage(img, dx, dy, taille, taille, null);
                g2.setComposite(old);
            }
        }
    }

    public boolean hasEffets() {
        return !effets.isEmpty();
    }
}
