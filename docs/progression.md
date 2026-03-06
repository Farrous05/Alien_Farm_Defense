# Progression — Niveaux, Barre de temps, Vagues

## Fonctionnalité

Le module de progression gère le flux complet d'un niveau de jeu :

1. **Barre de temps** — compte à rebours de 2+ minutes
2. **Vagues intermédiaires** — attaques d'aliens déclenchées à des moments programmés
3. **Combat final** — boss en fin de niveau
4. **Passage au niveau suivant** — si le joueur survit

## Classes

### `Niveau` (modele/progression/)
Définit les paramètres d'un niveau à partir de son numéro (1, 2, 3…) :
- **Durée** : 120s + 15s par niveau
- **Nombre de vagues** : 2 au niv.1, +1/niv, max 6
- **Aliens par vague** : 1 au niv.1, +1 tous les 2 niveaux
- **Stats aliens** : PV 20+(n-1)×10, dég 5+(n-1)×2, cd max(600, 1200-(n-1)×80)
- Fabrique les aliens (`creerVague(i)`) et le boss (`creerBoss()`)

### `EvenementTemporel` (modele/progression/)
Un marqueur sur la timeline :
- Moment (ms), type (`ATTAQUE_INTERMEDIAIRE` | `COMBAT_FINAL`), index de vague
- Flag `declenche` pour éviter les doublons

### `BarreProgression` (modele/progression/)
Chronomètre du niveau + détection d'événements :
- Construit automatiquement les `EvenementTemporel` à partir du `Niveau`
- `mettreAJour(deltaMs)` retourne la liste des événements déclenchés pendant ce tick
- Exposition : `getProgression()` (0.0→1.0), `getTempsRestant()`, `isTerminee()`

### `ControleurAttaque` (controleur/)
Gère les vagues intermédiaires :
- `declencherVague(indexVague)` → crée une `Attaque` avec les aliens du `Niveau`
- `mettreAJour(deltaMs, joueur)` → fait avancer le combat automatique
- Pendant une vague, la barre de progression est en pause

### `ControleurCombat` (controleur/)
Gère le combat final (boss) :
- `lancerCombatFinal()` → crée le `BossFinal` + `Attaque`
- `mettreAJour(deltaMs, joueur)` → fait avancer le combat
- Si victoire : récompense en monnaie ajoutée au joueur

### `ControleurJeu` (controleur/) — Mis à jour
Le game loop intègre maintenant la progression :
- `initialiserNiveau(partie)` prépare Niveau + BarreProgression + contrôleurs
- `tickProgression(delta)` orchestre la barre, les vagues, et le boss

## Flux d'un niveau

```
DÉBUT ──► Barre de progression avance
             │
             ├──► 33% : Vague 1 → combat auto → reprise barre
             ├──► 66% : Vague 2 → combat auto → reprise barre
             │
             └──► 100% : Combat Final (boss)
                          │
                    VICTOIRE → monnaie + niveau suivant
                    DÉFAITE  → game over
```

## Difficulté croissante (exemples)

| Niveau | Durée | Vagues | Aliens/vague | Alien PV | Boss PV |
|--------|-------|--------|-------------|----------|---------|
| 1      | 120s  | 2      | 1           | 20       | 120     |
| 2      | 135s  | 3      | 2           | 30       | 160     |
| 3      | 150s  | 4      | 2           | 40       | 200     |
| 5      | 180s  | 6      | 3           | 60       | 280     |

## Tests

- `NiveauTest` — 11 tests : paramètres, scaling, vagues, boss, bornes
- `BarreProgressionTest` — 12 tests : timing, événements, réinitialisation
- `ControleurAttaqueTest` — 6 tests : vagues, victoire, défaite
- `ControleurCombatTest` — 6 tests : boss, victoire, défaite, récompense
