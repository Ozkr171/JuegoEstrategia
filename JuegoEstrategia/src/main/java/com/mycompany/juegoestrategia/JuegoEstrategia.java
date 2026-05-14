package com.mycompany.juegoestrategia;

import javax.swing.*;
import java.awt.*;

public class JuegoEstrategia extends JFrame {
    
    public JuegoEstrategia() {
        // Forzar el diseño visual del sistema operativo
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        setTitle("Lanzador del Sistema de Guerra");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(20, 25, 30));
        
        // Título del juego
        JLabel titulo = new JLabel("SIMULADOR DE CONFLICTO", SwingConstants.CENTER);
        titulo.setFont(new Font("Monospaced", Font.BOLD, 24));
        titulo.setForeground(Color.CYAN);
        add(titulo, BorderLayout.CENTER);
        
        // Botón de despliegue
        JButton btnIniciar = new JButton("INICIAR BATALLA LOCAL");
        btnIniciar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnIniciar.setBackground(new Color(200, 50, 50));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);
        
        // Acción al presionar el botón
        btnIniciar.addActionListener(e -> {
            btnIniciar.setEnabled(false);
            btnIniciar.setText("Desplegando conexiones...");
            
            // 1. Iniciar el Servidor (Cuartel General) en un Hilo separado
            new Thread(() -> {
                Servidor cuartel = new Servidor();
                cuartel.iniciar();
            }).start();
            
            // 2. Pausa táctica de medio segundo para asegurar que el servidor abra el puerto
            try { Thread.sleep(500); } catch (InterruptedException ex) {}
            
            // 3. Desplegar Cliente 1 (General 1) en su propio Hilo
            new Thread(() -> {
                new Cliente().conectar();
            }).start();
            
            // 4. Desplegar Cliente 2 (General 2) en su propio Hilo
            new Thread(() -> {
                new Cliente().conectar();
            }).start();
            
            // 5. Cerrar y destruir esta ventana de inicio para no consumir memoria
            this.dispose();
        });
        
        add(btnIniciar, BorderLayout.SOUTH);
        setLocationRelativeTo(null); // Centrar en la pantalla
    }

    public static void main(String[] args) {
        // Iniciar la interfaz gráfica de forma segura
        SwingUtilities.invokeLater(() -> {
            new JuegoEstrategia().setVisible(true);
        });
    }
}