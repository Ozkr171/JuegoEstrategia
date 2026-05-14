package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final int PUERTO = 12345;
    private PrintWriter salidaJugador1;
    private PrintWriter salidaJugador2;
    private int turnoActual = 1; // El General 1 siempre ataca primero

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Cuartel general en línea. Esperando a los generales...");

            Socket jugador1 = serverSocket.accept();
            salidaJugador1 = new PrintWriter(jugador1.getOutputStream(), true);
            salidaJugador1.println("ID|1"); // Le indicamos que es el General 1

            Socket jugador2 = serverSocket.accept();
            salidaJugador2 = new PrintWriter(jugador2.getOutputStream(), true);
            salidaJugador2.println("ID|2"); // Le indicamos que es el General 2

            // Notificamos a ambos que el juego empieza y es el turno del General 1
            enviarAmbos("MENSAJE|Ambos mandos conectados. ¡Inicia el combate!");
            enviarAmbos("TURNO|" + turnoActual);

            new Thread(new ManejadorCliente(jugador1, 1)).start();
            new Thread(new ManejadorCliente(jugador2, 2)).start();

        } catch (IOException e) {
            System.out.println("Error crítico: " + e.getMessage());
        }
    }

    // Método auxiliar para evitar repetir código al transmitir
    private void enviarAmbos(String comando) {
        if (salidaJugador1 != null) salidaJugador1.println(comando);
        if (salidaJugador2 != null) salidaJugador2.println(comando);
    }

    private class ManejadorCliente implements Runnable {
        private Socket socket;
        private int id;
        private BufferedReader entrada;

        public ManejadorCliente(Socket s, int id) {
            this.socket = s;
            this.id = id;
            try { this.entrada = new BufferedReader(new InputStreamReader(s.getInputStream())); } 
            catch (IOException e) {}
        }

        @Override
        public void run() {
            try {
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    if (mensaje.startsWith("ATACAR")) {
                        // El servidor valida si realmente es el turno de quien disparó
                        if (this.id == turnoActual) {
                            String[] partes = mensaje.split("\\|");
                            enviarAmbos("IMPACTO|" + partes[1] + "|" + partes[2] + "|" + this.id);
                            
                            // Cambiamos el turno (Si era 1 pasa a 2, y viceversa)
                            turnoActual = (turnoActual == 1) ? 2 : 1;
                            enviarAmbos("TURNO|" + turnoActual);
                        } else {
                            // Si intenta atacar fuera de turno, se lo impedimos
                            PrintWriter salidaPrivada = (this.id == 1) ? salidaJugador1 : salidaJugador2;
                            salidaPrivada.println("MENSAJE|Comandante, aguarde su turno para ordenar el ataque.");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Se perdió contacto por radio con el General " + id);
            }
        }
    }
}