package com.mycompany.juegoestrategia;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final int PUERTO = 12345;

    public void iniciar() {
        // Usamos try-with-resources para asegurar que el puerto se cierre si ocurre un error
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Cuartel general en línea. Esperando a que los generales se conecten al mapa de guerra...");

            // El servidor se pausa aquí hasta que el primer cliente se conecte
            Socket jugador1 = serverSocket.accept();
            System.out.println("¡General 1 reportándose desde: " + jugador1.getInetAddress() + "!");

            // El servidor se pausa de nuevo esperando al segundo cliente
            Socket jugador2 = serverSocket.accept();
            System.out.println("¡General 2 reportándose desde: " + jugador2.getInetAddress() + "!");

            System.out.println("Ambos mandos confirmados. ¡Que comience la estrategia!");

            // Nota: Aquí más adelante agregaremos los "Hilos" para procesar los movimientos

        } catch (IOException e) {
            System.out.println("Error al iniciar el cuartel general: " + e.getMessage());
        }
    }
}