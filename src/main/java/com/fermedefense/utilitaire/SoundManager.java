package com.fermedefense.utilitaire;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * Gestionnaire audio.
 *
 * Musiques WAV  → chargement direct via AudioSystem (approche originale fiable).
 * Effets MP3   → décodés en PCM une seule fois dans un thread de fond,
 *                relus depuis la mémoire sans I/O sur le thread de jeu.
 */
public final class SoundManager {

    // --- Musiques (WAV, chargement direct) ---
    private static final String MENU_THEME   = "homepage.wav";
    private static final String JEU_THEME    = "calm1.wav";
    private static final String BATTLE_THEME = "Intense Battle Theme.wav";
    private static final String UI_CLICK     = "click.wav";

    // --- Effets WAV ---
    private static final String SFX_POTION = "ahh.wav";

    // --- Effets (MP3, mis en cache) ---
    private static final String SFX_VACHE_PRODUCTIVE = "cows-mooing-felix-blume-1-00-02.mp3";
    private static final String SFX_VACHE_VOLEE      = "freesound_community-mad-cow-102659.mp3";
    private static final String SFX_GAME_OVER        = "freesound_community-game-over-arcade-6435.mp3";
    private static final String SFX_PIECE_DOR        = "freesound_crunchpixstudio-drop-coin-384921.mp3";
    private static final String SFX_FONDS_INSUFF     = "freesound_community-wronganswer-37702.mp3";
    private static final String SFX_ACHAT_REUSSI     = "modestas123123-cash-register-kaching-sound-effect-125042.mp3";
    private static final String SFX_COUP_ARME        = "musicholder-sword-sound-260274.mp3";
    private static final String SFX_SUCCES           = "ribhavagrawal-achievement-video-game-type-1-230515.mp3";
    private static final String SFX_PAS              = "abdalrahman_bm-8-bit-grass-footsteps-2-408574.mp3";

    private static final String[] EFFETS_MP3 = {
        SFX_VACHE_PRODUCTIVE, SFX_VACHE_VOLEE, SFX_GAME_OVER,
        SFX_PIECE_DOR, SFX_FONDS_INSUFF, SFX_ACHAT_REUSSI, SFX_COUP_ARME, SFX_SUCCES, SFX_PAS
    };

    // Cache PCM pour les effets MP3 uniquement
    private record CachedAudio(AudioFormat format, byte[] data) {}
    private static final Map<String, CachedAudio> mp3Cache = new ConcurrentHashMap<>();
    private static volatile boolean prechargeDeclenche = false;

    private static Clip   musiqueCourante;
    private static String musiqueCouranteNom;
    private static Clip   musiqueJeuEnPause;
    private static Clip   pasClip;

    private SoundManager() {}

    // ── Pré-chargement des effets MP3 en arrière-plan ────────────────────────

    private static void prechargerSiNecessaire() {
        if (prechargeDeclenche) return;
        prechargeDeclenche = true;
        Thread t = new Thread(() -> {
            for (String sfx : EFFETS_MP3) chargerMP3DansCache(sfx);
        }, "audio-preload");
        t.setDaemon(true);
        t.start();
    }

    /** Décode un MP3 en PCM brut via JLayer (ne touche pas à AudioSystem → pas d'interférence WAV). */
    private static void chargerMP3DansCache(String nom) {
        if (mp3Cache.containsKey(nom)) return;
        Path chemin = resoudreChemin(nom);
        if (chemin == null) return;
        try (FileInputStream fis = new FileInputStream(chemin.toFile())) {
            Bitstream          bitstream = new Bitstream(new BufferedInputStream(fis));
            Decoder            decoder   = new Decoder();
            ByteArrayOutputStream pcmOut = new ByteArrayOutputStream();
            int sampleRate = 44100;
            int channels   = 2;
            Header frame;
            while ((frame = bitstream.readFrame()) != null) {
                if (pcmOut.size() == 0) {
                    sampleRate = frame.frequency();
                    channels   = (frame.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                }
                SampleBuffer buf = (SampleBuffer) decoder.decodeFrame(frame, bitstream);
                short[] samples  = buf.getBuffer();
                int     len      = buf.getBufferLength();
                for (int i = 0; i < len; i++) {
                    short s = samples[i];
                    pcmOut.write(s & 0xFF);
                    pcmOut.write((s >> 8) & 0xFF);
                }
                bitstream.closeFrame();
            }
            bitstream.close();
            byte[] data = pcmOut.toByteArray();
            if (data.length > 0) {
                AudioFormat fmt = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sampleRate, 16, channels, channels * 2, sampleRate, false);
                mp3Cache.put(nom, new CachedAudio(fmt, data));
            }
        } catch (Exception e) {
            // fichier absent ou décodage échoué — ignoré
        }
    }

    // ── Musiques ─────────────────────────────────────────────────────────────

    public static synchronized void jouerThemeMenu() {
        prechargerSiNecessaire();
        fermerMusiqueEnPause();
        jouerMusiqueEnBoucle(MENU_THEME);
    }

    public static synchronized void jouerThemeJeu() {
        prechargerSiNecessaire();
        fermerMusiqueEnPause();
        jouerMusiqueEnBoucle(JEU_THEME);
    }

    public static synchronized void jouerThemeCombat() {
        if (BATTLE_THEME.equals(musiqueCouranteNom)) return;

        if (JEU_THEME.equals(musiqueCouranteNom) && musiqueCourante != null) {
            musiqueCourante.stop();
            musiqueJeuEnPause  = musiqueCourante;
            musiqueCourante    = null;
            musiqueCouranteNom = null;
        } else {
            arreterMusique();
        }

        Clip clip = ouvrirWAV(BATTLE_THEME);
        if (clip == null) return;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        musiqueCourante    = clip;
        musiqueCouranteNom = BATTLE_THEME;
    }

