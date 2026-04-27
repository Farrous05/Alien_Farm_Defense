package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.imageio.ImageIO;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.Bombe;
import com.fermedefense.modele.ferme.Vache;
import com.fermedefense.modele.joueur.Inventaire;
import com.fermedefense.modele.joueur.ObjetInventaire;
import com.fermedefense.modele.joueur.Potion;

public class VueInventaire {
    
    private static Image imgEpee;
    private static Image imgShotgun;
    private static Image imgMinigun;
    private static Image imgRayonLaser;
    private static Image imgVache;
    private static Image imgPotion;
    private static Image imgBombe;

    static {
        try {
            imgEpee = ImageIO.read(VueInventaire.class.getResource("/images/spr_epee.png"));
            imgShotgun = ImageIO.read(VueInventaire.class.getResource("/images/spr_shotgun.png"));
            imgMinigun = ImageIO.read(VueInventaire.class.getResource("/images/spr_minigun.png"));
            imgRayonLaser = ImageIO.read(VueInventaire.class.getResource("/images/effects/laserGreen_burst.png"));
            imgVache = ImageIO.read(VueInventaire.class.getResource("/images/vache_bebe.png"));
            imgPotion = ImageIO.read(VueInventaire.class.getResource("/images/market/potion_red.png"));
            imgBombe = ImageIO.read(VueInventaire.class.getResource("/images/market/chest.png"));
        } catch (Exception e) {
            System.err.println("Erreur chargement images inventaire : " + e.getMessage());
        }
    }

    private final com.fermedefense.modele.joueur.Joueur joueur;
    private final Inventaire inventaire;
    private final int x;
    private final int y;
    private final int tailleCase = 40;
    
    private int selectionLigne = -1;
    private int selectionColonne = -1;

    public VueInventaire(com.fermedefense.modele.joueur.Joueur joueur, int x, int y) {
        this.joueur = joueur;
        this.inventaire = joueur.getInventaire();
        this.x = x;
        this.y = y;
    }

    public void dessiner(Graphics2D g2) {
        // Fond
        g2.setColor(new Color(50, 50, 50, 200));
        int width = inventaire.getColonnes() * tailleCase;
        int height = inventaire.getLignes() * tailleCase;
        g2.fillRect(x, y, width, height);

        // Titre
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Inventaire", x, y - 5);

        // Grille et Objets
        for (int i = 0; i < inventaire.getLignes(); i++) {
            for (int j = 0; j < inventaire.getColonnes(); j++) {
                int cx = x + j * tailleCase;
                int cy = y + i * tailleCase;
                
                // Dessiner la case
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(cx, cy, tailleCase, tailleCase);
                
                // Si sélectionnée
                if (i == selectionLigne && j == selectionColonne) {
                    g2.setColor(new Color(255, 255, 0, 100));
                    g2.fillRect(cx + 1, cy + 1, tailleCase - 1, tailleCase - 1);
                }

                // Dessiner l'objet
                ObjetInventaire obj = inventaire.getObjet(i, j);
                if (obj != null) {
                    if (obj == joueur.getArmeEquipee()) {
                        g2.setColor(new Color(255, 150, 0, 150));
                        g2.fillRect(cx + 1, cy + 1, tailleCase - 1, tailleCase - 1);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                        g2.drawString("E", cx + 2, cy + 10);
                    }
                    
                    Image spr = getSpriteObjet(obj);
                    
                    if (spr != null) {
                        g2.drawImage(spr, cx + 2, cy + 2, tailleCase - 4, tailleCase - 4, null);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                        g2.drawString(obj.getNom().substring(0, 1).toUpperCase(), cx + 14, cy + 25);
                    }
                }
            }
        }
    }

    private Image getSpriteObjet(ObjetInventaire obj) {
        if (obj instanceof Vache) {
            return imgVache;
        }
        if (obj instanceof Potion) {
            return imgPotion;
        }
        if (obj instanceof Bombe) {
            return imgBombe;
        }
        if (obj instanceof Arme arme) {
            String nom = arme.getNom().toLowerCase();
            if (nom.contains("ep") || nom.contains("ép")) return imgEpee;
            if (nom.contains("shotgun")) return imgShotgun;
            if (nom.contains("minigun")) return imgMinigun;
            if (nom.contains("rayon") || nom.contains("laser")) return imgRayonLaser;
        }
        return null;
    }
    
    public void setSelection(int ligne, int colonne) {
        this.selectionLigne = ligne;
        this.selectionColonne = colonne;
    }
    
    public int getSelectionLigne() { return selectionLigne; }
    public int getSelectionColonne() { return selectionColonne; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getTailleCase() { return tailleCase; }
}
