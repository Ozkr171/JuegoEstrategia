package com.mycompany.juegoestrategia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.PrintWriter;

public class TableroGrafico extends JFrame {
    private JButton[][] casillas;
    private JTextArea registroAcciones;
    private PrintWriter salidaRed;
    private JLabel indicadorTurno; // Nuevo letrero dinámico
    private boolean esMiTurno = false; // Candado lógico de la interfaz

    public TableroGrafico(PrintWriter salidaRed) {
        this.salidaRed = salidaRed;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        setTitle("État-Major Naval - Centro de Operaciones");
        setSize(700, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(20, 25, 30));

        // Reestructuración del panel superior para incluir el indicador de turno
        JPanel panelSuperior = new JPanel(new GridLayout(2, 1));
        panelSuperior.setBackground(new Color(20, 25, 30));
        
        JLabel titulo = new JLabel("SISTEMA DE CONTROL TERRITORIAL", SwingConstants.CENTER);
        titulo.setFont(new Font("Monospaced", Font.BOLD, 24));
        titulo.setForeground(new Color(200, 200, 200));
        titulo.setBorder(new EmptyBorder(15, 0, 5, 0));
        
        indicadorTurno = new JLabel("Estableciendo enlace satelital...", SwingConstants.CENTER);
        indicadorTurno.setFont(new Font("Monospaced", Font.BOLD, 18));
        indicadorTurno.setForeground(Color.YELLOW);
        indicadorTurno.setBorder(new EmptyBorder(0, 0, 10, 0));

        panelSuperior.add(titulo);
        panelSuperior.add(indicadorTurno);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelMapa = new JPanel();
        panelMapa.setLayout(new GridLayout(5, 5, 2, 2));
        panelMapa.setBackground(new Color(20, 25, 30));
        panelMapa.setBorder(new EmptyBorder(0, 20, 0, 20));

        casillas = new JButton[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                final int fila = i;
                final int col = j;
                casillas[i][j] = new JButton("~");
                casillas[i][j].setBackground(new Color(28, 57, 104)); 
                casillas[i][j].setForeground(new Color(0, 255, 255));
                casillas[i][j].setFont(new Font("Monospaced", Font.BOLD, 20));
                casillas[i][j].setFocusPainted(false);
                casillas[i][j].setBorder(new LineBorder(new Color(0, 100, 200), 1));
                
                casillas[i][j].addActionListener(e -> {
                    // Validamos la variable antes de permitir enviar el mensaje por red
                    if (this.salidaRed != null && esMiTurno) {
                        this.salidaRed.println("ATACAR|" + fila + "|" + col);
                    } else if (!esMiTurno) {
                        agregarMensaje("Error táctico: Aún no es tu turno.");
                    }
                });
                panelMapa.add(casillas[i][j]);
            }
        }

        registroAcciones = new JTextArea(6, 20);
        registroAcciones.setEditable(false);
        registroAcciones.setBackground(new Color(10, 10, 10));
        registroAcciones.setForeground(new Color(0, 255, 0));
        registroAcciones.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        JScrollPane scrollRegistro = new JScrollPane(registroAcciones);
        scrollRegistro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(100, 100, 100)), "Registro de Batalla", 0, 0, 
                new Font("SansSerif", Font.BOLD, 12), Color.LIGHT_GRAY));
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(20, 25, 30));
        panelInferior.setBorder(new EmptyBorder(10, 20, 20, 20));
        panelInferior.add(scrollRegistro, BorderLayout.CENTER);

        add(panelMapa, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    public void agregarMensaje(String mensaje) {
        registroAcciones.append("> " + mensaje + "\n");
        registroAcciones.setCaretPosition(registroAcciones.getDocument().getLength());
    }

    public void registrarImpacto(int fila, int col, int idJugador) {
        JButton boton = casillas[fila][col];
        boton.setEnabled(false);
        if (idJugador == 1) {
            boton.setBackground(new Color(200, 50, 50));
            boton.setText("X");
        } else {
            boton.setBackground(new Color(50, 200, 50));
            boton.setText("O");
        }
        agregarMensaje("Artillería del General " + idJugador + " impactó en la zona [" + fila + ", " + col + "]");
    }

    // NUEVO MÉTODO: Actualiza los colores y textos del letrero superior
    public void actualizarTurno(boolean tuTurno) {
        this.esMiTurno = tuTurno;
        if (tuTurno) {
            indicadorTurno.setText(">>> ES TU TURNO - ORDENA EL ATAQUE <<<");
            indicadorTurno.setForeground(new Color(0, 255, 0)); // Verde
        } else {
            indicadorTurno.setText("ESPERANDO MOVIMIENTO ENEMIGO...");
            indicadorTurno.setForeground(Color.RED);
        }
    }
}