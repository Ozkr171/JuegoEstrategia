package com.mycompany.juegoestrategia;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final int PUERTO = 12345;
    private PrintWriter salidaJugador1;
    private PrintWriter salidaJugador2;
    private int turnoActual = 1;
    
    // REFACTORIZACIÓN: Tablero ampliado a 7x7
    private final int TAM = 7;
    private final int MAX_BASES = 5;
    private int[][] mapaP1 = new int[TAM][TAM];
    private int[][] mapaP2 = new int[TAM][TAM];
    
    private int basesP1 = 0; 
    private int basesP2 = 0;
    private boolean enCombate = false; 
    
    // NUEVO: Contador para el límite de la partida
    private int casillasRestantes = TAM * TAM; 

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Cuartel general en línea. Esperando despliegue...");

            Socket jugador1 = serverSocket.accept();
            salidaJugador1 = new PrintWriter(jugador1.getOutputStream(), true);
            salidaJugador1.println("ID|1");

            Socket jugador2 = serverSocket.accept();
            salidaJugador2 = new PrintWriter(jugador2.getOutputStream(), true);
            salidaJugador2.println("ID|2");

            enviarAmbos("MENSAJE|Almirantes conectados. Fase 1: Posicionen 5 bases en el mapa.");
            enviarAmbos("FASE|COLOCAR");

            new Thread(new ManejadorCliente(jugador1, 1)).start();
            new Thread(new ManejadorCliente(jugador2, 2)).start();

        } catch (IOException e) {
            System.out.println("Error crítico: " + e.getMessage());
        }
    }

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
                    
                    if (mensaje.startsWith("COLOCAR") && !enCombate) {
                        String[] partes = mensaje.split("\\|");
                        int f = Integer.parseInt(partes[1]);
                        int c = Integer.parseInt(partes[2]);
                        
                        boolean puedeColocar = false;
                        boolean casillaLibre = false;
                        
                        if (this.id == 1 && basesP1 < MAX_BASES) {
                            puedeColocar = true;
                            casillaLibre = (mapaP1[f][c] == 0);
                        } else if (this.id == 2 && basesP2 < MAX_BASES) {
                            puedeColocar = true;
                            casillaLibre = (mapaP2[f][c] == 0);
                        }
                        
                        if (puedeColocar && casillaLibre) {
                            if (this.id == 1) { 
                                mapaP1[f][c] = 1; basesP1++; 
                            } else { 
                                mapaP2[f][c] = 1; basesP2++; 
                            }
                            
                            PrintWriter salidaPrivada = (this.id == 1) ? salidaJugador1 : salidaJugador2;
                            salidaPrivada.println("BASE_CONFIRMADA|" + f + "|" + c);
                            
                            if (basesP1 == MAX_BASES && basesP2 == MAX_BASES) {
                                enCombate = true; 
                                enviarAmbos("MENSAJE|Despliegue finalizado. ¡Inicia el fuego cruzado!");
                                enviarAmbos("FASE|COMBATE");
                                enviarAmbos("TURNO|" + turnoActual);
                            }
                        } else if (puedeColocar && !casillaLibre) {
                            PrintWriter salidaPrivada = (this.id == 1) ? salidaJugador1 : salidaJugador2;
                            salidaPrivada.println("MENSAJE|Ya estableciste una base propia en esa zona.");
                        }
                    }
                    
                    else if (mensaje.startsWith("ATACAR") && enCombate && this.id == turnoActual) {
                        String[] partes = mensaje.split("\\|");
                        int f = Integer.parseInt(partes[1]);
                        int c = Integer.parseInt(partes[2]);
                        
                        int[][] mapaEnemigo = (this.id == 1) ? mapaP2 : mapaP1;

                        if (mapaEnemigo[f][c] == 1) {
                            mapaEnemigo[f][c] = -1;
                            if (this.id == 1) basesP2--; else basesP1--;
                            enviarAmbos("ACIERTO|" + f + "|" + c + "|" + this.id);
                        } else if (mapaEnemigo[f][c] == 0) {
                            mapaEnemigo[f][c] = -1;
                            enviarAmbos("AGUA|" + f + "|" + c + "|" + this.id);
                        }
                        
                        // Reducimos el contador de casillas libres cada vez que alguien ataca
                        casillasRestantes--;
                        
                        // LÓGICA DE VICTORIA O EMPATE
                        if (basesP1 == 0) { enviarAmbos("VICTORIA|2"); return; }
                        if (basesP2 == 0) { enviarAmbos("VICTORIA|1"); return; }
                        
                        if (casillasRestantes <= 0) {
                            if (basesP1 > basesP2) enviarAmbos("VICTORIA|1");
                            else if (basesP2 > basesP1) enviarAmbos("VICTORIA|2");
                            else enviarAmbos("EMPATE|0");
                            return; // El juego termina
                        }
                        
                        turnoActual = (turnoActual == 1) ? 2 : 1;
                        enviarAmbos("TURNO|" + turnoActual);
                    }
                }
            } catch (IOException e) {
                System.out.println("Se perdió contacto por radio con el General " + id);
            }
        }
    }
}