package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import java.awt.geom.AffineTransform;

import com.fermedefense.Main;
import com.fermedefense.utilitaire.Constantes;

public class VueMenuPrincipal extends JFrame {

    private class VacheVolante {
        double x, y;
        double dx, dy;
        double rotation;
        double dRotation;
        int taille;

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
            taille = 30 + (int)(Math.random() * 20);
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
            
            // Ombre portée pour l'effet volant
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(-taille/2 + 5, -taille/2 + 5, taille, (int)(taille*0.75), 8, 8);

            // Corps vache
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(-taille/2, -taille/2, taille, (int)(taille*0.75), 8, 8);
            
            // Taches noires "simplistes"
            g2.setColor(Color.BLACK);
            g2.fillOval(-taille/4, -taille/4, taille/3, taille/4);
            g2.fillOval(taille/8, 0, taille/4, taille/5);
            
            // Bordure
            g2.drawRoundRect(-taille/2, -taille/2, taille, (int)(taille*0.75), 8, 8);
            
            g2.setTransform(old);
        }
    }

    private List<VacheVolante> vaches;
    private Timer timer;

    public VueMenuPrincipal() {
        super(Constantes.TITRE_FENETRE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        vaches = new ArrayList<>();
        for(int i = 0; i < 15; i++) {
            vaches.add(new VacheVolante());
        }
        
        // Panneau principal avec un fond sombre
        JPanel panneauMain = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dégradé ou fond simple sombre
                g2.setColor(new Color(20, 30, 40));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Petits détails décoratifs : étoiles
                g2.setColor(new Color(255, 255, 255, 50));
                java.util.Random rand = new java.util.Random(12345);
                for(int i = 0; i < 150; i++) {
                    int px = rand.nextInt(getWidth());
                    int py = rand.nextInt(getHeight());
                    int size = rand.nextInt(3) + 1;
                    g2.fillOval(px, py, size, size);
                }
                
                // Dessin des vaches
                for (VacheVolante v : vaches) {
                    v.draw(g2);
                }
            }
        };
        
        // Timer d'animation (approx 60 FPS)
        timer = new Timer(16, e -> {
            for (VacheVolante v : vaches) {
                v.update();
            }
            panneauMain.repaint();
        });
        timer.start();

        panneauMain.setLayout(new BoxLayout(panneauMain, BoxLayout.Y_AXIS));
        panneauMain.setPreferredSize(new Dimension(800, 600)); // Taille fixe pour le menu
        panneauMain.setBorder(new EmptyBorder(100, 50, 50, 50));
        
        // Titre
        JLabel titre = new JLabel("ALIEN FARM DEFENSE");
        titre.setFont(new Font("SansSerif", Font.BOLD, 48));
        titre.setForeground(new Color(80, 255, 80)); // Vert alien
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Sous-titre
        JLabel sousTitre = new JLabel("Protégez vos vaches. Détruisez les aliens.");
        sousTitre.setFont(new Font("SansSerif", Font.ITALIC, 18));
        sousTitre.setForeground(Color.LIGHT_GRAY);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Boutons
        JButton btnNouvellePartie = creerBouton("Nouvelle Partie");
        btnNouvellePartie.addActionListener(e -> {
            this.dispose(); // Fermer le menu
            Main.lancerJeu(); // Démarrer le jeu
        });
        
        JButton btnQuitter = creerBouton("Quitter");
        btnQuitter.addActionListener(e -> {
            System.exit(0);
        });
        
        // Ajout des composants
        panneauMain.add(Box.createVerticalGlue());
        panneauMain.add(titre);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 10)));
        panneauMain.add(sousTitre);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 80)));
        panneauMain.add(btnNouvellePartie);
        panneauMain.add(Box.createRigidArea(new Dimension(0, 20)));
        panneauMain.add(btnQuitter);
        panneauMain.add(Box.createVerticalGlue());
        
        add(panneauMain);
        pack();
        setLocationRelativeTo(null);
    }
    
    private JButton creerBouton(String texte) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("SansSerif", Font.BOLD, 24));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(50, 60, 80));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 60));
        btn.setPreferredSize(new Dimension(300, 60));
        
        // Hover effect simple
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
