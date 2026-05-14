package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.Socket;

public class Cliente {
    private final String HOST = "127.0.0.1";
    private final int PUERTO = 12345;
    private TableroGrafico tablero;

    public void conectar() {
        try {
            Socket socket = new Socket(HOST, PUERTO);
            
            // 1. Configuramos los canales de entrada y salida
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 2. Iniciamos la interfaz pasándole el canal de salida
            tablero = new TableroGrafico(salida);
            tablero.setVisible(true);
            tablero.agregarMensaje("Enlace establecido con el Cuartel General.");

            // 3. Hilo para escuchar al servidor sin congelar la interfaz
            Thread hiloEscucha = new Thread(() -> escucharServidor(entrada));
            hiloEscucha.start();

        } catch (IOException e) {
            System.out.println("Error de red: " + e.getMessage());
        }
    }

    private void escucharServidor(BufferedReader entrada) {
        try {
            String mensaje;
            // El ciclo while se detiene aquí a esperar hasta que el servidor diga algo
            while ((mensaje = entrada.readLine()) != null) {
                tablero.agregarMensaje("Servidor informa: " + mensaje);
            }
        } catch (IOException e) {
            tablero.agregarMensaje("[ALERTA] Conexión perdida con el mando central.");
        }
    }

    public static void main(String[] args) {
        new Cliente().conectar();
    }
}