    public static synchronized void reprendreThemeJeu() {
        if (musiqueCourante != null) {
            musiqueCourante.stop();
            musiqueCourante.close();
            musiqueCourante    = null;
            musiqueCouranteNom = null;
        }
        if (musiqueJeuEnPause != null) {
            try {
                musiqueJeuEnPause.loop(Clip.LOOP_CONTINUOUSLY);
                musiqueCourante    = musiqueJeuEnPause;
                musiqueCouranteNom = JEU_THEME;
                musiqueJeuEnPause  = null;
                return;
            } catch (RuntimeException e) {
                musiqueJeuEnPause.close();
                musiqueJeuEnPause = null;
            }
        }
        jouerMusiqueEnBoucle(JEU_THEME);
    }

    public static synchronized void jouerThemeExploration() {
        reprendreThemeJeu();
    }

    // ── Effets ───────────────────────────────────────────────────────────────

    public static void jouerClic()              { jouerEffetWAV(UI_CLICK); }
    public static void jouerPotion()            { jouerEffetWAV(SFX_POTION); }
    public static void jouerVacheProductive()   { jouerEffetMP3(SFX_VACHE_PRODUCTIVE); }
    public static void jouerVacheVolee()        { jouerEffetMP3(SFX_VACHE_VOLEE); }
    public static void jouerGameOver()          { jouerEffetMP3(SFX_GAME_OVER); }
    public static void jouerPieceDor()          { jouerEffetMP3(SFX_PIECE_DOR); }
    public static void jouerFondsInsuffisants() { jouerEffetMP3(SFX_FONDS_INSUFF); }
    public static void jouerAchatReussi()       { jouerEffetMP3(SFX_ACHAT_REUSSI); }
    public static void jouerCoupArme()          { jouerEffetMP3(SFX_COUP_ARME); }
    public static void jouerSucces()            { jouerEffetMP3(SFX_SUCCES); }

    public static synchronized void jouerPas() {
        if (pasClip != null && pasClip.isRunning()) return;
        if (pasClip != null) { pasClip.close(); pasClip = null; }
        CachedAudio c = mp3Cache.get(SFX_PAS);
        if (c == null) return;
        try {
            int frameSize = c.format().getFrameSize();
            long frames   = frameSize > 0 ? c.data().length / frameSize : AudioSystem.NOT_SPECIFIED;
            AudioInputStream stream = new AudioInputStream(
                    new ByteArrayInputStream(c.data()), c.format(), frames);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(-18.0f); // réduit le volume (-18 dB ≈ ~12% du volume original)
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            pasClip = clip;
        } catch (LineUnavailableException | IOException e) {
            // ignoré
        }
    }

    public static synchronized void arreterPas() {
        if (pasClip != null) {
            pasClip.stop();
            pasClip.close();
            pasClip = null;
        }
    }

    // ── Contrôle global ───────────────────────────────────────────────────────

    public static synchronized void arreterMusique() {
        if (musiqueCourante != null) {
            musiqueCourante.stop();
            musiqueCourante.close();
            musiqueCourante    = null;
            musiqueCouranteNom = null;
        }
        fermerMusiqueEnPause();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static synchronized void fermerMusiqueEnPause() {
        if (musiqueJeuEnPause != null) {
            musiqueJeuEnPause.close();
            musiqueJeuEnPause = null;
        }
    }

    private static synchronized void jouerMusiqueEnBoucle(String nom) {
        if (nom.equals(musiqueCouranteNom)) return;
        arreterMusique();
        Clip clip = ouvrirWAV(nom);
        if (clip == null) return;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        musiqueCourante    = clip;
        musiqueCouranteNom = nom;
    }

    /**
     * Charge un fichier WAV directement dans un Clip — approche originale,
     * sans cache, sans conversion (les fichiers musique sont du PCM standard).
     */
    private static Clip ouvrirWAV(String nom) {
        Path chemin = resoudreChemin(nom);
        if (chemin == null) return null;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(chemin.toFile())) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void jouerEffetWAV(String nom) {
        Clip clip = ouvrirWAV(nom);
        if (clip == null) return;
        attacherFermeture(clip);
        clip.start();
    }

    private static void jouerEffetMP3(String nom) {
        CachedAudio c = mp3Cache.get(nom);
        if (c == null) {
            chargerMP3DansCache(nom); // premier appel : synchrone, une seule fois
            c = mp3Cache.get(nom);
        }
        if (c == null) return;
        try {
            int  frameSize = c.format().getFrameSize();
            long frames    = frameSize > 0 ? c.data().length / frameSize : AudioSystem.NOT_SPECIFIED;
            AudioInputStream stream = new AudioInputStream(
                    new ByteArrayInputStream(c.data()), c.format(), frames);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            attacherFermeture(clip);
            clip.start();
        } catch (LineUnavailableException | IOException e) {
            // effet ignoré
        }
    }

    private static void attacherFermeture(Clip clip) {
        clip.addLineListener(e -> {
            if (e.getType() == LineEvent.Type.STOP || e.getType() == LineEvent.Type.CLOSE)
                clip.close();
        });
    }

    private static Path resoudreChemin(String nom) {
        Path p = Paths.get("media", nom);
        if (Files.isRegularFile(p)) return p;
        p = Paths.get(System.getProperty("user.dir"), "media", nom);
        if (Files.isRegularFile(p)) return p;
        Path base = Paths.get(System.getProperty("user.dir"));
        for (int i = 0; i < 5 && base != null; i++) {
            p = base.resolve("media").resolve(nom);
            if (Files.isRegularFile(p)) return p;
            base = base.getParent();
        }
        p = Paths.get(nom);
        return Files.isRegularFile(p) ? p : null;
    }
}
