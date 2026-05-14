package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Cliente {
    private final String HOST = "127.0.0.1";
    private final int PUERTO = 12345;
    private TableroGrafico tablero;
    private int miId; // Variable para recordar qué General somos

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
                
                // Procesamos todos los comandos dentro del hilo de Swing
                SwingUtilities.invokeLater(() -> {
                    if (msj.startsWith("ID|")) {
                        miId = Integer.parseInt(msj.split("\\|")[1]);
                        tablero.agregarMensaje("Asignado como General " + miId);
                    } 
                    else if (msj.startsWith("TURNO|")) {
                        int turnoActual = Integer.parseInt(msj.split("\\|")[1]);
                        // Si el turno que manda el servidor es igual a mi ID, es mi turno
                        tablero.actualizarTurno(turnoActual == miId); 
                    } 
                    else if (msj.startsWith("IMPACTO|")) {
                        String[] p = msj.split("\\|");
                        tablero.registrarImpacto(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                    } 
                    else if (msj.startsWith("MENSAJE|")) {
                        // Limpiamos la etiqueta para imprimir solo el texto
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