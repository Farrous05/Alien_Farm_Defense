package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.fermedefense.Main;
import com.fermedefense.modele.jeu.TableauScores;
import com.fermedefense.utilitaire.Constantes;
import com.fermedefense.utilitaire.SoundManager;

public class VueMenuPrincipal extends JFrame {

    private static Image imgBebe;
    private static Image imgAdulte;
    private static Image imgProductive;
    private static Font customFontMain;
    private static Font customFontSub;
    private static Font customFontBtn;

    static {
        try {
            imgBebe = ImageIO.read(VueMenuPrincipal.class.getResource("/images/vache_bebe.png"));
            imgAdulte = ImageIO.read(VueMenuPrincipal.class.getResource("/images/vache_adulte.png"));
            imgProductive = ImageIO.read(VueMenuPrincipal.class.getResource("/images/vache_productive.png"));
            
            InputStream is = VueMenuPrincipal.class.getResourceAsStream("/fonts/PressStart2P.ttf");
            if (is != null) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                customFontMain = baseFont.deriveFont(Font.PLAIN, 36f);
                customFontSub = baseFont.deriveFont(Font.PLAIN, 12f);
                customFontBtn = baseFont.deriveFont(Font.PLAIN, 18f);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement ressources menu: " + e.getMessage());
        }
        
        // Fallback fonts
        if (customFontMain == null) customFontMain = new Font("SansSerif", Font.BOLD, 48);
        if (customFontSub == null) customFontSub = new Font("SansSerif", Font.ITALIC, 18);
        if (customFontBtn == null) customFontBtn = new Font("SansSerif", Font.BOLD, 24);
    }

    private class VacheVolante {
        double x, y;
        double dx, dy;
        double rotation;
        double dRotation;
        int taille;
        Image sprite;

        VacheVolante() {
            x = Math.random() * 800;
            y = Math.random() * 600;
            // Vitesse aléatoire
            dx = (Math.random() - 0.5) * 3;
            dy = (Math.random() - 0.5) * 3;
            if (dx == 0) dx = 1;
            if (dy == 0) dy = 1;
            
            rotation = Math.random() * Math.PI * 2;
            dRotation = (Math.random() - 0.5) * 0.05;
            taille = 30 + (int)(Math.random() * 30);
            
            // Randomly pick a cow sprite
            int randSprite = (int)(Math.random() * 3);
            if(randSprite == 0) sprite = imgBebe;
            else if(randSprite == 1) sprite = imgAdulte;
            else sprite = imgProductive;
        }

        void update() {
            x += dx;
            y += dy;
            rotation += dRotation;
            if (x < -100) x = 900;
            if (x > 900) x = -100;
            if (y < -100) y = 700;
            if (y > 700) y = -100;
        }

        void draw(Graphics2D g2) {
            AffineTransform old = g2.getTransform();
            g2.translate(x, y);
            g2.rotate(rotation);
            
            if (sprite != null) {
                // Ombre
                g2.translate(4, 4);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.4f));
                g2.drawImage(sprite, -taille/2, -taille/2, taille, taille, null);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
                g2.translate(-4, -4);
                
                // Vache
                g2.drawImage(sprite, -taille/2, -taille/2, taille, taille, null);
            } else {
                // Fallback rendering
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(-taille/2, -taille/2, taille, (int)(taille*0.75), 8, 8);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(-taille/2, -taille/2, taille, (int)(taille*0.75), 8, 8);
            }
            
            g2.setTransform(old);
        }
    }

    private List<VacheVolante> vaches;
    private final Timer timer;

    public VueMenuPrincipal() {
        super(Constantes.TITRE_FENETRE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        vaches = new ArrayList<>();
        for(int i = 0; i < 15; i++) {
            vaches.add(new VacheVolante());
        }
        
        JPanel panneauMain = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(20, 30, 40));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(new Color(255, 255, 255, 50));
                java.util.Random rand = new java.util.Random(12345);
                for(int i = 0; i < 150; i++) {
                    int px = rand.nextInt(getWidth());
                    int py = rand.nextInt(getHeight());
                    int size = rand.nextInt(3) + 1;
                    g2.fillOval(px, py, size, size);
                }
                
                for (VacheVolante v : vaches) {
                    v.draw(g2);
                }
            }
        };
        
        timer = new Timer(16, e -> {
            for (VacheVolante v : vaches) {
                v.update();
            }
            panneauMain.repaint();
        });
        timer.start();
        SoundManager.jouerThemeMenu();

        panneauMain.setLayout(new BoxLayout(panneauMain, BoxLayout.Y_AXIS));
        panneauMain.setPreferredSize(new Dimension(800, 600)); 
        panneauMain.setBorder(new EmptyBorder(100, 50, 50, 50));
        
        JLabel titre = new JLabel("ALIEN FARM DEFENSE");
        titre.setFont(customFontMain);
        titre.setForeground(new Color(80, 255, 80)); 
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel sousTitre = new JLabel("Protegez vos vaches. Detruisez les aliens.");
        sousTitre.setFont(customFontSub);
        sousTitre.setForeground(Color.LIGHT_GRAY);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton btnNouvellePartie = creerBouton("Nouvelle Partie");
        btnNouvellePartie.addActionListener(e -> {
            SoundManager.jouerClic();
            SoundManager.arreterMusique();
            this.dispose();
            Main.lancerJeu();
        });

        JButton btnScores = creerBouton("Meilleurs Scores");
        btnScores.addActionListener(e -> {
            SoundManager.jouerClic();
            afficherLeaderboard();
        });

        JButton btnQuitter = creerBouton("Quitter");
        btnQuitter.addActionListener(e -> {
            SoundManager.jouerClic();
            SoundManager.arreterMusique();
            System.exit(0);
        });

        panneauMain.add(Box.createVerticalGlue());
        panneauMain.add(titre);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 20)));
        panneauMain.add(sousTitre);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 60)));
        panneauMain.add(btnNouvellePartie);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 15)));
        panneauMain.add(btnScores);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 15)));
        panneauMain.add(btnQuitter);
        panneauMain.add(Box.createVerticalGlue());
        
        add(panneauMain);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void afficherLeaderboard() {
        TableauScores tableau = TableauScores.charger();
        VueTableauScores vue  = new VueTableauScores();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 30, 40));
                g2.fillRect(0, 0, getWidth(), getHeight());
                vue.dessiner(g2, tableau, getWidth() / 2, getHeight() / 2);
            }
        };
        panel.setPreferredSize(new Dimension(400, 340));

        javax.swing.JOptionPane.showMessageDialog(
                this, panel, "Meilleurs Scores",
                javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private JButton creerBouton(String texte) {
        JButton btn = new JButton(texte);
        btn.setFont(customFontBtn);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(50, 60, 80));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(350, 60));
        btn.setPreferredSize(new Dimension(350, 60));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(70, 80, 100));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(50, 60, 80));
            }
        });
        
        return btn;
    }
}
