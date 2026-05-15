package controlador;

import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import modelo.Evento;
import modelo.*;
import vista.IVistaRummy;

import java.rmi.RemoteException;
import java.util.List;

public class Controlador implements IControladorRemoto {

    private final IVistaRummy vista;
    private IRummy juego;

    // GUARDO NOMBRE PARA IDENTIFICAR
    private String nombreLocal;

    public Controlador(IVistaRummy vista) {
        this.vista = vista;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modeloRemoto) throws RemoteException {
        this.juego = (IRummy) modeloRemoto;
    }

    @Override
    public void actualizar(IObservableRemoto modelo, Object cambio) throws RemoteException {
        if (cambio instanceof Evento evento) {

            vista.actualizar(evento);

            switch (evento) {
                case BAJAR_COMBINACION:
                case AGREGAR_A_COMBINACION:
                case NUEVA_RONDA:
                    // Si alguien bajó algo, pedimos el tapete nuevo y lo mostramos
                    vista.mostrarTapete(juego.getTapete());
                    break;

                case ROBAR_DESCARTE:
                case DESCARTAR:
                    // Si cambió el pozo, lo actualizamos también
                    vista.mostrarCartaDescarteActual(juego.getCartaDescarteSuperior());
                    break;
            }
        }
    }

    public void jugar(boolean soyAnfitrion) throws RemoteException {

        // 1. PEDIR NOMBRE Y GUARDARLO
        String nombre = vista.pedirNombreJugador(1);
        this.nombreLocal = nombre.trim(); // Guardamos quién sos

        // 2. REGISTRARSE
        boolean registrado = juego.registrarJugador(this.nombreLocal);

        if (!registrado) {
            vista.mostrarMensaje("Error: La partida está llena o el nombre ya existe.");
            return;
        }

        vista.mostrarMensaje("Registrado como: " + nombreLocal + ". Esperando rivales...");

        // 3. ESPERAR A OTROS
        while (!juego.partidaLista()) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
        }

        vista.mostrarMensaje("¡Todos conectados!");

