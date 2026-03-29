package com.fermedefense;

import javax.swing.SwingUtilities;

import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.jeu.Partie;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.utilitaire.Constantes;
import com.fermedefense.vue.VueMenuPrincipal;
import com.fermedefense.vue.VuePrincipale;

/**
 * Point d'entrée principal du jeu Alien Farm Defense.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VueMenuPrincipal().setVisible(true);
        });
    }

    public static void lancerJeu() {
        SwingUtilities.invokeLater(() -> {
            // Modèle
            Carte carte = new Carte(Constantes.LARGEUR_CARTE, Constantes.HAUTEUR_CARTE);
            Joueur joueur = new Joueur(
                    carte.getCentreZone(com.fermedefense.modele.jeu.Zone.FERME)[0],
                    carte.getCentreZone(com.fermedefense.modele.jeu.Zone.FERME)[1],
                    Constantes.VITESSE_JOUEUR,
                    Constantes.PV_MAX_JOUEUR,
                    Constantes.MONNAIE_INIT
            );
            joueur.getInventaire().ajouterObjet(com.fermedefense.modele.combat.Arme.EPEE);
            Ferme ferme = new Ferme();
            Marche marche = new Marche();

            // Partie (progression)
            Partie partie = new Partie(
                    Constantes.LARGEUR_CARTE, Constantes.HAUTEUR_CARTE,
                    Constantes.TEMPS_NIVEAU_MS,
                    Constantes.MONNAIE_INIT
            );

            // Vue + lancement
            VuePrincipale vue = new VuePrincipale(joueur, ferme, carte, marche, partie);
            vue.lancer();
        });
    }
}
