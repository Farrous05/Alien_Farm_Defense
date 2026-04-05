package com.fermedefense.vue;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Affiche des textes flottants temporaires (popup "+Xg", dégâts, etc.)
 * qui montent progressivement et s'effacent.
 */
public class VueEffetTexte {

    private static final long DUREE_MS = 900;
    private static final int MONTEE_PX = 35;
    private static final Font FONT = new Font("SansSerif", Font.BOLD, 14);

    private record TexteFlottant(String texte, int x, int y, Color couleur, long startMs) {}

    private final List<TexteFlottant> effets = new ArrayList<>();

    /** Déclenche un texte flottant centré sur (cx, cy). */
    public void trigger(String texte, int cx, int cy, Color couleur) {
        effets.add(new TexteFlottant(texte, cx, cy, couleur, System.currentTimeMillis()));
    }

    /** Déclenche un popup de monnaie standard "+Xg" en or. */
    public void triggerMonnaie(int montant, int cx, int cy) {
        trigger("+" + montant + "g", cx, cy, new Color(255, 215, 0));
    }

    /** Déclenche un popup de dégâts en rouge. */
    public void triggerDegats(int montant, int cx, int cy) {
        trigger("-" + montant, cx, cy, new Color(255, 80, 80));
    }

    /** Dessine tous les textes actifs et supprime les expirés. */
    public void draw(Graphics2D g2) {
        long now = System.currentTimeMillis();
        Iterator<TexteFlottant> it = effets.iterator();
        while (it.hasNext()) {
            TexteFlottant e = it.next();
            long elapsed = now - e.startMs();
            if (elapsed >= DUREE_MS) {
                it.remove();
                continue;
            }
            float ratio = (float) elapsed / DUREE_MS;
            float alpha = 1.0f - ratio * ratio; // ease out fade
            int dy = (int)(MONTEE_PX * ratio);

            g2.setFont(FONT);
            FontMetrics fm = g2.getFontMetrics();
            int tx = e.x() - fm.stringWidth(e.texte()) / 2;
            int ty = e.y() - dy;

            // Ombre
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.6f));
            g2.setColor(Color.BLACK);
            g2.drawString(e.texte(), tx + 1, ty + 1);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(e.couleur());
            g2.drawString(e.texte(), tx, ty);
            g2.setComposite(old);
        }
    }
}