        // 4. CONFIGURACIÓN O RECUPERACIÓN
        if (juego.esPartidaCargada()) {
            vista.mostrarMensaje(">>> PARTIDA RECUPERADA. RETOMANDO MESA... <<<");
            vista.mostrarTapete(juego.getTapete());
            vista.mostrarCartaDescarteActual(juego.getCartaDescarteSuperior());
        } else {
            if (soyAnfitrion) {
                int modo = vista.pedirModoDeJuego();
                if (modo == 2) {
                    int limite = vista.pedirLimiteDePuntos();
                    juego.setLimitePuntos(limite);
                } else {
                    juego.setLimitePuntos(0);
                }
                juego.iniciarNuevaRonda();
            } else {
                vista.mostrarMensaje("El anfitrión está configurando la partida...");
                while (juego.getNumeroRonda() == 0) {
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
        }

        // 5. BUCLE DE JUEGO
        int limiteActual = juego.getLimitePuntos();
        if (limiteActual == 0) {
            vista.mostrarMensaje(">>> MODO EXPRESS <<<");
            jugarModoExpress();
        } else {
            vista.mostrarMensaje(">>> MODO PUNTOS (Límite: " + limiteActual + ") <<<");
            jugarModoLimite();
        }
    }


    private void jugarModoExpress() throws RemoteException {
        // No inicia ronda acá, ya lo hizo el host o el loop anterior
        vista.mostrarMensaje("\n────────── RONDA ÚNICA ──────────");
        Jugador ganadorRonda = jugarRonda();
        vista.mostrarGanadorRonda(ganadorRonda);
        vista.mostrarGanadorFinal(ganadorRonda);
    }

    private void jugarModoLimite() throws RemoteException {

        while (juego.getCantidadJugadoresActivos() > 1) {

            // Pequeña pausa para sincronizar
            try { Thread.sleep(1000); } catch (Exception e) {}

            vista.mostrarMensaje("\n────────── RONDA " + juego.getNumeroRonda() + " ──────────");

            Jugador ganadorRonda = jugarRonda();
            vista.mostrarGanadorRonda(ganadorRonda);

            juego.puntuarPerdedores(ganadorRonda);

            List<Jugador> eliminados = juego.getJugadoresEliminadosPorPuntos();
            for (Jugador j : eliminados) {
                // Solo le pregunto al jugador afectado si es ÉL MISMO
                if (j.getNombre().equals(this.nombreLocal)) {
                    if (juego.getCantidadJugadoresActivos() >= 3) {
                        boolean quiere = vista.pedirReenganche(j);
                        if (quiere) {
                            // j.resetearPuntos(); // Server logic placeholder
                            vista.mostrarMensaje("Te has reenganchado.");
                        } else {
                            juego.eliminarJugador(j);
                            vista.mostrarMensaje("Has sido eliminado.");
                        }
                    } else {
                        juego.eliminarJugador(j);
                        vista.mostrarMensaje("Eliminado definitivamente.");
                    }
                }
            }

            // Siguiente ronda (solo el Host/Primero la inicia)
            if (juego.getCantidadJugadoresActivos() > 1) {
                try { Thread.sleep(2000); } catch (Exception e) {}

                // Lógica simple para no duplicar inicio de ronda
                if (juego.getJugadores().get(0).getNombre().equals(this.nombreLocal)) {
                    juego.iniciarNuevaRonda();
                } else {
                    int rondaActual = juego.getNumeroRonda();
                    while (juego.getNumeroRonda() == rondaActual) {
                        try { Thread.sleep(500); } catch(Exception e) {}
                    }
                }
            }
        }

        Jugador ganadorFinal = juego.getUltimoJugadorActivo();
        if (ganadorFinal != null) {
            vista.mostrarGanadorFinal(ganadorFinal);
        }
    }

    private Jugador jugarRonda() throws RemoteException {
        while (true) {
            // 1. Le preguntamos al servidor de quién es el turno REAL
            int turnoServidor = juego.getTurnoActual();
            List<Jugador> jugadores = juego.getJugadores();

            // Por seguridad, si el índice se desborda, cortamos
            if (turnoServidor >= jugadores.size()) break;

            Jugador j = jugadores.get(turnoServidor);

            // Si el jugador fue eliminado, esperamos a que el servidor lo saltee
            if (!juego.esJugadorActivo(j)) {
                try { Thread.sleep(500); } catch (Exception e) {}
                continue;
            }

            // >>> FILTRO DE IDENTIDAD <<<
            if (j.getNombre().equals(this.nombreLocal)) {
                // SI ES MI TURNO: JUEGO
                ejecutarTurno(j);
                if (j.sinCartas()) return j;

                // Bloqueo de seguridad: Esperamos a que el servidor confirme el fin de mi turno
                while (juego.getTurnoActual() == turnoServidor) {
                    try { Thread.sleep(500); } catch (Exception e) {}
                }
            } else {
                // SI NO ES MI TURNO: ESPERO
                esperarTurnoAjeno(j, turnoServidor);
                if (chequearSiGano(j.getNombre())) return j;
            }
        }
        return null;
    }

    private void esperarTurnoAjeno(Jugador j, int turnoActualServidor) throws RemoteException {
        vista.mostrarMensaje("Esperando turno de: " + j.getNombre() + "...");
        vista.mostrarCartaDescarteActual(juego.getCartaDescarteSuperior());

        // Nos quedamos esperando pacientemente hasta que el SERVIDOR cambie el turno
        while (juego.getTurnoActual() == turnoActualServidor) {
            if (chequearSiGano(j.getNombre())) break;
            try { Thread.sleep(500); } catch (InterruptedException e) { break; }
        }
    }

    private boolean chequearSiGano(String nombre) throws RemoteException {
        for (Jugador remoto : juego.getJugadores()) {
            if (remoto.getNombre().equals(nombre) && remoto.sinCartas()) return true;
        }
        return false;
    }

    private void ejecutarTurno(Jugador jugador) throws RemoteException {

        // Muestro mis cartas
        vista.mostrarEstadoJugador(jugador);
        vista.mostrarCartaDescarteActual(juego.getCartaDescarteSuperior());

        int opcionRobo = vista.pedirOpcionRobo(jugador);

        // LÓGICA DE ROBO
        if (opcionRobo == 1 || juego.descarteEstaVacio()) {
            juego.robarDelMazo(jugador);

            Jugador actualizado = refrescarJugador(this.nombreLocal);
            if (actualizado != null) {
                jugador.getMano().getCartas().clear();
                jugador.getMano().getCartas().addAll(actualizado.getMano().getCartas());
            }
        } else {
            juego.robarDeDescarte(jugador);

            Jugador actualizado = refrescarJugador(this.nombreLocal);
            if (actualizado != null) {
                jugador.getMano().getCartas().clear();
                jugador.getMano().getCartas().addAll(actualizado.getMano().getCartas());
            }
        }

        vista.mostrarEstadoJugador(jugador);

        while (!jugador.sinCartas()) {

            // MENÚ DE ACCIONES
            // (1: Bajar, 2: Agregar, 3: Terminar)
            int opcionJugada = vista.leerEnteroSimple();

            if (opcionJugada == 1) {
                List<Integer> indices = vista.pedirIndicesCombinacion(jugador);

                if (indices.size() >= 3) {
                    boolean combok = juego.bajarCombinacion(jugador, indices);
                    if (!combok) {
                        vista.mostrarMensaje("Combinación inválida.");
                    } else {
                        // ACTUALIZACIÓN LOCAL INMEDIATA
                        jugador.bajarCombinacion(indices);
                        vista.mostrarMensaje("¡Combinación bajada!");
                        vista.mostrarEstadoJugador(jugador);
                    }
                }
            }
            else if (opcionJugada == 2) {
                vista.mostrarTapete(juego.getTapete());
                int cualComb = vista.pedirCombinacionATocar(juego.getTapete());

                if (cualComb != -1) {
                    int indiceCarta = vista.pedirCartaParaAgregar(jugador);
                    boolean combinacionOk = juego.agregarCartaACombinacion(jugador, cualComb, indiceCarta);

                    if (!combinacionOk) {
                        vista.mostrarMensaje("No encaja.");
                    } else {
                        // ACTUALIZACIÓN LOCAL INMEDIATA
                        jugador.quitarCarta(indiceCarta);
                        vista.mostrarMensaje("¡Carta agregada!");
                        vista.mostrarEstadoJugador(jugador);
                    }
                }
            }
            else if (opcionJugada == 3) break; // Terminar turno

            if (jugador.sinCartas()) break;
        }

        if (!jugador.sinCartas()) {
            boolean descartado = false;

            // LOOP HASTA QUE INGRESE UN ÍNDICE VÁLIDO
            while (!descartado) {
                int indiceDescartar = vista.pedirIndiceCartaADescartar(jugador);
                descartado = juego.descartar(jugador, indiceDescartar);

                if (descartado) {
                    // Actualización local final solo si funcionó en el servidor
                    jugador.quitarCarta(indiceDescartar);
                    vista.mostrarEstadoJugador(jugador);
                } else {
                    vista.mostrarMensaje("Error: Índice inválido. Elegí una carta de tu mano.");
                }
            }
        }
    }

    // Metodo auxiliar para sincronizar la mano despues robar
    private Jugador refrescarJugador(String nombre) throws RemoteException {
        for (Jugador j : juego.getJugadores()) {
            if (j.getNombre().equals(nombre)) return j;
        }
        return null;
    }
}