package com.fermedefense.modele.combat;

/**
 * Représente le boss final extraterrestre.
 *
 * Hérite d'Extraterrestre avec des stats plus élevées
 * et un bonus de récompense en monnaie.
 */
public class BossFinal extends Extraterrestre {

    private final int recompense; // monnaie gagnée si le boss est vaincu
    private boolean phase2Activee = false;
    private static final double MULTIPLICATEUR_ENRAGE = 1.75;

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
        super(nom, pointsDeVieMax, degats, cooldownMs, Long.MAX_VALUE);
        this.recompense = recompense;
    }

    /**
     * Crée un boss par défaut pour le niveau donné.
     * Les stats augmentent avec le niveau.
     */
    public static BossFinal pourNiveau(int niveau) {
        int pv = 80 + niveau * 40;
        int deg = 7 + niveau * 3;
        long cd = Math.max(600, 1200 - niveau * 100L);
        int recomp = 100 + niveau * 50;
        return new BossFinal("Boss Alien Nv." + niveau, pv, deg, cd, recomp);
    }

    public void enrager() {
        this.phase2Activee = true;
    }

    public boolean isPhase2Activee() {
        return phase2Activee;
    }

    @Override
    public int getDegats() {
        if (phase2Activee) {
            return (int) Math.round(super.getDegats() * MULTIPLICATEUR_ENRAGE);
        }
        return super.getDegats();
    }

    public int getRecompense() {
        return recompense;
    }

    @Override
    public String toString() {
        return "BOSS " + super.toString() + " récompense=" + recompense;
    }
}
