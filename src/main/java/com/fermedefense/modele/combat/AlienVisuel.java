package com.fermedefense.modele.combat;

/**
 * Représentation visuelle d'un alien sur la carte.
 *
 * Gère la position, le mouvement (approche de droite vers la ferme),
 * et l'état visuel (approche, combat, fuite).
 *
 * Classe modèle pure — utilisée uniquement pour le rendu.
 */
public class AlienVisuel {

    public enum EtatVisuel {
        APPROCHE,   // marche du bord droit vers la ferme
        COMBAT,     // en train de se battre (sur place, secoue)
        FUITE,      // s'enfuit vers la droite (victoire joueur)
        ENLEVEMENT  // repart avec une vache (défaite joueur)
    }

    private double x, y;
    private final double cibleX, cibleY;
    private final double departX;
    private EtatVisuel etat;
    private final double vitesse; // pixels par seconde
    private final Extraterrestre.TypeAlien type;

    /** Temps accumulé dans l'état courant (pour animations). */
    private long tempsEtat;

    public AlienVisuel(double departX, double departY, double cibleX, double cibleY, double vitesse) {
        this(departX, departY, cibleX, cibleY, vitesse, Extraterrestre.TypeAlien.NORMAL);
    }

    public AlienVisuel(double departX, double departY, double cibleX, double cibleY, double vitesse, Extraterrestre.TypeAlien type) {
        this.x = departX;
        this.y = departY;
        this.departX = departX;
        this.cibleX = cibleX;
        this.cibleY = cibleY;
        this.vitesse = vitesse;
        this.type = type;
        this.etat = EtatVisuel.APPROCHE;
        this.tempsEtat = 0;
    }

    /**
     * Met à jour la position de l'alien.
     *
     * @param deltaMs temps écoulé
     * @return true si l'alien a atteint sa cible dans la phase courante
     */
    public boolean mettreAJour(long deltaMs) {
        tempsEtat += deltaMs;
        double deplacement = vitesse * deltaMs / 1000.0;

        switch (etat) {
            case APPROCHE:
                // Se déplace vers la cible (gauche)
                if (x > cibleX) {
                    x -= deplacement;
                    // Ajuster Y progressivement
                    double ratio = 1.0 - (x - cibleX) / (departX - cibleX);
                    y = lerp(y, cibleY, Math.min(0.05, ratio * 0.1));
                    if (x <= cibleX) {
                        x = cibleX;
                        return true; // arrivé
                    }
                } else {
                    return true;
                }
                break;

            case COMBAT:
                // Légère oscillation sur place
                break;

            case FUITE:
                // Se déplace vers la droite (hors écran)
                x += deplacement;
                if (x > departX + 50) return true; // sorti
                break;

            case ENLEVEMENT:
                // Repart vers la droite mais plus lentement
                x += deplacement * 0.6;
                if (x > departX + 50) return true;
                break;
        }
        return false;
    }

    public void setEtat(EtatVisuel etat) {
        this.etat = etat;
        this.tempsEtat = 0;
    }

    /** Petit tremblement pendant le combat. */
    public double getOffsetCombat() {
        if (etat != EtatVisuel.COMBAT) return 0;
        return Math.sin(tempsEtat * 0.01) * 3;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // --- Accesseurs ---
    public double getX() { return x; }
    public double getY() { return y; }
    public EtatVisuel getEtat() { return etat; }
    public long getTempsEtat() { return tempsEtat; }
    public Extraterrestre.TypeAlien getType() { return type; }
}
