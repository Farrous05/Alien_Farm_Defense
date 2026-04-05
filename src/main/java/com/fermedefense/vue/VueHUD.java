package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Partie;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.utilitaire.Constantes;

/**
 * Affichage tête haute : PV, monnaie, vaches, arme équipée, niveau, score.
 * Barre horizontale en haut de la fenêtre.
 * Utilise les sprites PNG existants pour l'arme et les vaches productives.
 */
public class VueHUD extends JPanel {

    private final Joueur joueur;
    private final Ferme ferme;
    private final Partie partie;

    // Sprites armes (réutilisés depuis les ressources existantes)
    private static Image sprEpee;
    private static Image sprShotgun;
    private static Image sprMinigun;
    // Sprite vache productive pour l'icône de ferme
    private static Image sprVacheProductive;

    static {
        try {
            sprEpee            = ImageIO.read(VueHUD.class.getResource("/images/spr_epee.png"));
            sprShotgun         = ImageIO.read(VueHUD.class.getResource("/images/spr_shotgun.png"));
            sprMinigun         = ImageIO.read(VueHUD.class.getResource("/images/spr_minigun.png"));
            sprVacheProductive = ImageIO.read(VueHUD.class.getResource("/images/vache_productive.png"));
        } catch (Exception e) {
            System.err.println("VueHUD: erreur chargement sprites — " + e.getMessage());
        }
    }

    public VueHUD(Joueur joueur, Ferme ferme, Partie partie) {
        this.joueur = joueur;
        this.ferme  = ferme;
        this.partie = partie;
        setPreferredSize(new Dimension(Constantes.LARGEUR_FENETRE, 50));
        setBackground(new Color(18, 20, 36));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 32;   // baseline text

        // ── PV ─────────────────────────────────────────────────────────────
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("PV", 10, y);

        int barX = 36, barW = 130, barH = 16;
        double ratio = (double) joueur.getPointsDeVie() / joueur.getPointsDeVieMax();
        g2.setColor(new Color(40, 40, 40));
        g2.fillRoundRect(barX, y - 13, barW, barH, 6, 6);
        Color barColor = ratio > 0.5 ? new Color(50, 200, 50)
                       : ratio > 0.25 ? new Color(240, 160, 30)
                       : new Color(220, 50, 50);
        g2.setColor(barColor);
        g2.fillRoundRect(barX, y - 13, (int)(barW * ratio), barH, 6, 6);
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(barX, y - 13, barW, barH, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(joueur.getPointsDeVie() + "/" + joueur.getPointsDeVieMax(),
                barX + barW + 5, y);

        // ── Monnaie ────────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(255, 210, 50));
        g2.drawString("\u2B50 " + joueur.getMonnaie(), 290, y);

        // ── Vaches (avec sprite PNG) ───────────────────────────────────────
        int vachesX = 400;
        if (sprVacheProductive != null) {
            g2.drawImage(sprVacheProductive, vachesX, y - 16, 20, 20, null);
            vachesX += 24;
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.drawString(ferme.getNombreAnimaux() + "/" + ferme.getCapaciteMax()
                + "  prod:" + ferme.getNombreProductives(), vachesX, y);

        // ── Arme équipée (sprite PNG) ──────────────────────────────────────
        Arme arme = joueur.getArmeEquipee();
        int armeX = 580;
        g2.setColor(new Color(180, 180, 200));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Arme:", armeX, y);
        armeX += 48;
        if (arme != null) {
            Image sprArme = spriteArme(arme);
            if (sprArme != null) {
                g2.drawImage(sprArme, armeX, y - 16, 20, 20, null);
                armeX += 24;
            }
            g2.setColor(new Color(220, 220, 100));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(arme.getNom(), armeX, y);
        } else {
            g2.setColor(new Color(140, 140, 140));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g2.drawString("Aucune", armeX, y);
        }

        // ── Niveau ─────────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(140, 200, 255));
        g2.drawString("Niv." + partie.getNiveau(), 780, y);

        // ── Score ──────────────────────────────────────────────────────────
        g2.setColor(new Color(180, 255, 160));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Score:" + partie.getScoreCumule(), 850, y);
    }

    private Image spriteArme(Arme arme) {
        if (arme == Arme.EPEE)    return sprEpee;
        if (arme == Arme.SHOTGUN) return sprShotgun;
        if (arme == Arme.MINIGUN) return sprMinigun;
        // Rayon laser (ControleurMarche.RAYON_LASER) ou arme custom — pas de sprite dédié
        return sprEpee; // fallback
    }
}
