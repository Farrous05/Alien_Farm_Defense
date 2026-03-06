package com.fermedefense.modele.progression;

import java.util.ArrayList;
import java.util.List;

import com.fermedefense.modele.combat.BossFinal;
import com.fermedefense.modele.combat.Extraterrestre;

/**
 * Représente un niveau de jeu avec sa difficulté et ses paramètres.
 *
 * Chaque niveau définit :
 *   - la durée de la phase ferme (avant le boss)
 *   - le nombre de vagues d'attaques intermédiaires
 *   - le nombre d'aliens par vague (croissant)
 *   - les stats des aliens (croissantes avec le niveau)
 *   - le boss final
 *
 * Utilisation :
 *   Niveau niv = new Niveau(1);          // niveau 1
 *   long duree = niv.getDureeMs();       // 120 000 ms
 *   List<Long> moments = niv.getMomentsAttaques(); // quand les vagues arrivent
 *   List<Extraterrestre> vague = niv.creerVague(0); // aliens de la vague 0
 *   BossFinal boss = niv.creerBoss();
 */
public class Niveau {

    private final int numero;
    private final long dureeMs;
    private final int nombreVagues;
    private final int aliensParVague;

    // Stats de base des aliens pour ce niveau
    private final int alienPv;
    private final int alienDegats;
    private final long alienCooldownMs;

    /**
     * Crée un niveau avec des paramètres calculés automatiquement.
     *
     * @param numero numéro du niveau (1, 2, 3…)
     */
    public Niveau(int numero) {
        if (numero < 1) throw new IllegalArgumentException("Le numéro de niveau doit être >= 1");
        this.numero = numero;

        // Durée : 120s pour le niveau 1, +15s par niveau
        this.dureeMs = 120_000L + (numero - 1) * 15_000L;

        // Vagues : 2 au niveau 1, +1 par niveau, max 6
        this.nombreVagues = Math.min(6, 1 + numero);

        // Aliens par vague : 1 au niveau 1, +1 tous les 2 niveaux
        this.aliensParVague = 1 + (numero / 2);

        // Stats aliens croissantes
        this.alienPv = 20 + (numero - 1) * 10;
        this.alienDegats = 5 + (numero - 1) * 2;
        this.alienCooldownMs = Math.max(600, 1200 - (numero - 1) * 80L);
    }

    /**
     * Calcule les moments (en ms) où chaque vague intermédiaire se déclenche.
     * Les vagues sont réparties uniformément sur la durée du niveau.
     */
    public List<Long> getMomentsAttaques() {
        List<Long> moments = new ArrayList<>();
        long intervalle = dureeMs / (nombreVagues + 1);
        for (int i = 1; i <= nombreVagues; i++) {
            moments.add(intervalle * i);
        }
        return moments;
    }

    /**
     * Crée la liste d'aliens pour une vague intermédiaire.
     *
     * @param indexVague index de la vague (0-based)
     * @return liste d'aliens pour cette vague
     */
    public List<Extraterrestre> creerVague(int indexVague) {
        List<Extraterrestre> aliens = new ArrayList<>();
        for (int i = 0; i < aliensParVague; i++) {
            aliens.add(new Extraterrestre(
                    "Alien Nv." + numero + " V" + (indexVague + 1) + "#" + (i + 1),
                    alienPv,
                    alienDegats,
                    alienCooldownMs
            ));
        }
        return aliens;
    }

    /**
     * Crée le boss final pour ce niveau.
     */
    public BossFinal creerBoss() {
        return BossFinal.pourNiveau(numero);
    }

    // --- Accesseurs ---

    public int getNumero() { return numero; }
    public long getDureeMs() { return dureeMs; }
    public int getNombreVagues() { return nombreVagues; }
    public int getAliensParVague() { return aliensParVague; }
    public int getAlienPv() { return alienPv; }
    public int getAlienDegats() { return alienDegats; }
    public long getAlienCooldownMs() { return alienCooldownMs; }

    @Override
    public String toString() {
        return "Niveau " + numero + " [durée=" + (dureeMs / 1000) + "s, vagues="
                + nombreVagues + ", aliens/vague=" + aliensParVague + "]";
    }
}
