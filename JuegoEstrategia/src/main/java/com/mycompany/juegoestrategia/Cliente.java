package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.Socket;
import javax.swing.SwingUtilities; // Importante para la seguridad gráfica

public class Cliente {
    private final String HOST = "127.0.0.1";
    private final int PUERTO = 12345;
    private TableroGrafico tablero;

    public void conectar() {
        try {
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            tablero = new TableroGrafico(salida);
            tablero.setVisible(true);
            tablero.agregarMensaje("Enlace establecido con el Cuartel General.");

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
                // Hacemos que la variable sea "final" o efectivamente final para usarla en SwingUtilities
                final String mensajeFinal = mensaje; 
                
                // NUEVO: Decodificación de comandos
                if (mensajeFinal.startsWith("IMPACTO")) {
                    String[] partes = mensajeFinal.split("\\|");
                    int f = Integer.parseInt(partes[1]);
                    int c = Integer.parseInt(partes[2]);
                    int jugador = Integer.parseInt(partes[3]);
                    
                    // Actualización segura de la interfaz gráfica
                    SwingUtilities.invokeLater(() -> {
                        tablero.registrarImpacto(f, c, jugador);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        tablero.agregarMensaje("Servidor informa: " + mensajeFinal);
                    });
                }
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                tablero.agregarMensaje("[ALERTA] Conexión perdida con el mando central.");
            });
        }
    }

    public static void main(String[] args) {
        new Cliente().conectar();
    }
}