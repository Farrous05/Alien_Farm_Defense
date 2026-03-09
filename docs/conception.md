# Conception — Alien Farm Defense

## Architecture MVC

```
┌──────────────────────────────────────────────────────┐
│                      MODÈLE                           │
│  Joueur  Ferme  Vache  Carte  Marche  Partie         │
│  Niveau  BarreProgression  EvenementTemporel         │
│  Attaque  Alien  BossFinal  Arme  AlienVisuel        │
│  ActionDuree                                          │
└────────────────────┬─────────────────────────────────┘
                     │
┌────────────────────┼─────────────────────────────────┐
│                 CONTRÔLEUR                            │
│  ControleurJeu ──── boucle principale (60 FPS)       │
│    ├── ControleurJoueur  (déplacement, récolte)      │
│    ├── ControleurAttaque (vagues intermédiaires)     │
│    ├── ControleurCombat  (combat final / boss)       │
│    └── ControleurMarche  (achat vache / arme)        │
└────────────────────┬─────────────────────────────────┘
                     │
┌────────────────────┼─────────────────────────────────┐
│                     VUE                               │
│  VuePrincipale ── fenêtre Swing (JFrame)             │
│    ├── VueFerme           (grille de parcelles)      │
│    ├── VueHUD             (PV, monnaie, vaches)      │
│    ├── VueMarche          (panneau marché)           │
│    ├── VueBarreProgression (timer + événements)      │
│    ├── VueCombat          (overlay combat)           │
│    ├── VueAliens          (aliens visuels sur carte) │
│    └── VueActionJoueur    (barre action joueur)      │
└──────────────────────────────────────────────────────┘
```

## Flux de jeu

```
Main.java
  │
  ├── Crée les objets modèle (Joueur, Ferme, Carte, Marche, Partie)
  ├── Crée VuePrincipale (passe tous les modèles)
  └── VuePrincipale.lancer()
       │
       ├── partie.demarrer()
       ├── controleurJeu.initialiserNiveau(partie)
       └── Timer Swing (16ms ≈ 60 FPS)
            │
            └── tick(deltaMs)
                 ├── controleurJoueur.tick()
                 ├── ferme.mettreAJour(deltaMs)
                 └── tickProgression(deltaMs)
                      ├── barreProgression.mettreAJour()
                      ├── → ATTAQUE_INTERMEDIAIRE → controleurAttaque
                      ├── → COMBAT_FINAL → controleurCombat
                      └── fin barre → partie.terminer(true)
```

## États de la partie (Partie.java)

```
MENU ──demarrer()──► EN_COURS ──► COMBAT_FINAL
                        │              │
                        │         terminer(true) → VICTOIRE
                        │         terminer(false) → DEFAITE
                        │
                   terminer(false) → DEFAITE
```

## Système de combat

- **Automatique** : les attaques se déclenchent par cooldown (pas d'input joueur)
- **Arme du joueur** : `Arme` avec dégâts + cooldown (Épée 15/1s, Rayon laser 25/800ms)
- **Aliens** : PV + dégâts + cooldown, scaling par niveau
- **Boss** : PV = 80 + niv×40, dégâts = 8 + niv×3

### Animation visuelle des combats

Chaque combat (vague ou boss) suit un cycle en 3 phases :

```
INACTIF → APPROCHE → COMBAT → DEPART → INACTIF
```

- **APPROCHE** : les aliens (`AlienVisuel`) marchent du bord droit vers la ferme (~250 px/s pour les vagues, ~180 px/s pour le boss). La barre de progression est en pause.
- **COMBAT** : combat automatique avec overlay PV. Les aliens tremblent sur place (oscillation sinusoïdale) et flashent en rouge à chaque coup.
- **DEPART** :
  - *Victoire* → les aliens fuient vers la droite (état `FUITE`)
  - *Défaite* → les aliens repartent lentement avec une vache volée (état `ENLEVEMENT`, appel `ferme.enleverDerniereVache()`)

Classes impliquées :
- `AlienVisuel` (modèle) : position, mouvement lerp, état visuel, offset tremblements
- `ControleurAttaque.PhaseAttaque` : enum INACTIF/APPROCHE/COMBAT/DEPART
- `ControleurCombat.PhaseBoss` : même cycle pour le boss final
- `VueAliens` (vue) : dessine les aliens (ovale vert, yeux, flash combat, icône vache)

## Actions avec durée (cooldowns joueur)

Certaines actions du joueur prennent du temps (ne sont pas instantanées) :

| Action | Durée | Touche | Zone |
|--------|-------|--------|------|
| Récolte | 2000 ms | R | Ferme |
| Achat | 1500 ms | ENTER | Marché |

- Modèle : `ActionDuree` avec `TypeAction` (RECOLTE, ACHAT), progression 0→1, callback à la fin
- Vue : `VueActionJoueur` dessine une barre cyan 60×8 px au-dessus du joueur avec label
- Une seule action à la fois (les inputs sont ignorés pendant une action)

## Système économique

- **Monnaie initiale** : 200
- **Revenus** : récolte de lait (vaches à maturité) + récompense boss
- **Dépenses** : vaches (50) + armes (Rayon laser 150) au marché
- **Marché** : géré par `ControleurMarche`, retourne `ResultatAchat`

## Progression

- **Niveaux infinis** avec difficulté croissante
- **Durée** : 120s + 15s × numéro
- **Vagues** : 2 à 6, réparties uniformément sur la durée
- **Pause** : touche P, suspend timer + game loop

## Contrôles

| Touche | Action |
|--------|--------|
| Flèches / ZQSD | Déplacer le fermier |
| 1 / 2 | Sélectionner article au marché |
| TAB | Article suivant |
| ENTER | Acheter l'article sélectionné |
| R | Récolter le lait |
| ESPACE | Niveau suivant / Recommencer |
| P | Pause / Reprendre |

## Tests (185 total)

| Classe | Tests | Catégorie |
|--------|-------|-----------|
| JoueurTest | 25 | Modèle — joueur |
| PartieTest | 27 | Modèle — états jeu |
| CarteTest | 18 | Modèle — carte/zones |
| MarcheTest | 12 | Modèle — marché |
| FermeTest | 14 | Modèle — ferme (+ enlèvement vache) |
| VacheTest | 12 | Modèle — vache |
| ActionDureeTest | 10 | Modèle — actions avec durée |
| NiveauTest | 11 | Progression — niveaux |
| BarreProgressionTest | 12 | Progression — timer |
| CombatUnitTest | 11 | Combat — attaque/alien |
| AttaqueTest | 8 | Combat — mécanique |
| ControleurAttaqueTest | 9 | Contrôleur — vagues + phases + enlèvement |
| ControleurCombatTest | 6 | Contrôleur — boss + phases |
| ControleurMarcheTest | 10 | Contrôleur — marché |
