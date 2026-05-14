package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Cliente {
    private final String HOST = "127.0.0.1";
    private final int PUERTO = 12345;
    private TableroGrafico tablero;
    private int miId;

    public void conectar() {
        try {
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            tablero = new TableroGrafico(salida);
            tablero.setVisible(true);

            Thread hiloEscucha = new Thread(() -> escucharServidor(entrada));
            hiloEscucha.start();

        } catch (IOException e) {
            System.out.println("Error de red: " + e.getMessage());
        }
    }

    private void escucharServidor(BufferedReader entrada) {
        try {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                final String msj = mensaje;
                SwingUtilities.invokeLater(() -> {
                    if (msj.startsWith("ID|")) {
                        miId = Integer.parseInt(msj.split("\\|")[1]);
                        tablero.agregarMensaje("Asignado como General " + miId);
                    } 
                    else if (msj.startsWith("FASE|COLOCAR")) {
                        tablero.iniciarFaseColocacion();
                    }
                    else if (msj.startsWith("BASE_CONFIRMADA|")) {
                        String[] p = msj.split("\\|");
                        tablero.registrarMiBase(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                    }
                    else if (msj.startsWith("FASE|COMBATE")) {
                        tablero.iniciarFaseCombate(); 
                    }
                    else if (msj.startsWith("TURNO|")) {
                        int turnoActual = Integer.parseInt(msj.split("\\|")[1]);
                        tablero.actualizarTurno(turnoActual == miId); 
                    } 
                    else if (msj.startsWith("ACIERTO|")) {
                        String[] p = msj.split("\\|");
                        tablero.registrarAcierto(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                    }
                    else if (msj.startsWith("AGUA|")) {
                        String[] p = msj.split("\\|");
                        tablero.registrarAgua(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                    }
                    // NUEVA LÓGICA DE FIN DE PARTIDA
                    else if (msj.startsWith("VICTORIA|")) {
                        int ganador = Integer.parseInt(msj.split("\\|")[1]);
                        if (ganador == miId) {
                            tablero.finDelJuego("¡Felicidades Comandante! Ha asegurado la superioridad. Ganó la guerra.");
                        } else {
                            tablero.finDelJuego("Derrota confirmada. El enemigo tiene el control de la zona.");
                        }
                    }
                    else if (msj.startsWith("EMPATE|")) {
                        tablero.finDelJuego("Territorio agotado. Ambos bandos sufrieron bajas iguales. ¡Es un EMPATE TÁCTICO!");
                    }
                    else if (msj.startsWith("MENSAJE|")) {
                        tablero.agregarMensaje(msj.split("\\|")[1]);
                    }
                });
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> tablero.agregarMensaje("[ALERTA] Conexión perdida."));
        }
    }

    public static void main(String[] args) {
        new Cliente().conectar();
    }
}