# Handoff — Alien Farm Defense (PCII 2026)

## Project
Java 17 + Maven + Swing, MVC architecture.
Working dir: `/Users/faresshretah/code/projet_pcii/farmdefense`
Package root: `com.fermedefense`
Build: `mvn compile` → **CLEAN** (0 errors, 55 source files).
Run: `mvn exec:java` or run `Main.java`

---

## World & Window Dimensions (Constantes.java)

```
LARGEUR_CARTE    = 2000   (world width, 2 zones × 25 cols × 40px tile)
HAUTEUR_CARTE    = 1400   (world height, 35 rows × 40px tile)
LARGEUR_VIEWPORT = 1200
HAUTEUR_VIEWPORT = 840
LARGEUR_SIDEBAR  = 240
LARGEUR_FENETRE  = 1440   (viewport + sidebar)
HAUTEUR_FENETRE  = 900    (viewport + HUD)
```

Farm zone: `x=0..1000`, Market zone: `x=1000..2000`. Split at `LARGEUR_CARTE/2 = 1000`.
Camera centers on the player, clamped to world bounds.

---

## ✅ DONE (this session)

### Camera System — `vue/Camera.java`
- `centrerSur(Joueur)` centers viewport on player, clamped to world edges
- `toScreenX/Y(worldCoord)` converts world → screen coordinates
- Used by all renderers: `MondeRenderer`, `VueFerme`, `VueAliens`, `VueMarchePopup`, `VueIndicateurs`

### Tile-Map World — `vue/TileManager.java` + `vue/MondeRenderer.java`
- 132 Kenney Tiny Town tiles (16×16px source, rendered at 40×40px)
- Tiles in `src/main/resources/images/tiles/tt/tile_0000.png` … `tile_0131.png`
- Maps: `src/main/resources/maps/farm_map.txt` (25×35 grid) and `market_map.txt` (25×35 grid)
- `MondeRenderer.draw(Graphics2D, Camera)` — camera-aware culling, only draws visible tiles
- Divider: animated blue-dot line at world x=1000

### Player Sprites — `vue/VuePrincipale.java`
- Blue Boy Adventure walk sprites: `src/main/resources/images/player/boy_{down,up,left,right}_{1,2}.png`
- 4 directions × 2 frames, 180ms per frame, `PLAYER_SIZE = 48px`
- Direction tracked from `joueur.getDirectionCourante()`

### Market Redesign — `vue/VueMarche.java`, `vue/VendeurMarche.java`, `vue/VueMarchePopup.java`
- **No zone detection** — replaced with proximity detection (`VendeurMarche.RAYON = 90px`)
- 4 world vendor objects in market zone:
  - Forge — Armes at (1120, 260)
  - Élevage — Vaches at (1400, 460)
  - Apothicaire at (1680, 260)
  - Armurerie — Bombes at (1280, 700)
- When player is within 90px: yellow pulsing ring + "[R] acheter" hint drawn by `VueMarchePopup.dessinerVendeur()`
- `VueMarchePopup.dessinerPopup()`: semi-transparent bottom-center popup, shows items, prices, level locks
- Keys: UP/DOWN navigate vendor selection; R buys selected item
- `ControleurMarche.acheter(ArticleMarche)` overload added

### Cow Sprites — `src/main/resources/images/`
- Source: LPC Farm Animals (OpenGameArt, CC-BY) — sprite sheets `vache_walk.png`, `vache_productive_new.png` (kept as source)
- **Bébé** (`vache_bebe.png`): warm brown/tan, walking right frame
- **Adulte** (`vache_adulte.png`): black-and-white Holstein, standard frame
- **Productive** (`vache_productive.png`): golden/amber, head-down eating pose
- All same pixel art style, all clearly distinct colors

### Cow Rendering — `vue/VueFerme.java`
- `COW_SIZE = 96px` (up from 48)
- State name label drawn above each cow: "Bébé" (tan), "Adulte" (light blue), "Productive" (gold)
- Progress bar beneath, gold badge shows accumulated money

### Alien Sprites — `vue/CacheSpritesAliens.java`
- Now uses **manned** variants: `shipGreen_manned.png`, `shipYellow_manned.png`, `shipBeige_manned.png`, `shipPink_manned.png`
- Source: `farmdefense/kenney_alien-ufo-pack/PNG/` (already on disk, CC0)
- Copied to `src/main/resources/images/aliens/`
- Boss damage variants: `shipPink_damage.png` (33–66% HP), `shipPink_damage1.png` (<33%)
- Procedural fallbacks still exist if PNGs missing

