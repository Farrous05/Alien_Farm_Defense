package com.fermedefense.utilitaire;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Gestionnaire audio simple pour les musiques et effets du jeu.
 */
public final class SoundManager {

    private static final String MENU_THEME = "Menu Theme.wav";
    private static final String EXPLORATION_THEME = "Through Mountains.wav";
    private static final String BATTLE_THEME = "Intense Battle Theme.wav";
    private static final String UI_CLICK = "click.wav";
    private static final String JINGLE = "homepage.wav";

    private static Clip musiqueCourante;
    private static String musiqueCouranteNom;

    private SoundManager() {}

    public static synchronized void jouerThemeMenu() {
        jouerMusiqueEnBoucle(MENU_THEME);
    }

    public static synchronized void jouerThemeExploration() {
        jouerMusiqueEnBoucle(EXPLORATION_THEME);
    }

    public static synchronized void jouerThemeCombat() {
        jouerMusiqueEnBoucle(BATTLE_THEME, MENU_THEME);
    }

    public static void jouerClic() {
        jouerEffet(UI_CLICK);
    }

    public static void jouerJingle() {
        jouerEffet(JINGLE);
    }

    public static synchronized void arreterMusique() {
        if (musiqueCourante != null) {
            musiqueCourante.stop();
            musiqueCourante.close();
            musiqueCourante = null;
            musiqueCouranteNom = null;
        }
    }

    private static synchronized void jouerMusiqueEnBoucle(String nomFichier) {
        jouerMusiqueEnBoucle(nomFichier, null);
    }

    private static synchronized void jouerMusiqueEnBoucle(String nomFichier, String fallback) {
        if (nomFichier.equals(musiqueCouranteNom)) {
            return;
        }
        arreterMusique();
        try {
            Clip clip = ouvrirClip(nomFichier);
            if (clip == null && fallback != null) {
                clip = ouvrirClip(fallback);
                nomFichier = fallback;
            }
            if (clip == null) {
                return;
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            musiqueCourante = clip;
            musiqueCouranteNom = nomFichier;
        } catch (RuntimeException e) {
            // Ne jamais casser la boucle de jeu à cause d'un souci audio local.
            arreterMusique();
        }
    }

    private static void jouerEffet(String nomFichier) {
        try {
            Clip clip = ouvrirClip(nomFichier);
            if (clip == null) {
                return;
            }
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP
                        || event.getType() == LineEvent.Type.CLOSE) {
                    clip.close();
                }
            });
            clip.start();
        } catch (RuntimeException e) {
            // Effet ignoré si backend audio indisponible.
        }
    }

    private static Clip ouvrirClip(String nomFichier) {
        Path chemin = resoudreChemin(nomFichier);
        if (chemin == null) {
            return null;
        }
        try (var flux = AudioSystem.getAudioInputStream(chemin.toFile())) {
            Clip clip = AudioSystem.getClip();
            clip.open(flux);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Path resoudreChemin(String nomFichier) {
        Path relatif = Paths.get("media", nomFichier);
        if (Files.isRegularFile(relatif)) {
            return relatif;
        }

        Path depuisUserDir = Paths.get(System.getProperty("user.dir"), "media", nomFichier);
        if (Files.isRegularFile(depuisUserDir)) {
            return depuisUserDir;
        }

        Path base = Paths.get(System.getProperty("user.dir"));
        for (int i = 0; i < 5 && base != null; i++) {
            Path candidate = base.resolve("media").resolve(nomFichier);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            base = base.getParent();
        }

        Path simple = Paths.get(nomFichier);
        if (Files.isRegularFile(simple)) {
            return simple;
        }
        return null;
    }
}