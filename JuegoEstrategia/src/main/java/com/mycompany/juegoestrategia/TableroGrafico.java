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
    private JLabel indicadorTurno;
    private boolean esMiTurno = false;
    public boolean faseColocacion = false; 

    // Ajuste de tamaño para el nuevo tablero
    private final int TAM = 7; 

    public TableroGrafico(PrintWriter salidaRed) {
        this.salidaRed = salidaRed;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        setTitle("État-Major Naval - Centro de Operaciones");
        setSize(800, 850); // Ventana ligeramente más grande
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(20, 25, 30));

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
        panelMapa.setLayout(new GridLayout(TAM, TAM, 2, 2)); // Matriz 7x7
        panelMapa.setBackground(new Color(20, 25, 30));
        panelMapa.setBorder(new EmptyBorder(0, 20, 0, 20));

        casillas = new JButton[TAM][TAM];
        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                final int fila = i;
                final int col = j;
                casillas[i][j] = new JButton("~");
                casillas[i][j].setBackground(new Color(28, 57, 104)); 
                casillas[i][j].setForeground(new Color(0, 255, 255));
                casillas[i][j].setFont(new Font("Monospaced", Font.BOLD, 18)); // Fuente un poco más pequeña
                casillas[i][j].setFocusPainted(false);
                casillas[i][j].setBorder(new LineBorder(new Color(0, 100, 200), 1));
                
                casillas[i][j].addActionListener(e -> {
                    if (faseColocacion) {
                        if (this.salidaRed != null) this.salidaRed.println("COLOCAR|" + fila + "|" + col);
                    } else if (esMiTurno) {
                        if (this.salidaRed != null) this.salidaRed.println("ATACAR|" + fila + "|" + col);
                    } else {
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

    public void registrarMiBase(int f, int c) {
        casillas[f][c].setBackground(new Color(0, 85, 164)); 
        casillas[f][c].setText("B");
        casillas[f][c].setEnabled(false); 
        agregarMensaje("Base instalada y camuflada en [" + f + "," + c + "]");
    }

    public void actualizarTurno(boolean tuTurno) {
        this.esMiTurno = tuTurno;
        if (tuTurno) {
            indicadorTurno.setText(">>> ES TU TURNO - ORDENA EL ATAQUE <<<");
            indicadorTurno.setForeground(new Color(0, 255, 0));
        } else {
            indicadorTurno.setText("ESPERANDO MOVIMIENTO ENEMIGO...");
            indicadorTurno.setForeground(Color.RED);
        }
    }
    
    public void iniciarFaseColocacion() {
        this.faseColocacion = true;
        indicadorTurno.setText("FASE DE DESPLIEGUE: UBICA TUS 5 BASES");
        indicadorTurno.setForeground(Color.CYAN);
    }

    public void iniciarFaseCombate() {
        this.faseColocacion = false;
        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                casillas[i][j].setEnabled(true); 
            }
        }
        agregarMensaje("Sistemas de armamento desbloqueados. Listos para disparar.");
    }

    public void registrarAcierto(int f, int c, int idJugador) {
        casillas[f][c].setEnabled(false);
        casillas[f][c].setBackground(new Color(220, 20, 20)); 
        casillas[f][c].setText("💥");
        agregarMensaje("¡IMPACTO CRÍTICO! El General " + idJugador + " destruyó una base en [" + f + "," + c + "]");
    }

    public void registrarAgua(int f, int c, int idJugador) {
        casillas[f][c].setEnabled(false);
        casillas[f][c].setBackground(new Color(100, 100, 100)); 
        casillas[f][c].setText("O");
        agregarMensaje("General " + idJugador + " disparó a [" + f + "," + c + "]. Solo fue agua.");
    }

    public void finDelJuego(String mensajeVictoria) {
        this.esMiTurno = false; 
        indicadorTurno.setText("FIN DE LA TRANSMISIÓN");
        indicadorTurno.setForeground(Color.WHITE);
        JOptionPane.showMessageDialog(this, mensajeVictoria, "Resolución del Conflicto", JOptionPane.INFORMATION_MESSAGE);
    }
}