### Alien Targeting — `controleur/ControleurAttaque.java`
- Each alien now targets the world position of a real cow: `vaches.get(i % vaches.size())`
- Aliens spawn at `fermeX + fermeW + 100` (x=1100, visible in the 1200px viewport immediately)
- Fallback to fixed farm positions if no cows deployed

### Combat Overlay — `vue/VueCombat.java` gated in `VuePrincipale`
- The HP-bar overlay (`VueCombat.dessiner`) **only renders while [A] is held down**
- `aEstPresse` flag: set true in `keyPressed(A)`, false in `keyReleased(A)`
- Aliens still approach and fight visually even without the overlay

### Navigation Arrows — `vue/VueIndicateurs.java`
- Screen-edge arrows pointing to off-screen cows and vendors
- Floating down-arrow above on-screen targets (pulses + bobs)
- Colors: Bébé=green, Adulte=cyan, Productive=gold, Vendors=gold
- Short label under each edge arrow ("Bébé", "Armes", etc.)
- Called in `paintComponent` after `vueFerme.dessiner()`

---

## ❌ TODO (Pending — see PLAN.md for details)

1. **Local Leaderboard** — top-10 scores saved to disk, shown on game over + main menu
2. **Achievement System** — 5 unlockable achievements, shown as sidebar badges
3. **Upgrade Shop** — between-level shop (HP, damage, cow speed, starting gold)

---

## Key Files Reference

| File | Role |
|------|------|
| `vue/VuePrincipale.java` | Main frame, PanneauJeu (paintComponent), ActionKeyListener |
| `vue/Camera.java` | World→screen coordinate translation |
| `vue/MondeRenderer.java` | Tile-map world rendering (camera-aware) |
| `vue/TileManager.java` | Tile loader + grid renderer |
| `vue/VueFerme.java` | Cow rendering with camera, state labels |
| `vue/VueMarche.java` | World vendor objects list + proximity lookup |
| `vue/VendeurMarche.java` | Single vendor: position, sprite, item list, selection |
| `vue/VueMarchePopup.java` | Vendor ring glow + purchase popup |
| `vue/VueAliens.java` | Alien/boss drawing, bob animation, camera coords |
| `vue/CacheSpritesAliens.java` | Manned PNG sprite loader for aliens |
| `vue/VueIndicateurs.java` | Screen-edge navigation arrows |
| `vue/VueCombat.java` | Combat HP overlay (only shown while A held) |
| `vue/VueEffetHit.java` | Hit burst effects |
| `vue/VueEffetTexte.java` | Floating "+Xg" / "-X" text |
| `vue/VueHUD.java` | Top HUD bar (HP, gold, level, timer) |
| `vue/VueInventaire.java` | Sidebar inventory grid |
| `controleur/ControleurJeu.java` | Main game loop, level flow, tick |
| `controleur/ControleurAttaque.java` | Alien wave logic + visuals |
| `controleur/ControleurCombat.java` | Boss combat logic + visuals |
| `controleur/ControleurMarche.java` | Purchase logic |
| `controleur/ControleurJoueur.java` | Player movement input |
| `modele/jeu/Partie.java` | Game state machine (EtatJeu) |
| `modele/jeu/Carte.java` | World dimensions, zone boundaries, player clamping |
| `modele/joueur/Joueur.java` | Player stats, inventory, weapon |
| `modele/ferme/Ferme.java` | Cow list, growth, harvest |
| `modele/ferme/Vache.java` | Cow model (BEBE→ADULTE→PRODUCTIVE states) |
| `modele/marche/Marche.java` | Item list for all 4 vendor types |
| `modele/progression/Niveau.java` | Per-level config (duration, waves, alien stats) |
| `modele/progression/BarreProgression.java` | Time bar, event scheduling |
| `utilitaire/Constantes.java` | All global constants |

---

## Asset Packs (in `farmdefense/`)

| Pack | Used for |
|------|----------|
| `kenney_tiny-town/` | 132 top-down tiles → world tilemap |
| `kenney_alien-ufo-pack/PNG/` | Manned alien spaceships |
| `kenney_pixel-platformer-farm-expansion/` | Available but no cows found |
| `kenney_pixel-platformer/` | Available but side-view, mostly unused |
| LPC Farm Animals (OpenGameArt CC-BY) | Cow sprites (source sheets kept in `images/`) |
| Blue Boy Adventure (GitHub) | Player walk sprites |
