package com.fermedefense.modele.jeu;

/**
 * Suit les statistiques de combat et calcule le score d'un niveau.
 *
 * Formule :
 *   +100 pts par alien éliminé
 *   +300 pts par vague gagnée
 *   +2000 × niveau pts si le boss est vaincu
 *   -150 pts par vache perdue
 *   +1 pt tous les 5 points de dégâts infligés
 *
 * Classe modèle pure — mise à jour par ControleurJeu après chaque vague/boss.
 */
public class ScorePartie {

    private int aliensElimines;
    private int vaguesGagnees;
    private int vachesPerdues;
    private boolean bossVaincu;
    private int totalDegatsInfliges;
    private final int niveau;

    public ScorePartie(int niveau) {
        this.niveau = niveau;
    }

    /**
     * Enregistre une vague gagnée (tous les aliens vaincus par le joueur).
     */
    public void enregistrerVagueGagnee(int nbAliensVaincus, int degatsInfliges) {
        vaguesGagnees++;
        aliensElimines += nbAliensVaincus;
        totalDegatsInfliges += degatsInfliges;
    }

    /**
     * Enregistre une vache enlevée par les aliens.
     */
    public void enregistrerVachePerdue() {
        vachesPerdues++;
    }

    /**
     * Enregistre la défaite du boss final.
     */
    public void enregistrerBossVaincu(int degatsInfliges) {
        bossVaincu = true;
        totalDegatsInfliges += degatsInfliges;
    }

    /**
     * Calcule et retourne le score total de ce niveau.
     */
    public int calculerScore() {
        int score = 0;
        score += aliensElimines * 100;
        score += vaguesGagnees * 300;
        score += bossVaincu ? 2000 * niveau : 0;
        score -= vachesPerdues * 150;
        score += totalDegatsInfliges / 5;
        return Math.max(0, score);
    }

    // --- Getters ---

    public int getAliensElimines() { return aliensElimines; }
    public int getVaguesGagnees() { return vaguesGagnees; }
    public int getVachesPerdues() { return vachesPerdues; }
    public boolean isBossVaincu() { return bossVaincu; }
    public int getTotalDegatsInfliges() { return totalDegatsInfliges; }
    public int getNiveau() { return niveau; }
}
