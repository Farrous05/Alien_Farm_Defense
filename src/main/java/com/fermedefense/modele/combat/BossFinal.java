package com.fermedefense.modele.combat;

/**
 * Représente le boss final extraterrestre.
 *
 * Hérite d'Extraterrestre avec des stats plus élevées
 * et un bonus de récompense en monnaie.
 */
public class BossFinal extends Extraterrestre {

    private final int recompense; // monnaie gagnée si le boss est vaincu

    /**
     * Crée un boss final.
     *
     * @param nom           nom du boss
     * @param pointsDeVieMax PV max
     * @param degats        dégâts par coup
     * @param cooldownMs    intervalle entre les coups (ms)
     * @param recompense    monnaie obtenue après victoire
     */
    public BossFinal(String nom, int pointsDeVieMax, int degats, long cooldownMs, int recompense) {
        super(nom, pointsDeVieMax, degats, cooldownMs);
        this.recompense = recompense;
    }

    /**
     * Crée un boss par défaut pour le niveau donné.
     * Les stats augmentent avec le niveau.
     */
    public static BossFinal pourNiveau(int niveau) {
        int pv = 80 + niveau * 40;
        int deg = 8 + niveau * 4;
        long cd = Math.max(600, 1200 - niveau * 100L);
        int recomp = 100 + niveau * 50;
        return new BossFinal("Boss Alien Nv." + niveau, pv, deg, cd, recomp);
    }

    public int getRecompense() {
        return recompense;
    }

    @Override
    public String toString() {
        return "BOSS " + super.toString() + " récompense=" + recompense;
    }
}
