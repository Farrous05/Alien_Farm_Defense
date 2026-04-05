# Handoff — Alien Farm Defense (PCII 2026)

## Project
Java 17 + Maven + Swing, MVC architecture.
Working dir: `/Users/faresshretah/code/projet_pcii/farmdefense`
Build: `mvn compile` → currently **CLEAN** (0 errors).

Game window: 1120×700 px total. Game area: 900×640 px + sidebar.
- **Farm zone**: LEFT half (x=0–449), `carte.getZoneFerme()` = `{0, 0, 450, 640}`
- **Market zone**: RIGHT half (x=450–899), `carte.getZoneMarche()` = `{450, 0, 450, 640}`

View is **top-down (bird's-eye)**, like a 2D RPG (Zelda-style).

---

## ✅ DONE

### World Rendering — `vue/MondeRenderer.java`
- Full top-down world renderer (NO sky, NO side-view)
- **Farm zone**: grass tiles (Kenney Tiny Town `tt_0000/0001`) tiled across entire floor, barn roof seen from above (dark red rect with ridge), plowed dirt field, fence borders, tree/bush/plant decorations
- **Market zone**: stone tiles (`tt_0097/0098/0099`) tiled across entire floor, two market stalls with striped awnings ("Armes & Armures" / "Potions & Bêtes"), crates, barrels, glowing lamp posts
- **Divider**: thin dark stripe with pulsing blue dots at x=splitX
- Integrated in `VuePrincipale.PanneauJeu.paintComponent` — replaces the old black fill
- `VueFerme.dessiner()` and `VueMarche.dessiner()` no longer draw their own backgrounds

Tile assets in `src/main/resources/images/tiles/`:
- `tt_0000.png` – grass, `tt_0001.png` – grass sparkle
- `tt_0002.png` – autumn tree, `tt_0003.png` – green tree
- `tt_0004.png` – bush, `tt_0005.png` – plant
- `tt_0013.png` – dirt, `tt_0097/0098/0099.png` – stone floor
- Source pack: `farmdefense/kenney_tiny-town/Tiles/`

### Market UI — `vue/VueMarche.java`
- **No modal** — market stall cards always visible in-world (no `[M]` key)
- Cards show: icon, name, type, price (gold), level lock badge ("Niv.X") if locked
- Selection pulsing white border when player is in market zone
- `dessiner(g2, zx, zy, zw, zh, niveauActuel, joueurDansZone)` — new signature
- `VueMarcheModal` fully removed from codebase (was ~17 references in `VuePrincipale`)
- UP/DOWN keys navigate items when in MARCHE zone; `[R]` to buy

### Alien Sprites — `vue/CacheSpritesAliens.java`
- Loads Kenney UFO PNG ships (space ships = aliens attacking the farm)
- NORMAL → `shipGreen.png`, RUNNER → `shipYellow.png`, TANK → `shipBeige.png`, BOSS → `shipPink.png`
- Boss damage states: `getBoss(hpRatio)` — >66% full, 33–66% damage1, <33% damage2
- Procedural fallback if PNG missing

### Alien Animation — `vue/VueAliens.java`
- Sine-bob floating animation: `bob = (int)(BOB_AMP * sin(now * BOB_FREQ + x * 0.05))`
- New overload: `dessiner(g2, aliens, boss, hpRatio)` — boss sprite changes with HP
- Boss halo: red normally, orange-red at <33% HP
- Old overload `dessiner(g2, aliens, boss)` calls new with hpRatio=1.0

Alien PNG assets in `src/main/resources/images/aliens/`:
`shipGreen.png`, `shipYellow.png`, `shipBeige.png`, `shipPink.png`, `shipPink_damage.png`, `shipPink_damage1.png`

### Hit Effects — `vue/VueEffetHit.java`
- Player hits alien → green burst (`laserGreen_burst.png`)
- Alien hits player → blue burst (`laserBlue_burst.png`)
- 400ms duration, scales up + fades out
- Triggered in `VuePrincipale.attaquerAvecArmeEquipee()` and HP-drop detection in paintComponent
- Assets in `src/main/resources/images/effects/`

### Screen Shake
- In `VuePrincipale.PanneauJeu.paintComponent`: `g2.translate(sx, sy)` when `now < shakeFinMs`
- Boss combat hit triggers `shakeFinMs = now + 220ms`
- Amplitude: ±4px

### Floating Text — `vue/VueEffetTexte.java`
- `triggerMonnaie(amount, cx, cy)` → "+Xg" in gold, rises 35px, fades 900ms
- `triggerDegats(amount, cx, cy)` → "-X" in red
- Triggered in paintComponent when `monnaieCourante > derniereMonnaie`

---

## ❌ TODO (Pending)

### 1. Local Leaderboard
- `modele/jeu/TableauScores.java`: top-10 list, saved to `~/.alienfarm/scores.dat` via `ObjectOutputStream`
- `vue/VueTableauScores.java`: shown after game over and on main menu
- 3-char initials input (simple text field dialog)
- Hook into `VuePrincipale.dessinerFinDePartie()` and main menu screen

### 2. Achievement System
- `modele/jeu/Succes.java` (enum): 5 achievements
  - `PREMIER_SANG` — first alien killed
  - `FERMIER_PROSPERE` — earn 500g total
  - `EXTERMINATEUR` — kill 50 aliens
  - `INDESTRUCTIBLE` — complete a level at full HP
  - `RANCHER` — have 3 cows simultaneously
- Save alongside scores in same file
- `vue/VueSucces.java` or sidebar badge rendering: 20×20 icon per unlocked achievement, shown in sidebar

### 3. Upgrade Shop (between levels)
- `vue/VueUpgrades.java`: shown during level transition before level N+1 starts
- 4 upgrades purchasable with gold:
  - Max HP +25 → 100g
  - Weapon DMG +10% → 150g
  - Cow speed +20% → 80g
  - Starting gold +50 → 75g
- Store multipliers in `modele/joueur/Joueur.java`
- Hook into `ControleurJeu` level-end flow

---

## Key Files Reference

| File | Role |
|------|------|
| `vue/VuePrincipale.java` | Main frame + PanneauJeu (paintComponent), ActionKeyListener |
| `vue/MondeRenderer.java` | Top-down world background |
| `vue/VueMarche.java` | In-world market stall cards |
| `vue/VueAliens.java` | Alien/boss drawing + bob animation |
| `vue/CacheSpritesAliens.java` | PNG sprite loader for aliens |
| `vue/VueEffetHit.java` | Hit burst effects |
| `vue/VueEffetTexte.java` | Floating "+Xg" / "-X" text |
| `vue/VueFerme.java` | Cow rendering (no background anymore) |
| `vue/VueHUD.java` | Top HUD bar (HP, gold, level) |
| `vue/VueInventaire.java` | Sidebar inventory grid |
| `controleur/ControleurJeu.java` | Main game loop, level flow |
| `controleur/ControleurAttaque.java` | Alien wave combat |
| `controleur/ControleurCombat.java` | Boss combat |
| `controleur/ControleurMarche.java` | Buy logic |
| `modele/jeu/Partie.java` | Game state (niveau, EtatJeu) |
| `modele/joueur/Joueur.java` | Player stats (HP, gold, inventory) |
| `modele/marche/Marche.java` | Market item list |
| `utilitaire/Constantes.java` | LARGEUR_CARTE=900, HAUTEUR_CARTE=640, etc. |

---

## Kenney Asset Packs (all in `farmdefense/`)
- `kenney_tiny-town/` — top-down tiles (grass, stone, building parts) ← **use this**
- `kenney_alien-ufo-pack/PNG/` — spaceship PNGs for aliens
- `kenney_pixel-platformer/` — side-view (mostly unused now)
- `kenney_pixel-platformer-farm-expansion/` — side-view farm (mostly unused)
