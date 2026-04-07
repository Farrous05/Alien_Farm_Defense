# Alien Farm Defense
## Rapport de projet de programmation concurrente et interface interactive

**Université Paris-Saclay**
**L3 Informatique — PCII 2026**

---

## Table des matières

1. [Introduction](#1-introduction)
2. [Analyse globale](#2-analyse-globale)
3. [Plan de développement](#3-plan-de-développement)
4. [Conception générale](#4-conception-générale)
5. [Conception détaillée](#5-conception-détaillée)
6. [Résultats](#6-résultats)
7. [Documentation utilisateur](#7-documentation-utilisateur)
8. [Documentation développeur](#8-documentation-développeur)
9. [Conclusion et perspectives](#9-conclusion-et-perspectives)

---

## 1. Introduction

Ce projet a été réalisé dans le cadre du module de **Programmation Concurrente et Interfaces Interactives** (L3 Informatique, Université Paris-Saclay). L'objectif est de développer un jeu vidéo original en **Java/Swing** intégrant les concepts de **programmation concurrente** et d'**interfaces interactives** vus en cours.

Le jeu, intitulé **Alien Farm Defense**, se déroule sur une carte vue du dessus en deux zones. Le joueur incarne un fermier qui gère son élevage de vaches pour accumuler de l'argent, s'approvisionne au marché en achetant des armes et du bétail, puis doit défendre sa ferme contre des vagues d'extraterrestres qui tentent d'enlever ses vaches. Une barre de progression temporelle rythme chaque niveau : des attaques intermédiaires ponctuent la montée en tension, avant un combat de boss final qui conditionne la victoire ou la défaite. Entre deux niveaux, une boutique d'améliorations permet d'investir ses gains dans des upgrades permanentes.

Ce projet a permis de mettre en pratique l'architecture **MVC**, la **boucle de jeu à 60 FPS** via un `Timer` Swing, la gestion de l'état global d'une partie à travers une machine à états, ainsi que la conception d'un système de rendu avec caméra, tile-map et sprites animés.

---

## 2. Analyse globale

L'analyse du cahier des charges a fait émerger **9 fonctionnalités principales**, classées par priorité et niveau de difficulté.

### F1 — Déplacement du joueur et caméra

Le joueur se déplace librement sur un monde de 2 000 × 1 400 pixels à l'aide des touches directionnelles. Une caméra suit le joueur et convertit les coordonnées du monde en coordonnées écran. Le monde est clampé aux bords pour que la caméra ne dépasse jamais les limites.

- **Difficulté** : Moyenne
- **Priorité** : Haute

### F2 — Monde tilemap et zones

Le monde est divisé en deux zones de 1 000 px chacune : la ferme (gauche) et le marché (droite). Chaque zone est rendue par une grille de 25 × 35 tuiles (40 px/tuile) chargées depuis des fichiers texte. Un trait animé bleu marque la frontière.

- **Difficulté** : Moyenne
- **Priorité** : Haute

### F3 — Ferme et cycle de vie des vaches

Les vaches achetées apparaissent dans la ferme et évoluent automatiquement selon trois états : **Bébé** → **Adulte** → **Productive**. En état productif, elles accumulent de la monnaie par cycle. Le joueur doit se rapprocher d'une vache productive et appuyer sur R pour récolter.

- **Difficulté** : Moyenne
- **Priorité** : Haute

### F4 — Système économique (monnaie et marché)

Le joueur dispose d'une monnaie initiale, gagnée par la récolte et les récompenses de combat. Il la dépense au marché auprès de quatre vendeurs (forge, élevage, apothicaire, armurerie) accessibles par proximité. Chaque achat prend un temps incompressible modélisé par une `ActionDuree`.

- **Difficulté** : Moyenne
- **Priorité** : Haute

### F5 — Système de combat : vagues aliens et boss

À des instants prédéfinis sur la barre de progression, des vagues d'aliens apparaissent depuis le bord droit de l'écran et marchent vers la ferme. Le combat est automatique (basé sur des cooldowns). En cas de défaite, l'alien repart avec une vache. À la fin du timer, un boss final détermine la victoire ou la défaite du niveau.

- **Difficulté** : Élevée
- **Priorité** : Haute

### F6 — Progression temporelle et gestion des niveaux

Une barre de progression compte le temps du niveau. Les vagues et le boss sont des `EvenementTemporel` planifiés à des positions précises sur cette barre. La difficulté augmente à chaque niveau : durée, nombre de vagues, PV et dégâts des aliens.

- **Difficulté** : Moyenne
- **Priorité** : Haute

### F7 — Boutique d'améliorations (entre les niveaux)

Après chaque victoire, le jeu entre dans un état `UPGRADE_SHOP` où le joueur peut acheter des améliorations permanentes (PV max, dégâts d'arme, vitesse de croissance des vaches, or de départ) avant de passer au niveau suivant.

- **Difficulté** : Facile
- **Priorité** : Moyenne

### F8 — Tableau des scores

Le jeu enregistre les 10 meilleurs scores sur le disque. À la fin d'une partie, le joueur saisit ses initiales (3 caractères). Le tableau est consultable depuis le menu principal.

- **Difficulté** : Facile
- **Priorité** : Moyenne

### F9 — Système de succès

Cinq succès débloquables récompensent des accomplissements spécifiques (premier alien tué, 500g gagnés, 50 aliens éliminés, niveau terminé à PV max, 3 vaches simultanées). Les succès débloqués sont affichés comme badges dans la barre latérale.

- **Difficulté** : Facile
- **Priorité** : Basse

### Tableau récapitulatif

| # | Fonctionnalité | Difficulté | Priorité |
|---|----------------|------------|----------|
| F1 | Déplacement joueur et caméra | Moyenne | Haute |
| F2 | Monde tilemap et zones | Moyenne | Haute |
| F3 | Ferme et cycle de vie des vaches | Moyenne | Haute |
| F4 | Système économique (marché) | Moyenne | Haute |
| F5 | Combat : vagues aliens et boss | Élevée | Haute |
| F6 | Progression temporelle et niveaux | Moyenne | Haute |
| F7 | Boutique d'améliorations | Facile | Moyenne |
| F8 | Tableau des scores | Facile | Moyenne |
| F9 | Système de succès | Facile | Basse |

---

## 3. Plan de développement

Le développement s'est organisé en **6 séances** selon une approche incrémentale, en partant du socle MVC jusqu'aux fonctionnalités de polish.

### Séance 1 — Squelette MVC, joueur et monde

- **Analyse** (30 min) : Étude de l'architecture MVC avec Swing, choix du Timer à 60 FPS, conception du système de coordonnées monde/écran.
- **Conception** (30 min) : Définition des classes `Joueur`, `Carte`, `Partie`, `EtatJeu`. Machine à états simplifiée.
- **Développement** (~3h) : `Main.java` + `VuePrincipale` (JFrame + boucle Timer), `Joueur` (position, vitesse, déplacement delta-time), `Camera` (centrer + clamper + conversion coordonnées), `ControleurJoueur` (KeyListener 4 directions), `Constantes.java`.
- **Tests** (20 min) : Déplacement fluide, joueur ne sort pas du monde, caméra clampée.

### Séance 2 — Tilemap, ferme et vaches

- **Analyse** (20 min) : Étude du format tile-map texte, choix du pack Kenney Tiny Town.
- **Conception** (20 min) : Algorithme de rendu de tuiles avec culling caméra. Modèle `Animal` → `Vache` → `EtatVache`. Algorithme de croissance avec report de surplus.
- **Développement** (~4h) : `TileManager` (chargement PNGs 16×16 → 40×40), `MondeRenderer` (grille + culling), `farm_map.txt` / `market_map.txt`, `Animal` (abstrait), `Vache` (BEBE→ADULTE→PRODUCTIVE, accumulation monnaie), `Ferme` (troupeau, tick, récolte).
- **Tests** (30 min) : 12 `VacheTest` + 14 `FermeTest` (transitions d'état, surplus, récolte, capacité).

### Séance 3 — Économie, marché et actions avec durée

- **Analyse** (20 min) : Conception du marché à vendeurs-objets monde, système de proximité (rayon 90 px), `ActionDuree` pour cooldown achat/récolte.
- **Conception** (20 min) : `ArticleMarche`, `ResultatAchat`, flux d'achat dans `ControleurMarche`. Architecture 4 vendeurs positionnés dans la zone marché.
- **Développement** (~3h) : `Marche` + `ArticleMarche`, `VendeurMarche` (position monde, rayon, items), `VueMarchePopup` (anneau pulsé + popup achat), `ControleurMarche`, `ActionDuree` + `VueActionJoueur`.
- **Tests** (20 min) : 12 `MarcheTest` + 10 `ActionDureeTest` + 10 `ControleurMarcheTest`.

### Séance 4 — Combat, aliens et boss

- **Analyse** (30 min) : Étude de la mécanique de combat automatique par cooldown, animation aliens (approach → combat → départ), cycle phases vague/boss.
- **Conception** (30 min) : `Attaque` (échange de coups, gestion séquentielle des aliens), `AlienVisuel` (machine à états visuelle), `PhaseAttaque` enum.
- **Développement** (~4h) : `Arme`, `Extraterrestre`, `BossFinal`, `Attaque`, `AlienVisuel` + `EtatVisuel`, `ControleurAttaque` (phases + enlèvement vache), `ControleurCombat` (boss + récompense), `VueAliens` (sprites PNG manned), `VueCombat` (overlay HP).
- **Tests** (30 min) : 11 `CombatUnitTest` + 8 `AttaqueTest` + 9 `ControleurAttaqueTest` + 6 `ControleurCombatTest`.

### Séance 5 — Progression temporelle, HUD et polish visuel

- **Analyse** (20 min) : Conception de la barre de progression avec événements planifiés, sprites directionnels joueur, indicateurs de navigation.
- **Conception** (20 min) : `EvenementTemporel`, `BarreProgression` (chrono + détection événements), `Niveau` (factory paramétrique).
- **Développement** (~4h) : `Niveau`, `BarreProgression`, `EvenementTemporel`, `ControleurJeu.tickProgression()`, `VueBarreProgression` (marqueurs triangulaires), `VueHUD`, `VueIndicateurs` (flèches de bord), sprites joueur (Blue Boy, 4 directions × 2 frames), sprites vaches (Bébé/Adulte/Productive), `CacheSpritesAliens` (UFOs manned PNG).
- **Tests** (20 min) : 11 `NiveauTest` + 12 `BarreProgressionTest`.

### Séance 6 — Boutique, leaderboard, succès et états globaux

- **Analyse** (20 min) : Conception des 3 features de polish. `EtatJeu.UPGRADE_SHOP`, persistance scores sur disque, enum `Succes` avec seuils.
- **Conception** (20 min) : `Upgrades` (multiplicateurs), `TableauScores` (top 10, sérialisation), `GestionnaireSucces` (set + compteurs).
- **Développement** (~3h) : `Upgrades`, `VueUpgrades` (4 cartes, navigation ←/→, ENTRÉE/ESPACE), `TableauScores` + `VueTableauScores`, `Succes` (enum), `GestionnaireSucces`, hooks dans `ControleurJeu` / `ControleurAttaque` / `Joueur`.
- **Tests** (15 min) : Tests `PartieTest` (27 tests, transitions `UPGRADE_SHOP`).

### Autres tâches (transversales)

- **Rédaction du rapport** : tout au long du projet (~3h).
- **Création / adaptation des assets** : sprites vaches custom, copie des assets aliens Kenney, tile-maps farm/market (~2h, réparties sur séances 2 et 5).
- **Tests de régression** : séance 6 (vérification globale, 185 tests au total).

**Total estimé : ~26h.**

### Diagramme de Gantt

> *Le diagramme de Gantt détaillé est fourni en fichier séparé (`docs/Gantt_AlienFarmDefense.xlsx`). Le tableau ci-dessous en présente une vue synthétique.*

| Tâche | S1 | S2 | S3 | S4 | S5 | S6 |
|-------|----|----|----|----|----|----|
| Squelette MVC + joueur + caméra | ██ | | | | | |
| Tilemap + ferme + vaches | | ██ | | | | |
| Économie + marché + actions | | | ██ | | | |
| Combat + aliens + boss | | | | ██ | | |
| Progression + HUD + polish visuel | | | | | ██ | |
| Boutique + leaderboard + succès | | | | | | ██ |
| Tests (185 au total) | ░ | ░ | ░ | ░ | ░ | ██ |
| Rapport | ░ | ░ | ░ | ░ | ░ | ██ |

---

## 4. Conception générale

Le projet suit le patron **Modèle-Vue-Contrôleur (MVC)** pour séparer rigoureusement les données, leur représentation et la logique d'interaction.

### Structure des paquets

- **Modèle** (`com.fermedefense.modele`) : logique métier pure — état du joueur, cycle de vie des vaches, mécanique de combat, progression du niveau, achievements, scores.
- **Vue** (`com.fermedefense.vue`) : rendu graphique Swing — tile-map, sprites, overlays, HUD, indicateurs.
- **Contrôleur** (`com.fermedefense.controleur`) : boucle de jeu principale, gestion des événements clavier, orchestration des sous-systèmes.
- **Utilitaire** (`com.fermedefense.utilitaire`) : constantes globales.

### Blocs fonctionnels et circulation de l'information

Le schéma ci-dessous présente l'architecture fonctionnelle du projet.

![Diagramme de blocs fonctionnels](docs/images/blocs_fonctionnels.png)

#### Description des blocs

1. **Main** : Point d'entrée. Instancie tous les objets modèle, crée la `VuePrincipale` et démarre la boucle.

2. **Boucle de jeu (Timer 60 FPS)** : Cœur de l'application. Appelle `tick(deltaMs)` à chaque frame. Orchestre tous les contrôleurs.

3. **Contrôleur joueur** : Lit les touches enfoncées et déplace le joueur dans le monde. Vérifie la zone (ferme/marché) pour autoriser les actions.

4. **Contrôleur marché** : Gère les achats (proximité vendeur, fonds suffisants, type d'article). Retourne un `ResultatAchat`.

5. **Ferme** : Met à jour toutes les vaches à chaque tick (croissance, production). Gère l'ajout, le retrait et l'enlèvement de vaches.

6. **Contrôleur attaque** : Déclenche les vagues d'aliens au signal de la barre de progression. Gère le cycle APPROCHE → COMBAT → DEPART.

7. **Contrôleur combat** : Gère le boss final avec le même cycle visuel. Accorde une récompense en monnaie si victoire.

8. **Progression** : Compte le temps du niveau, planifie les événements (vagues, boss), détecte leur déclenchement à chaque tick.

9. **Machine à états (Partie)** : Centralise l'état global du jeu (MENU, EN_COURS, COMBAT_FINAL, UPGRADE_SHOP, VICTOIRE, DEFAITE).

10. **Vue principale** : `paintComponent` dessine le monde (tilemap + entités) puis les overlays appropriés selon l'état courant.

11. **Caméra** : Convertit les coordonnées monde en coordonnées écran. Centrée sur le joueur, clampée aux bords du monde.

12. **Gestionnaire de scores / succès** : Vérifie les seuils de déclenchement à chaque événement. Sauvegarde le tableau des scores sur disque.

### Boucle de jeu et machine à états

```
Main.java
  │
  ├── Crée modèle (Joueur, Ferme, Carte, Marche, Partie, Upgrades, …)
  └── VuePrincipale.lancer()
       │
       └── Timer Swing (16ms ≈ 60 FPS)
            │
            └── tick(deltaMs)
                 ├── controleurJoueur.tick()        ← déplacement
                 ├── ferme.mettreAJour(deltaMs)     ← croissance vaches
                 ├── actionDuree?.tick(deltaMs)     ← cooldown achat/récolte
                 └── tickProgression(deltaMs)
                      ├── barreProgression.mettreAJour()
                      ├── → ATTAQUE_INTERMEDIAIRE → controleurAttaque
                      ├── → COMBAT_FINAL         → controleurCombat
                      └── fin barre             → VICTOIRE / DEFAITE
```

### États de la partie

```
MENU ──demarrer()──► EN_COURS ──► COMBAT_FINAL ──► VICTOIRE ──► UPGRADE_SHOP ──► EN_COURS (niv+1)
                        │              │
                        │         terminer(false) ──► DEFAITE
                   terminer(false) ──► DEFAITE
```

---

## 5. Conception détaillée

---

### 5.1 F1 — Déplacement du joueur et caméra

#### Structures de données

**`Joueur`** (modele/joueur/) — classe centrale du modèle joueur :

| Attribut | Type | Description |
|----------|------|-------------|
| `x`, `y` | `double` | Position dans le monde (pixels) |
| `vitesse` | `double` | Vitesse en pixels/seconde |
| `directionCourante` | `Action` | Direction active (HAUT/BAS/GAUCHE/DROITE) ou null |
| `enMouvement` | `boolean` | True si une touche est enfoncée |
| `pointsDeVie` | `int` | PV actuels |
| `pointsDeVieMax` | `int` | PV maximum |
| `monnaie` | `int` | Or disponible |
| `arme` | `Arme` | Arme équipée |

**`Camera`** (vue/) :

| Attribut | Type | Description |
|----------|------|-------------|
| `offsetX`, `offsetY` | `double` | Décalage monde→écran |

**`Carte`** (modele/jeu/) : dimensions du monde, zones, clampage de la position du joueur.

#### Constantes du modèle

| Constante | Valeur | Description |
|-----------|--------|-------------|
| `LARGEUR_CARTE` | 2 000 | Largeur totale du monde (px) |
| `HAUTEUR_CARTE` | 1 400 | Hauteur totale du monde (px) |
| `LARGEUR_VIEWPORT` | 1 200 | Largeur de la fenêtre de jeu |
| `HAUTEUR_VIEWPORT` | 840 | Hauteur de la fenêtre de jeu |
| `LARGEUR_SIDEBAR` | 240 | Largeur de la barre latérale |
| `PLAYER_SIZE` | 48 | Taille du sprite joueur (px) |
| `vitesse joueur` | 200 | px/s (configurable dans `Constantes`) |

#### Algorithmes

**Joueur.mettreAJour(deltaMs) :**

```
SI enMouvement ET directionCourante ≠ null ALORS
    deplacement ← vitesse × deltaMs / 1000
    SELON directionCourante:
        HAUT   → y ← y - deplacement
        BAS    → y ← y + deplacement
        GAUCHE → x ← x - deplacement
        DROITE → x ← x + deplacement
    x ← CLAMP(x, 0, LARGEUR_CARTE - PLAYER_SIZE)
    y ← CLAMP(y, 0, HAUTEUR_CARTE - PLAYER_SIZE)
FIN SI
```

**Camera.centrerSur(joueur) :**

```
offsetX ← joueur.x - LARGEUR_VIEWPORT / 2
offsetY ← joueur.y - HAUTEUR_VIEWPORT / 2
offsetX ← CLAMP(offsetX, 0, LARGEUR_CARTE - LARGEUR_VIEWPORT)
offsetY ← CLAMP(offsetY, 0, HAUTEUR_CARTE - HAUTEUR_VIEWPORT)
```

**Camera.toScreenX/Y(coordMonde) :**

```
screenX ← coordMonde - offsetX
screenY ← coordMonde - offsetY
```

#### Conditions limites

- Le joueur ne peut pas sortir du monde (clampage dans `Carte`).
- La caméra ne peut pas sortir du monde (offsetX ∈ [0, LARGEUR_CARTE − LARGEUR_VIEWPORT]).
- Le déplacement est proportionnel au delta-time pour être indépendant du FPS.

#### Diagramme de classes (F1)

![Diagramme de classes F1](docs/images/uml_f1_joueur_camera.png)

---

### 5.2 F2 — Monde tilemap et zones

#### Structures de données

**`TileManager`** (vue/) : charge 132 images PNG 16×16 px (tiles Kenney Tiny Town), les redimensionne à 40×40 px et les stocke dans un tableau `BufferedImage[]`.

**`MondeRenderer`** (vue/) : lit deux fichiers `.txt` (grilles 25×35 d'entiers), appelle `TileManager` pour récupérer chaque tuile et la dessiner avec culling caméra.

**`Carte`** (modele/jeu/) : stocke les dimensions du monde et les limites des zones (ferme : x ∈ [0, 1 000], marché : x ∈ [1 000, 2 000]).

#### Constantes du modèle

| Constante | Valeur | Description |
|-----------|--------|-------------|
| `TAILLE_TUILE` | 40 px | Taille d'une tuile à l'écran |
| `NB_COLS_ZONE` | 25 | Colonnes par zone |
| `NB_LIGNES` | 35 | Lignes de la carte |
| `LARGEUR_ZONE` | 1 000 | Largeur d'une zone (px) |

#### Algorithme — MondeRenderer.draw(Graphics2D, Camera)

```
POUR ligne DE 0 À NB_LIGNES - 1:
    POUR col DE 0 À NB_COLS_TOTAL - 1:
        worldX ← col × TAILLE_TUILE
        worldY ← ligne × TAILLE_TUILE
        screenX ← camera.toScreenX(worldX)
        screenY ← camera.toScreenY(worldY)

        SI screenX + TAILLE_TUILE < 0 OU screenX > LARGEUR_VIEWPORT ALORS
            CONTINUER  ← culling horizontal
        FIN SI

        tileId ← grille[ligne][col]
        image  ← tileManager.getTile(tileId)
        g2.drawImage(image, screenX, screenY, TAILLE_TUILE, TAILLE_TUILE, null)
    FIN POUR
FIN POUR

// Trait de séparation animé (x = 1000 en coordonnées monde)
divX ← camera.toScreenX(LARGEUR_CARTE / 2)
Dessiner ligne pointillée bleue à divX
```

#### Conditions limites

- Le culling évite de dessiner les tuiles hors viewport, optimisant le rendu.
- Si une image de tuile est absente, une couleur de fallback est dessinée.

#### Diagramme de classes (F2)

![Diagramme de classes F2](docs/images/uml_f2_tilemap.png)

---

### 5.3 F3 — Ferme et cycle de vie des vaches

#### Structures de données

**`Animal`** (abstract, modele/ferme/) : classe mère avec `nom`, `x`, `y`, `mettreAJour(deltaMs)` (abstract), `isProductif()`, `getRevenusParCycle()`.

**`EtatVache`** (enum) : `BEBE`, `ADULTE`, `PRODUCTIVE`.

**`Vache`** (extends Animal) :

| Attribut | Type | Défaut | Description |
|----------|------|--------|-------------|
| `etat` | `EtatVache` | `BEBE` | Phase de croissance |
| `tempsEcoule` | `long` | 0 | Temps dans la phase courante (ms) |
| `tempsBebeMs` | `long` | 10 000 | Durée phase Bébé |
| `tempsAdulteMs` | `long` | 15 000 | Durée phase Adulte |
| `cycleProdMs` | `long` | 8 000 | Intervalle entre deux productions |
| `revenuParCycle` | `int` | 10 | Monnaie produite par cycle |
| `monnaieAccumulee` | `int` | 0 | En attente de récolte |

**`Ferme`** :

| Attribut | Type | Description |
|----------|------|-------------|
| `vaches` | `List<Vache>` | Troupeau |
| `capaciteMax` | `int` | 10 max (configurable) |

#### Algorithmes

**Vache.mettreAJour(deltaMs) :**

```
tempsEcoule ← tempsEcoule + deltaMs
SELON etat:
    BEBE:
        SI tempsEcoule ≥ tempsBebeMs ALORS
            surplus ← tempsEcoule - tempsBebeMs
            etat ← ADULTE ; tempsEcoule ← surplus
        FIN SI
    ADULTE:
        SI tempsEcoule ≥ tempsAdulteMs ALORS
            surplus ← tempsEcoule - tempsAdulteMs
            etat ← PRODUCTIVE ; tempsEcoule ← surplus
        FIN SI
    PRODUCTIVE:
        TANT QUE tempsEcoule ≥ cycleProdMs FAIRE
            monnaieAccumulee ← monnaieAccumulee + revenuParCycle
            tempsEcoule ← tempsEcoule - cycleProdMs
        FIN TANT QUE
FIN SELON
```

Le **report de surplus** garantit qu'un grand delta (lag, pause) ne fait pas perdre de temps de croissance.

**Ferme.recolterTout() :**

```
total ← 0
POUR CHAQUE vache DANS vaches FAIRE
    total ← total + vache.recolter()  // recolter() remet monnaieAccumulee à 0
FIN POUR
RETOURNER total
```

#### Conditions limites

| Condition | Comportement |
|-----------|-------------|
| Ferme pleine | `ajouterVache()` retourne `false` |
| Vache null | `ajouterVache()` retourne `false` |
| Récolte sans production | Retourne 0 |
| Delta très grand | Surplus reporté, pas de perte de temps |
| Alien enlèvement | `ferme.enleverDerniereVache()` retire la dernière vache |

#### Diagramme de classes (F3)

![Diagramme de classes F3](docs/images/uml_f3_ferme_vache.png)

---

### 5.4 F4 — Système économique : monnaie et marché

#### Structures de données

**`Joueur`** (attributs monnaie) :

| Méthode | Comportement |
|---------|--------------|
| `ajouterMonnaie(int)` | Ignore les montants ≤ 0 |
| `depenser(int)` | Retourne false si montant > monnaie ; déduit sinon |

**`ArticleMarche`** : `nom`, `prix`, `typeArticle` (VACHE / ARME / BOMBE / SOIN), `niveauRequis`.

**`ResultatAchat`** (enum) : `OK`, `FONDS_INSUFFISANTS`, `FERME_PLEINE`, `AUCUNE_SELECTION`, `NIVEAU_INSUFFISANT`.

**`VendeurMarche`** : position monde (x, y), rayon de proximité (90 px), liste d'articles.

**`ActionDuree`** : `TypeAction` (RECOLTE 2 000 ms / ACHAT 1 500 ms), progression 0 → 1, callback à la fin.

#### Vendeurs dans la zone marché

| Vendeur | Position monde | Articles |
|---------|---------------|----------|
| Forge | (1 120, 260) | Épée, Rayon laser |
| Élevage | (1 400, 460) | Vache |
| Apothicaire | (1 680, 260) | Potion soin |
| Armurerie | (1 280, 700) | Bombe |

#### Algorithme — ControleurMarche.acheter(ArticleMarche)

```
SI joueur.monnaie < article.prix ALORS RETOURNER FONDS_INSUFFISANTS
SI article.niveauRequis > partie.getNiveauCourant() ALORS RETOURNER NIVEAU_INSUFFISANT
SI article est une vache ET ferme.estPleine() ALORS RETOURNER FERME_PLEINE

joueur.depenser(article.prix)
SELON article.type:
    VACHE → ferme.ajouterVache(new Vache())
    ARME  → controleurJeu.setArme(article.arme)
    SOIN  → joueur.soigner(montantSoin)
    BOMBE → joueur.ajouterBombe()
RETOURNER OK
```

#### Conditions limites

- Un achat ne peut démarrer que si aucune `ActionDuree` n'est en cours.
- Le joueur doit être à ≤ 90 px du vendeur pour ouvrir son menu.
- Les achats avec `niveauRequis > niveauCourant` apparaissent grisés dans le popup.

#### Diagramme de classes (F4)

![Diagramme de classes F4](docs/images/uml_f4_marche_economie.png)

---

### 5.5 F5 — Système de combat : vagues aliens et boss

#### Structures de données

**`Arme`** : `nom`, `degats` (int), `cooldownMs` (long). Constante statique `Arme.EPEE` (15 dég, 1 000 ms).

**`Extraterrestre`** : `pointsDeVie`, `pointsDeVieMax`, `degats`, `cooldownMs`.

**`BossFinal`** (extends Extraterrestre) : `recompense`. Fabrique statique `BossFinal.pourNiveau(n)`.

**`Attaque`** : liste d'`Extraterrestre`, index alien courant, cooldowns joueur/alien, résultat (`ResultatCombat`).

**`AlienVisuel`** : position monde (x, y), cible, état visuel (`EtatVisuel` : APPROCHE/COMBAT/FUITE/ENLEVEMENT), offset sinusoïdal en combat.

#### Algorithme — Attaque.mettreAJour(deltaMs, joueur, arme)

```
SI résultat ≠ EN_COURS ALORS RETOURNER

alien ← aliens[indexAlienCourant]
SI alien = null ALORS résultat ← VICTOIRE ; RETOURNER

tempsCooldownJoueur ← tempsCooldownJoueur - deltaMs
tempsCooldownAlien  ← tempsCooldownAlien  - deltaMs

SI tempsCooldownJoueur ≤ 0 ALORS
    degatsEffectifs ← arme.degats × upgrades.dommageMulti
    alien.subirDegats(degatsEffectifs)
    tempsCooldownJoueur ← arme.cooldownMs
    SI alien mort ALORS
        indexAlienCourant++
        SI plus d'aliens ALORS résultat ← VICTOIRE
    FIN SI
FIN SI

SI tempsCooldownAlien ≤ 0 ET alien vivant ALORS
    joueur.subirDegats(alien.degats)
    tempsCooldownAlien ← alien.cooldownMs
    SI joueur mort ALORS résultat ← DEFAITE
FIN SI
```

#### Cycle visuel des phases (ControleurAttaque)

```
INACTIF
  ↓ declencherVague()
APPROCHE  — aliens marchent depuis x=1100 vers la ferme (~250 px/s)
  ↓ alien atteint cible
COMBAT    — combat automatique, barre de progression en pause
  ↓ résultat ≠ EN_COURS
DEPART
  ↓ Victoire : aliens fuient à droite (état FUITE)
  ↓ Défaite  : aliens repartent avec une vache (état ENLEVEMENT) → GAME OVER
INACTIF
```

#### Scaling du boss par niveau — BossFinal.pourNiveau(n)

```
pv       ← 80 + n × 40
degats   ← 8 + n × 4
cooldown ← max(600, 1200 - n × 100)
recompense ← 100 + n × 50
```

#### Conditions limites

| Condition | Comportement |
|-----------|-------------|
| Dégâts ≤ 0 | Ignorés |
| PV < 0 | Plafonnés à 0 |
| Vague vide | Victoire immédiate |
| Combat terminé | `mettreAJour()` est sans effet |
| Défaite vague intermédiaire | Enlève une vache + continue (pas de game over immédiat) |
| Défaite boss | Game over |

#### Diagramme de classes (F5)

![Diagramme de classes F5](docs/images/uml_f5_combat.png)

---

### 5.6 F6 — Progression temporelle et gestion des niveaux

#### Structures de données

**`EvenementTemporel`** : `momentMs` (long), `type` (`TypeEvenement` : ATTAQUE_INTERMEDIAIRE / COMBAT_FINAL), `indexVague` (int), `declenche` (boolean).

**`BarreProgression`** : liste d'`EvenementTemporel`, `tempsEcoule`, `dureeNiveauMs`, flag `terminee`, flag `enPause`.

**`Niveau`** : calcule tous les paramètres du niveau depuis son numéro (durée, nombre de vagues, stats aliens, boss).

#### Paramètres de scaling

| Niveau | Durée | Vagues | Aliens/vague | Alien PV | Boss PV |
|--------|-------|--------|-------------|----------|---------|
| 1 | 120s | 2 | 1 | 20 | 120 |
| 2 | 135s | 3 | 2 | 30 | 160 |
| 3 | 150s | 4 | 2 | 40 | 200 |
| 5 | 180s | 6 | 3 | 60 | 280 |

#### Algorithme — BarreProgression.mettreAJour(deltaMs)

```
SI enPause ALORS RETOURNER []

tempsEcoule ← tempsEcoule + deltaMs
evenementsDeclenchés ← []

POUR CHAQUE evt DANS evenements FAIRE
    SI evt.declenche = faux ET tempsEcoule ≥ evt.momentMs ALORS
        evt.declenche ← vrai
        evenementsDeclenchés.ajouter(evt)
    FIN SI
FIN POUR

SI tempsEcoule ≥ dureeNiveauMs ALORS terminee ← vrai

RETOURNER evenementsDeclenchés
```

**ControleurJeu.tickProgression(deltaMs) :**

```
evenements ← barreProgression.mettreAJour(deltaMs)
POUR CHAQUE evt DANS evenements FAIRE
    SI evt.type = ATTAQUE_INTERMEDIAIRE ALORS
        controleurAttaque.declencherVague(evt.indexVague)
        barreProgression.mettreEnPause()
    SI evt.type = COMBAT_FINAL ALORS
        controleurCombat.lancerCombatFinal()
        barreProgression.mettreEnPause()
FIN POUR

SI controleurAttaque.estTermine() ALORS barreProgression.reprendre()
SI controleurCombat.estTermine(VICTOIRE) ALORS passer au niveau suivant
SI controleurCombat.estTermine(DEFAITE) ALORS game over
```

#### Conditions limites

- La barre est en **pause** pendant tout combat (vague ou boss) — le temps de jeu ne s'écoule pas.
- Un événement ne peut se déclencher qu'une seule fois (`declenche` flag).
- `getProgression()` retourne un ratio 0.0 → 1.0, utilisé par `VueBarreProgression`.

#### Diagramme de classes (F6)

![Diagramme de classes F6](docs/images/uml_f6_progression.png)

---

### 5.7 F7 — Boutique d'améliorations

#### Structures de données

**`Upgrades`** (modele/joueur/) :

| Attribut | Type | Défaut | Description |
|----------|------|--------|-------------|
| `dommageMulti` | `double` | 1.0 | Multiplicateur global des dégâts d'armes |
| `cowSpeedMulti` | `double` | 1.0 | Multiplicateur de vitesse de croissance des vaches |
| `startingGoldBonus` | `int` | 0 | Or supplémentaire reçu au début de chaque niveau |

Les PV max sont appliqués directement sur `Joueur` lors de l'achat (pas de champ dédié dans `Upgrades`).

**`VueUpgrades`** (vue/) : 4 cartes colorées, navigation ← / →, indicateur triangle sous la carte sélectionnée.

#### Catalogue des améliorations

| Index | Nom | Effet | Coût |
|-------|-----|-------|------|
| 0 | Max PV +25 | `joueur.pvMax += 25` + soin immédiat | 100g |
| 1 | Dégâts Arme +10% | `upgrades.dommageMulti × 1.10` | 150g |
| 2 | Croissance vache +20% | `upgrades.cowSpeedMulti × 1.20` | 80g |
| 3 | Or de départ +50g | `upgrades.startingGoldBonus += 50` | 75g |

#### Flux de la boutique

```
Victoire niveau N
  │
  └── EtatJeu ← UPGRADE_SHOP
       │
       ├── VuePrincipale.paintComponent() : dessine VueUpgrades en overlay
       ├── [← →] : naviguer entre les 4 cartes
       ├── [ENTRÉE] : si joueur.monnaie ≥ cout → achat + application immédiate
       └── [ESPACE] : passer → ControleurJeu.initialiserNiveau(N+1)
                                    └── joueur.ajouterMonnaie(startingGoldBonus)
```

#### Conditions limites

- Si le joueur n'a pas assez d'or, la carte s'affiche en rouge avec « Or insuffisant » et l'achat est bloqué.
- Les multiplicateurs sont **cumulatifs** : deux achats de +10% donnent ×1.21, pas ×1.20.
- L'état `UPGRADE_SHOP` bloque la boucle de jeu normale (pas de tick progression ni de mouvement joueur).

#### Diagramme de classes (F7)

![Diagramme de classes F7](docs/images/uml_f7_upgrades.png)

---

### 5.8 F8 — Tableau des scores

#### Structures de données

**`ScorePartie`** : `initiales` (String, 3 chars), `score` (int), `niveau` (int). Implémente `Serializable`.

**`TableauScores`** : liste de 10 `ScorePartie` max, triée par score décroissant. Sauvegarde/chargement via `ObjectOutputStream` dans `~/.alienfarm/scores.dat`.

#### Algorithme — TableauScores.ajouter(initiales, score, niveau)

```
nouvEntry ← new ScorePartie(initiales, score, niveau)
scores.ajouter(nouvEntry)
trier scores par score décroissant
SI scores.taille() > 10 ALORS supprimer le dernier
sauvegarder()
```

#### Hooks d'intégration

- `VuePrincipale.dessinerFinDePartie()` : à la fin de partie, `JOptionPane.showInputDialog()` demande les initiales, puis `tableauScores.ajouter(initiales, score)`.
- Menu principal : bouton « Meilleurs Scores » affiche `VueTableauScores` en overlay.

#### Conditions limites

- Si le fichier `scores.dat` est absent ou corrompu, le tableau démarre vide sans crash.
- Les initiales sont tronquées à 3 caractères, converties en majuscules.

#### Diagramme de classes (F8)

![Diagramme de classes F8](docs/images/uml_f8_leaderboard.png)

---

### 5.9 F9 — Système de succès

#### Structures de données

**`Succes`** (enum, modele/jeu/) :

| Succès | Condition | Seuil |
|--------|-----------|-------|
| `PREMIER_SANG` | Tuer un premier alien | 1 |
| `FERMIER_PROSPERE` | Gagner 500g au total | 500 |
| `EXTERMINATEUR` | Tuer 50 aliens au total | 50 |
| `INDESTRUCTIBLE` | Finir un niveau à PV maximum | 1 |
| `RANCHER` | Avoir 3 vaches simultanément | 3 |

**`GestionnaireSucces`** : `Set<Succes> debloquees`, `Map<Succes, Integer> compteurs`.

#### Algorithme — GestionnaireSucces.verifier(Succes, valeur)

```
SI s DANS debloquees ALORS RETOURNER  ← déjà débloqué

compteurs[s] ← compteurs.getOrDefault(s, 0) + valeur
SI compteurs[s] ≥ s.seuil ALORS
    debloquees.ajouter(s)
    afficherNotification(s)
FIN SI
```

#### Hooks d'intégration

- `ControleurAttaque` : sur alien tué → `verifier(PREMIER_SANG, 1)` + `verifier(EXTERMINATEUR, 1)`.
- `ControleurJeu.tickProgression` : fin de niveau + joueur PV max → `verifier(INDESTRUCTIBLE, 1)`.
- `Joueur.ajouterMonnaie(montant)` : → `verifier(FERMIER_PROSPERE, montant)`.
- `VuePrincipale.paintComponent` : si `ferme.getNombreAnimaux() ≥ 3` → `verifier(RANCHER, 3)`.

#### Conditions limites

- Un succès déjà débloqué ne peut pas être déclenché une seconde fois.
- Les succès sont sauvegardés avec le tableau des scores (même fichier de persistance).

#### Diagramme de classes (F9)

![Diagramme de classes F9](docs/images/uml_f9_succes.png)

---

### Diagramme de classes global

![Diagramme de classes global](docs/images/uml_global.png)

---

## 6. Résultats

Le projet aboutit à un jeu complet et jouable, intégrant toutes les fonctionnalités prévues.

### Menu principal et tableau des scores

![Menu principal](docs/images/screen_menu.png)

L'écran d'accueil présente le titre, un bouton « Jouer » et un bouton « Meilleurs Scores » qui affiche le tableau des 10 meilleures parties (initiales, score, niveau atteint).

### Ferme et déplacement

![Ferme et joueur](docs/images/screen_ferme.png)

Le joueur se déplace librement sur le monde tilemap. Les vaches apparaissent dans la zone ferme avec des sprites distincts selon leur stade (Bébé marron, Adulte noir et blanc, Productive dorée). Des étiquettes d'état et des barres de progression sont affichées au-dessus de chaque animal. La caméra suit le joueur avec un effet fluide.

### Marché et achats

![Marché](docs/images/screen_marche.png)

En zone marché, les 4 vendeurs sont des objets positionnés dans le monde. Quand le joueur s'approche à moins de 90 px, un anneau pulsé jaune s'affiche avec le hint « [R] acheter ». Le popup d'achat (en bas au centre) liste les articles, leurs prix et les verrous de niveau. Pendant un achat, une barre cyan apparaît au-dessus du joueur.

### Combat — Vague alien

![Combat vague](docs/images/screen_combat_vague.png)

Lors d'une vague, les aliens (sprites Kenney UFO manned) approchent depuis le bord droit, tremblent en phase COMBAT, puis fuient (victoire) ou repartent avec une vache (défaite). L'overlay semi-transparent affiche les barres de PV des deux camps.

### Combat final — Boss

![Combat boss](docs/images/screen_boss.png)

Le boss (alien plus grand, rose, avec variantes de dégâts visuels) approche en fin de barre de progression. Le résultat — VICTOIRE ou DÉFAITE — s'affiche en overlay avec le gain d'or associé.

### Boutique d'améliorations

![Boutique](docs/images/screen_upgrade_shop.png)

Entre les niveaux, l'overlay de la boutique présente 4 cartes colorées. La carte sélectionnée est surlignée avec sa couleur thématique (bleu HP, rouge dégâts, vert vaches, or monnaie). Le coût s'affiche en rouge si les fonds sont insuffisants.

### Tests unitaires

185 tests JUnit 5 passent sans erreur :

| Classe de test | Tests |
|---------------|-------|
| JoueurTest | 25 |
| PartieTest | 27 |
| CarteTest | 18 |
| MarcheTest | 12 |
| FermeTest | 14 |
| VacheTest | 12 |
| ActionDureeTest | 10 |
| NiveauTest | 11 |
| BarreProgressionTest | 12 |
| CombatUnitTest | 11 |
| AttaqueTest | 8 |
| ControleurAttaqueTest | 9 |
| ControleurCombatTest | 6 |
| ControleurMarcheTest | 10 |
| **Total** | **185** |

```
[INFO] Tests run: 185, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 7. Documentation utilisateur

### Prérequis

1. **Java 17+** : Vérifier avec `java --version`.
2. **Apache Maven 3.8+** : Vérifier avec `mvn --version`.

### Compilation et lancement

Dans le dossier `farmdefense/` :

```bash
mvn compile exec:java
```

Ou en deux étapes :

```bash
mvn compile
java -cp target/classes com.fermedefense.Main
```

### Comment jouer

#### Objectif

Gérer votre ferme, acheter des armes et défendre vos vaches contre les extraterrestres. Survivez le plus longtemps possible et battez le boss de fin de niveau pour progresser.

#### Déplacement

| Touche | Action |
|--------|--------|
| Flèches directionnelles ou Z/Q/S/D | Déplacer le fermier |
| P | Pause / Reprendre |

Le monde est divisé en deux zones :
- **Gauche (0–1 000 px)** : la ferme — vos vaches s'y trouvent.
- **Droite (1 000–2 000 px)** : le marché — achetez vaches et armes.

#### Gestion de la ferme

- Vos vaches évoluent automatiquement : **Bébé** (marron) → **Adulte** (noir et blanc) → **Productive** (dorée).
- En phase **Productive**, elles accumulent de l'or visible comme un badge au-dessus d'elles.
- Approchez-vous d'une vache productive et appuyez sur **R** pour récolter l'or. Une barre cyan s'affiche pendant la récolte (2 secondes).

#### Achats au marché

- Approchez-vous d'un vendeur (un anneau jaune pulsé apparaît).
- Appuyez sur **R** pour ouvrir le menu d'achat.
- Naviguez avec **HAUT/BAS**, achetez avec **R** (un achat prend 1,5 secondes).
- Les articles grisés nécessitent un niveau supérieur au vôtre.

#### Combat

- Des **vagues d'aliens** apparaissent automatiquement selon la barre de progression en bas de l'écran (marqueurs triangulaires jaunes).
- Soyez dans la ferme pour défendre vos vaches — le combat est automatique, votre arme équipée s'en charge.
- Si vous perdez un combat intermédiaire, un alien repart avec une vache. Si vous perdez le boss final, c'est **Game Over**.

#### Boutique d'améliorations

- Après chaque victoire de niveau, une boutique apparaît.
- Naviguez avec **← →**, achetez avec **ENTRÉE**, passez au niveau suivant avec **ESPACE**.

#### Fin de partie

- En cas de Game Over, saisissez vos **3 initiales** pour enregistrer votre score.
- Consultez le tableau des meilleurs scores depuis le menu principal.

---

## 8. Documentation développeur

Cette section s'adresse à toute personne souhaitant reprendre, améliorer ou étendre le projet.

### 8.1 Point d'entrée et classes à explorer en premier

| Classe | Paquet | Rôle |
|--------|--------|------|
| `Main` | `com.fermedefense` | Point d'entrée. Instancie tous les objets et lance la vue. **À lire en premier.** |
| `VuePrincipale` | `vue/` | JFrame principal + boucle Timer 60 FPS + KeyListener. |
| `ControleurJeu` | `controleur/` | Orchestrateur : gère le tick global, la progression et les transitions d'état. |
| `Partie` | `modele/jeu/` | Machine à états (`EtatJeu`) — point de vérité de l'état du jeu. |
| `Constantes` | `utilitaire/` | Toutes les constantes globales modifiables. |

### 8.2 Arborescence du projet

```
farmdefense/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/fermedefense/
│   │   │   ├── Main.java
│   │   │   ├── modele/
│   │   │   │   ├── jeu/          ← Partie, EtatJeu, Carte, Zone,
│   │   │   │   │                    TableauScores, Succes, GestionnaireSucces
│   │   │   │   ├── joueur/       ← Joueur, Action, ActionDuree, Upgrades
│   │   │   │   ├── ferme/        ← Animal, Vache, EtatVache, Ferme
│   │   │   │   ├── marche/       ← Marche, ArticleMarche, TypeArticle, ResultatAchat
│   │   │   │   ├── combat/       ← Arme, Extraterrestre, BossFinal, Attaque,
│   │   │   │   │                    AlienVisuel, EtatVisuel, ResultatCombat
│   │   │   │   └── progression/  ← Niveau, BarreProgression, EvenementTemporel
│   │   │   ├── vue/
│   │   │   │   ├── VuePrincipale.java    ← Fenêtre + boucle + input
│   │   │   │   ├── Camera.java           ← Monde → écran
│   │   │   │   ├── MondeRenderer.java    ← Tilemap avec culling
│   │   │   │   ├── TileManager.java      ← Chargement PNG tuiles
│   │   │   │   ├── VueFerme.java         ← Rendu vaches + sprites
│   │   │   │   ├── VueHUD.java           ← Barre HP, or, niveau, timer
│   │   │   │   ├── VueMarche.java        ← Objets vendeurs monde
│   │   │   │   ├── VendeurMarche.java    ← Un vendeur (position, items)
│   │   │   │   ├── VueMarchePopup.java   ← Anneau + popup achat
│   │   │   │   ├── VueAliens.java        ← Dessine aliens/boss
│   │   │   │   ├── CacheSpritesAliens.java ← Chargement PNGs aliens
│   │   │   │   ├── VueIndicateurs.java   ← Flèches de navigation bord
│   │   │   │   ├── VueCombat.java        ← Overlay HP combat
│   │   │   │   ├── VueBarreProgression.java ← Timer + marqueurs
│   │   │   │   ├── VueActionJoueur.java  ← Barre cooldown au-dessus joueur
│   │   │   │   ├── VueUpgrades.java      ← Boutique overlay
│   │   │   │   ├── VueTableauScores.java ← Rendu top 10
│   │   │   │   ├── VueEffetHit.java      ← Burst d'impact
│   │   │   │   ├── VueEffetTexte.java    ← Textes flottants (+Xg, -X)
│   │   │   │   └── VueInventaire.java    ← Grille inventaire sidebar
│   │   │   ├── controleur/
│   │   │   │   ├── ControleurJeu.java    ← Boucle principale + niveau
│   │   │   │   ├── ControleurJoueur.java ← Mouvement + récolte
│   │   │   │   ├── ControleurAttaque.java ← Vagues + phases visuelles
│   │   │   │   ├── ControleurCombat.java ← Boss + phases visuelles
│   │   │   │   └── ControleurMarche.java ← Logique d'achat
│   │   │   └── utilitaire/
│   │   │       └── Constantes.java
│   │   └── resources/
│   │       ├── images/
│   │       │   ├── tiles/tt/             ← tile_0000.png … tile_0131.png
│   │       │   ├── player/               ← boy_{down,up,left,right}_{1,2}.png
│   │       │   └── aliens/               ← shipGreen_manned.png, etc.
│   │       └── maps/
│   │           ├── farm_map.txt          ← Grille 25×35 zone ferme
│   │           └── market_map.txt        ← Grille 25×35 zone marché
│   └── test/java/com/fermedefense/      ← 185 tests JUnit 5
├── docs/                                 ← Documentation + images rapport
├── HANDOFF.md
├── PLAN.md
└── RAPPORT.md                           ← Ce fichier
```

### 8.3 Constantes principales à modifier

Toutes les constantes se trouvent dans `utilitaire/Constantes.java` ou comme champs statiques des classes concernées.

| Constante | Classe | Effet |
|-----------|--------|-------|
| `LARGEUR_VIEWPORT / HAUTEUR_VIEWPORT` | `Constantes` | Taille de la fenêtre de jeu |
| `PLAYER_SIZE` | `VuePrincipale` | Taille du sprite joueur |
| `vitesse` (Joueur) | `Joueur` | Vitesse de déplacement (px/s) |
| `TAILLE_TUILE` | `Constantes` | Taille d'une tuile (40 px par défaut) |
| `tempsBebeMs / tempsAdulteMs` | `Vache` | Durée des phases de croissance |
| `cycleProdMs / revenuParCycle` | `Vache` | Cadence et montant de production |
| `RAYON` | `VendeurMarche` | Rayon de proximité vendeur (90 px) |
| Durée `Arme.EPEE` | `Arme` | Cooldown de l'épée (1 000 ms) |
| `BossFinal.pourNiveau(n)` | `BossFinal` | Formule de scaling du boss |
| `COUTS[]` | `VueUpgrades` | Prix des 4 améliorations |
| `monnaieInitiale` | `Joueur` | Or de départ (200g par défaut) |

### 8.4 Ajouter un nouvel upgrade

1. Ajouter les constantes (nom, coût, couleur, description) dans `VueUpgrades` en incrémentant `NB`.
2. Si cela nécessite un multiplicateur persistant, l'ajouter dans `Upgrades.java`.
3. Appliquer l'effet dans `VuePrincipale.actionKeyListener` (case `UPGRADE_SHOP`, touche ENTRÉE).
4. Si le bonus doit s'appliquer chaque niveau, l'intégrer dans `ControleurJeu.initialiserNiveau()`.

### 8.5 Ajouter un nouveau succès

1. Ajouter l'entrée dans l'enum `Succes` avec son `nom`, `description` et `seuil`.
2. Appeler `gestionnaireSucces.verifier(Succes.MON_SUCCES, valeur)` à l'endroit approprié dans le code.
3. Le reste (déclenchement, notification, sauvegarde) est géré automatiquement par `GestionnaireSucces`.

### 8.6 Fonctionnalités non implémentées — perspectives

1. **Sons et musique** — Actuellement absent. Ajouter un `SoundManager` (Singleton) avec `Clip` Java Sound. Les hooks naturels : démarrage de niveau, coup d'arme, achat, death alien, boss final.

2. **Écran d'accueil animé** — La vue démarre directement en jeu. Ajouter un `EcranAccueil extends JPanel` avec un `CardLayout` dans `VuePrincipale`.

3. **Nouvelles armes** — Le modèle `Arme` est générique (dégâts + cooldown). Il suffit d'ajouter des constantes statiques supplémentaires et de les référencer dans `Marche`.

4. **Mode multijoueur local** — L'architecture MVC sépare clairement le modèle de la vue ; un second `ControleurJoueur` lisant des touches différentes pourrait contrôler un second `Joueur` dans le même monde.

---

## 9. Conclusion et perspectives

### Réalisations

Ce projet a abouti à un jeu de stratégie/défense complet et jouable. Toutes les fonctionnalités prévues au cahier des charges ont été implémentées : déplacement libre dans un monde tilemap de 2 000 × 1 400 px avec caméra, cycle de vie des vaches (Bébé → Adulte → Productive), système économique avec 4 vendeurs en monde ouvert, vagues d'aliens avec phases visuelles animées (approche/combat/départ), boss final avec scaling par niveau, boutique d'améliorations entre les niveaux, tableau des meilleurs scores persistant sur disque, et 5 succès débloquables. L'ensemble est couvert par **185 tests unitaires** JUnit 5 qui passent sans erreur.

### Difficultés rencontrées et solutions

La principale difficulté a été la **gestion de la boucle de jeu temps réel** avec le modèle événementiel de Swing. L'utilisation d'un `Timer` Swing (plutôt qu'un thread séparé) garantit que tous les accès au modèle se font sur l'EDT, évitant les problèmes de concurrence sans recourir à des `synchronized` complexes.

Une seconde difficulté a été la **coordination des phases visuelles** des combats (approche/combat/départ) avec la logique de jeu. Cela a été résolu par une machine à états `PhaseAttaque` dédiée dans `ControleurAttaque`, indépendante de la logique de combat (`Attaque`), ce qui permet de tester les deux séparément.

La **gestion de la caméra** dans un monde plus grand que le viewport a nécessité une réflexion soignée sur la séparation coordonnées-monde / coordonnées-écran. La classe `Camera` centralisée, utilisée par tous les renderers, a évité les bugs de décalage.

### Apprentissages

- Maîtrise de l'architecture **MVC** dans un jeu temps réel avec Swing.
- Conception d'une **machine à états** pour orchestrer des flux complexes (combats en phases, transitions entre états de jeu).
- Utilisation avancée de `Graphics2D` (sprites, `RenderingHints`, transformations de coordonnées, overlays semi-transparents).
- Importance du **delta-time** pour un mouvement indépendant du FPS.
- Valeur des **tests unitaires** : les 185 tests ont permis de détecter des régressions à plusieurs reprises, notamment sur la logique de combat et la barre de progression.

### Perspectives

L'architecture du projet est extensible. Les évolutions naturelles seraient :

- **Ambiance sonore** : effets et musique de fond (la structure `SoundManager` est prête à être ajoutée).
- **Écran d'accueil** avec animation et meilleur score affiché dès le lancement.
- **Nouvelles armes et ennemis** : le modèle `Arme` et `Extraterrestre` sont génériques et facilement étendables.
- **Mode survie infini** : remplacer la condition de fin de niveau par une difficulté croissante sans limite.
- **Animations de transitions** : fondu lors des changements d'état (victoire → boutique → niveau suivant).

---

## Annexe — Prompts pour la génération des diagrammes UML

> Les diagrammes UML référencés dans ce rapport (section 5 et section 4) sont à générer via un agent de génération d'images. Voici les prompts à utiliser.

### Prompt 1 — Diagramme de blocs fonctionnels (section 4)

```
Generate a functional block diagram (architecture overview) for a Java game called "Alien Farm Defense".
The diagram should show rectangular blocks connected by labeled arrows (information flow).
Blocks: Main, Game Loop (Timer 60fps), Player Controller, Market Controller,
Farm (cows), Attack Controller (alien waves), Combat Controller (boss),
Time Progression Bar, Game State Machine (Partie), Main View (VuePrincipale),
Camera, Score/Achievement Manager.
Arrows showing: Main → Game Loop, Game Loop → all controllers,
controllers ↔ State Machine, all renderers ← Camera,
State Machine → Main View (current state).
Style: clean dark background, pastel colored boxes grouped by MVC layer
(Model=blue, View=green, Controller=orange), white arrows with labels.
```

### Prompt 2 — Diagramme de classes F1 (Joueur + Camera)

```
Generate a UML class diagram for the player and camera system of a Java game.
Classes:
- Joueur: fields x:double, y:double, vitesse:double, pointsDeVie:int, pointsDeVieMax:int, monnaie:int, arme:Arme, directionCourante:Action; methods mettreAJour(deltaMs:long), subirDegats(int), soigner(int), depenser(int):boolean, ajouterMonnaie(int)
- Action: enum HAUT, BAS, GAUCHE, DROITE
- Camera: fields offsetX:double, offsetY:double; methods centrerSur(Joueur), toScreenX(double):int, toScreenY(double):int
- Carte: fields LARGEUR:int=2000, HAUTEUR:int=1400; methods clampJoueur(Joueur), getZone(double x):Zone
- Zone: enum FERME, MARCHE
Relationships: Joueur uses Action, Camera depends on Joueur, Carte uses Zone.
UML style, white background, standard notation.
```

### Prompt 3 — Diagramme de classes F3 (Ferme + Vache)

```
Generate a UML class diagram for the farm and cow subsystem of a Java game.
Classes:
- Animal (abstract): fields nom:String, x:double, y:double; abstract methods mettreAJour(long), isProductif():boolean, getRevenusParCycle():int
- Vache extends Animal: fields etat:EtatVache, tempsEcoule:long, tempsBebeMs:long=10000, tempsAdulteMs:long=15000, cycleProdMs:long=8000, revenuParCycle:int=10, monnaieAccumulee:int; methods recolter():int, getProgression():double
- EtatVache: enum BEBE, ADULTE, PRODUCTIVE
- Ferme: fields vaches:List<Vache>, capaciteMax:int=10; methods ajouterVache(Vache):boolean, recolterTout():int, enleverDerniereVache():Vache, mettreAJour(long)
UML style, white background.
```

### Prompt 4 — Diagramme de classes F5 (Combat)

```
Generate a UML class diagram for the combat system of a Java game.
Classes:
- Arme: fields nom:String, degats:int, cooldownMs:long; static EPEE:Arme
- Extraterrestre: fields nom:String, pointsDeVie:int, pointsDeVieMax:int, degats:int, cooldownMs:long; methods subirDegats(int), isVivant():boolean, reinitialiser()
- BossFinal extends Extraterrestre: fields recompense:int; static pourNiveau(n:int):BossFinal
- Attaque: fields aliens:List<Extraterrestre>, indexAlienCourant:int, tempsCooldownJoueur:long, tempsCooldownAlien:long, resultat:ResultatCombat; methods mettreAJour(long, Joueur, Arme)
- ResultatCombat: enum EN_COURS, VICTOIRE, DEFAITE
- AlienVisuel: fields x:double, y:double, cibleX:double, cibleY:double, etatVisuel:EtatVisuel; methods mettreAJour(long)
- EtatVisuel: enum APPROCHE, COMBAT, FUITE, ENLEVEMENT
UML style, white background.
```

### Prompt 5 — Diagramme de classes F6 (Progression)

```
Generate a UML class diagram for the time progression and level system of a Java game.
Classes:
- Niveau: fields numero:int; methods getDureeMs():long, getNombreVagues():int, creerVague(i:int):List<Extraterrestre>, creerBoss():BossFinal
- EvenementTemporel: fields momentMs:long, type:TypeEvenement, indexVague:int, declenche:boolean
- TypeEvenement: enum ATTAQUE_INTERMEDIAIRE, COMBAT_FINAL
- BarreProgression: fields evenements:List<EvenementTemporel>, tempsEcoule:long, dureeNiveauMs:long, enPause:boolean; methods mettreAJour(long):List<EvenementTemporel>, getProgression():double, mettreEnPause(), reprendre(), isTerminee():boolean
UML style, white background.
```

### Prompt 6 — Diagramme de classes F7 (Upgrades)

```
Generate a UML class diagram for the upgrade shop system of a Java game.
Classes:
- Upgrades: fields dommageMulti:double=1.0, cowSpeedMulti:double=1.0, startingGoldBonus:int=0
- VueUpgrades: fields NB:int=4, NOMS:String[], COUTS:int[], selectionIndex:int; methods selectionPrecedente(), selectionSuivante(), dessiner(Graphics2D, int, int, Joueur), getCoutSelectionne():int
- EtatJeu: enum MENU, EN_COURS, COMBAT_FINAL, UPGRADE_SHOP, VICTOIRE, DEFAITE
VueUpgrades depends on Upgrades and Joueur. ControleurJeu uses EtatJeu.
UML style, white background.
```

### Prompt 7 — Diagramme de classes F9 (Succès)

```
Generate a UML class diagram for the achievement system of a Java game.
Classes:
- Succes: enum PREMIER_SANG, FERMIER_PROSPERE, EXTERMINATEUR, INDESTRUCTIBLE, RANCHER; fields nom:String, description:String, seuil:int
- GestionnaireSucces: fields debloquees:Set<Succes>, compteurs:Map<Succes,Integer>; methods verifier(Succes, int), estDebloque(Succes):boolean, getDebloquees():Set<Succes>
- TableauScores: fields scores:List<ScorePartie>; methods ajouter(String, int, int), sauvegarder(), charger()
- ScorePartie: fields initiales:String, score:int, niveau:int; implements Serializable
GestionnaireSucces uses Succes. TableauScores contains ScorePartie list.
UML style, white background.
```

### Prompt 8 — Diagramme de classes global (simplifié)

```
Generate a simplified global UML class diagram for a Java MVC game called "Alien Farm Defense".
Show the main classes grouped by layer:

MODEL layer: Joueur, Ferme, Vache, Partie (EtatJeu), Marche, Attaque, BarreProgression, Niveau, Upgrades, GestionnaireSucces, TableauScores

CONTROLLER layer: ControleurJeu, ControleurJoueur, ControleurAttaque, ControleurCombat, ControleurMarche

VIEW layer: VuePrincipale, Camera, MondeRenderer, VueFerme, VueAliens, VueMarchePopup, VueHUD, VueBarreProgression, VueUpgrades

Show key dependencies between layers with dashed arrows (controllers read/write model, views read model through camera).
Keep class boxes minimal (name only, no fields).
Color by layer: blue=model, orange=controller, green=view.
Clean UML style, white background.
```
