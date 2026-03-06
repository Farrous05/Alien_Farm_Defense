package com.fermedefense.controleur;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.combat.BossFinal;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.Niveau;

/**
 * Gère la logique du combat final (boss) en fin de niveau.
 *
 * Quand la BarreProgression atteint 100 %, le ControleurJeu
 * passe le relais à ce contrôleur. Il :
 *   1. Crée le BossFinal via le Niveau
 *   2. Fait tourner l'Attaque tick par tick
 *   3. Signale le résultat : VICTOIRE (→ niveau suivant) ou DEFAITE
 *
 * Le combat final est similaire aux vagues intermédiaires
 * mais contre un seul ennemi plus puissant, avec une récompense.
 */
public class ControleurCombat {

    private final Niveau niveau;
    private final Arme arme;
    private BossFinal boss;
    private Attaque attaqueBoss;
    private boolean actif;
    private boolean termine;
    private ResultatCombat resultat;

    public ControleurCombat(Niveau niveau, Arme arme) {
        this.niveau = niveau;
        this.arme = arme;
        this.actif = false;
        this.termine = false;
        this.resultat = null;
    }

    /**
     * Lance le combat final contre le boss du niveau.
     */
    public void lancerCombatFinal() {
        boss = niveau.creerBoss();
        attaqueBoss = new Attaque(boss);
        actif = true;
        termine = false;
        resultat = null;
    }

    /**
     * Met à jour le combat du boss.
     *
     * @param deltaMs temps écoulé
     * @param joueur  le joueur
     */
    public void mettreAJour(long deltaMs, Joueur joueur) {
        if (!actif || attaqueBoss == null) return;

        attaqueBoss.mettreAJour(deltaMs, joueur, arme);

        if (attaqueBoss.isTerminee()) {
            actif = false;
            termine = true;
            resultat = attaqueBoss.getResultat();

            // Si victoire, accorder la récompense
            if (resultat == ResultatCombat.VICTOIRE) {
                joueur.ajouterMonnaie(boss.getRecompense());
            }
        }
    }

    // --- Accesseurs ---

    public boolean isActif() { return actif; }
    public boolean isTermine() { return termine; }
    public ResultatCombat getResultat() { return resultat; }
    public BossFinal getBoss() { return boss; }
    public Attaque getAttaqueBoss() { return attaqueBoss; }
}
