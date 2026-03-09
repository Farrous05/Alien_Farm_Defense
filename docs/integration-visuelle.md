# Intégration Visuelle — Barre de progression, Combat, Flux de niveau

## Fonctionnalité

Le jeu est maintenant jouable de bout en bout. Au lancement :

1. Le niveau démarre immédiatement (timer en bas de l'écran)
2. Des vagues d'aliens attaquent à intervalles réguliers
3. Le combat est automatique (overlay central avec barres de PV)
4. À la fin du timer, le boss final apparaît
5. Victoire → niveau suivant / Défaite → recommencer

## Composants modifiés / créés

### `AlienVisuel.java` (modele/combat/) — NOUVEAU
Représentation visuelle d'un alien sur la carte :
- **Position** : coordonnées x/y, départ hors écran à droite, cible dans la zone ferme
- **États** : `EtatVisuel` — APPROCHE (marche), COMBAT (tremble), FUITE (s'enfuit), ENLEVEMENT (vole vache)
- **Mouvement** : déplacement par delta-time, ajustement Y par lerp, offset sinusoïdal en combat
- Utilisé par `ControleurAttaque` (liste d'aliens) et `ControleurCombat` (boss unique)

### `VueAliens.java` (vue/) — NOUVEAU
Dessine les aliens visuels (vagues + boss) sur la carte :
- **Corps** : ovale vert avec yeux noirs (24px normal, 40px boss)
- **Combat** : flash rouge toutes les 600ms simulant les coups
- **Enlèvement** : icône vache affichée sous l'alien en fuite

### `VueActionJoueur.java` (vue/) — NOUVEAU
Barre de progression au-dessus du joueur pendant les actions à durée :
- Barre cyan 60×8 px avec label ("Récolte..." / "Achat...")
- Remplissage proportionnel à `ActionDuree.getProgression()`

### `ActionDuree.java` (modele/joueur/) — NOUVEAU
Modèle d'action avec durée et progression :
- `TypeAction` : RECOLTE (2s), ACHAT (1.5s)
- `mettreAJour(deltaMs)` → retourne true à la fin
- `getProgression()` → ratio 0.0 à 1.0
- `getLabel()` → texte affiché pendant l'action

### `VueBarreProgression.java` (vue/) — NOUVEAU
Barre horizontale en bas du panneau de jeu :
- **Remplissage** gauche→droite : bleu (normal), rouge (< 20% restant)
- **Marqueurs triangulaires jaunes** : vagues intermédiaires
- **Losange rouge** : combat final (en bout de barre)
- **Texte central** : temps restant (format `m:ss`)
- Données lues depuis `BarreProgression.getProgression()` et `getEvenements()`

### `VueCombat.java` (vue/) — NOUVEAU
Overlay semi-transparent centré pendant les combats :
- **Titre** : "ATTAQUE ALIEN !" (vert) ou "COMBAT FINAL" (rouge)
- **Barre PV alien** : nom + barre verte avec ratio PV
- **Barre PV joueur** : "Fermier" + barre bleue
- **Icônes** : cercle vert (alien), carré orange (joueur)
- **Flash épée** : animation simple (emoji ⚔ toutes les secondes)
- **Compteur** : "Aliens restants : N"
- **Résultat** : VICTOIRE ! (vert) ou DÉFAITE... (rouge) quand terminé

### `VuePrincipale.java` (vue/) — MIS À JOUR
Intègre la progression, les aliens visuels et les actions à durée :
- `paintComponent` dessine :
  - Barre de progression (via `VueBarreProgression`)
  - **Aliens visuels** (via `VueAliens`) pendant toute phase active (approche, combat, départ)
  - Overlay de combat (via `VueCombat`) **uniquement pendant la phase COMBAT** (`isEnCombat()`)
  - **Barre d'action** (via `VueActionJoueur`) au-dessus du joueur pendant récolte/achat
  - Écran fin de partie (overlay VICTOIRE / GAME OVER)
- Touches **R** (récolte) et **ENTER** (achat) lancent une `ActionDuree` avec cooldown

### `ControleurAttaque.java` (controleur/) — MIS À JOUR
Cycle visuel des vagues intermédiaires :
- `PhaseAttaque` : INACTIF → APPROCHE → COMBAT → DEPART
- `declencherVague()` crée les `AlienVisuel` positionnés hors écran à droite
- `setZoneFerme(x,y,w,h)` : reçoit les coordonnées de la ferme pour cibler les aliens
- `isEnCombat()` : true uniquement pendant la phase COMBAT (pour l'overlay)
- `getAliensVisuels()` : liste des aliens à dessiner
- **Enlèvement** : sur défaite, appelle `ferme.enleverDerniereVache()` + état ENLEVEMENT

### `ControleurCombat.java` (controleur/) — MIS À JOUR
Même cycle visuel pour le boss final :
- `PhaseBoss` : INACTIF → APPROCHE → COMBAT → DEPART
- `lancerCombatFinal()` crée un `AlienVisuel` boss (plus lent, 180 px/s, centré sur la ferme)
- `isEnCombat()`, `getBossVisuel()`, `getAliensVisuels()`

### `ControleurJeu.java` (controleur/) — MIS À JOUR
- `initialiserNiveau()` passe `setZoneFerme()` aux deux contrôleurs de combat
- Gère `ActionDuree` : tick dans la boucle principale, getter/setter

## Flux visuel complet

```
LANCEMENT
  │
  ├── Écran principal : ferme (gauche) + marché (droite)
  │   └── Barre de progression en bas (timer décompte)
  │       └── Marqueurs ▲ pour les vagues, ◆ pour le boss
  │
  ├── ~33% : VAGUE 1
  │   ├── APPROCHE : aliens verts marchent du bord droit vers la ferme
  │   ├── COMBAT : overlay PV + aliens tremblent / flashent rouge
  │   ├── Victoire → DEPART : aliens fuient à droite
  │   └── Défaite → DEPART : aliens enlèvent une vache + GAME OVER
  │
  ├── ~66% : VAGUE 2 (même cycle)
  │
  └── 100% : COMBAT FINAL
      ├── APPROCHE : boss (grand alien vert) marche vers le centre
      ├── COMBAT : overlay PV boss
      ├── Victoire → boss fuit + "NIVEAU TERMINÉ !" + [ESPACE]
      └── Défaite → boss repart avec vache + "GAME OVER" + [ESPACE]
```

## Contrôles

| Touche | Action |
|--------|--------|
| Flèches / ZQSD | Déplacer le fermier |
| 1 / 2 | Sélectionner article au marché |
| TAB | Article suivant |
| ENTER | Acheter |
| R | Récolter (en zone ferme) |
| ESPACE | Niveau suivant / Recommencer |
| P | Pause / Reprendre |

## Achat d'armes (ControleurMarche)

Le marché permet maintenant d'acheter des armes en plus des vaches :

- **ControleurMarche** (controleur/) — contrôleur MVC dédié au marché
  - `acheter(int indexArticle)` → retourne un `ResultatAchat` (OK, FONDS_INSUFFISANTS, FERME_PLEINE, AUCUNE_SELECTION)
  - Si l'article est une arme (ex. Rayon laser), appelle `controleurJeu.setArme(arme)`
  - Si l'article est une vache, l'ajoute à la ferme
- **VuePrincipale** délègue désormais les achats à `ControleurMarche`
- **Arme Rayon laser** : 25 dégâts, 800ms cooldown (vs Épée : 15 dégâts, 1000ms)

## Tests

| Classe de test | Nb tests | Couverture |
|---------------|----------|------------|
| NiveauTest | 11 | Paramètres, scaling, vagues, boss |
| BarreProgressionTest | 12 | Timing, événements, réinitialisation |
| ControleurAttaqueTest | 9 | Vagues, phases (approche→combat→départ), enlèvement vache |
| ControleurCombatTest | 6 | Boss, phases, victoire, défaite, récompense |
| JoueurTest | 25 | PV, monnaie, position, arme, limites |
| ActionDureeTest | 10 | Progression, complétion, label, types |
| MarcheTest | 12 | Articles, achat, fonds, capacité |
| CarteTest | 18 | Zones, collision, limites |
| FermeTest | 14 | Vaches, production, récolte, enlèvement |
| VacheTest | 12 | Croissance, lait, noms |
| PartieTest | 27 | États, transitions, pause, niveaux |
| CombatUnitTest | 11 | Attaque/alien/arme |
| AttaqueTest | 8 | Mécanique combat |
| ControleurMarcheTest | 10 | Achat vache/arme, erreurs, setArme |
| **Total** | **185** | |
