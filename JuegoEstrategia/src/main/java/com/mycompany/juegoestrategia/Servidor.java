package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final int PUERTO = 12345;
    private PrintWriter salidaJugador1;
    private PrintWriter salidaJugador2;

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Cuartel general en línea. Esperando a los generales...");

            Socket jugador1 = serverSocket.accept();
            System.out.println("General 1 conectado desde: " + jugador1.getInetAddress());
            salidaJugador1 = new PrintWriter(jugador1.getOutputStream(), true);
            
            Socket jugador2 = serverSocket.accept();
            System.out.println("General 2 conectado desde: " + jugador2.getInetAddress());
            salidaJugador2 = new PrintWriter(jugador2.getOutputStream(), true);

            System.out.println("Ambos mandos confirmados. Iniciando transmisión táctica...");

            salidaJugador1.println("MANDO: Eres el General 1. Prepárate.");
            salidaJugador2.println("MANDO: Eres el General 2. Prepárate.");

            new Thread(new ManejadorCliente(jugador1, 1)).start();
            new Thread(new ManejadorCliente(jugador2, 2)).start();

        } catch (IOException e) {
            System.out.println("Error crítico en el servidor: " + e.getMessage());
        }
    }

    private class ManejadorCliente implements Runnable {
        private Socket socket;
        private int numeroJugador;
        private BufferedReader entrada;

        public ManejadorCliente(Socket socket, int numeroJugador) {
            this.socket = socket;
            this.numeroJugador = numeroJugador;
            try {
                this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Fallo al preparar el canal del General " + numeroJugador);
            }
        }

        @Override
        public void run() {
            try {
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    if (mensaje.startsWith("ATACAR")) {
                        String[] partes = mensaje.split("\\|"); 
                        String fila = partes[1];
                        String col = partes[2];
                        
                        // NUEVO PROTOCOLO: En lugar de texto plano, enviamos un comando estructurado
                        String comandoVisual = "IMPACTO|" + fila + "|" + col + "|" + numeroJugador;
                        
                        if (salidaJugador1 != null) salidaJugador1.println(comandoVisual);
                        if (salidaJugador2 != null) salidaJugador2.println(comandoVisual);
                    }
                }
            } catch (IOException e) {
                System.out.println("Se perdió contacto por radio con el General " + numeroJugador);
            }
        }
    }
}