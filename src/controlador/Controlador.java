package controlador;

import modelo.Jugador;
import modelo.Observador;
import modelo.Rummy;
import vista.IVistaRummy;

import java.util.ArrayList;
import java.util.List;

public class Controlador {

    private final IVistaRummy vista;
    private Rummy juego;

    public Controlador(IVistaRummy vista) {
        this.vista = vista;
    }

    public void jugar() {

        // Pido cantidad y nombres
        int cantidad = vista.pedirCantidadJugadores();
        List<String> nombres = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            nombres.add(vista.pedirNombreJugador(i));
        }

        // Creo el modelo
        juego = new Rummy(nombres);

        // Enlazo OBSERVER
        if (vista instanceof Observador obs) {
            juego.enlazarObservador(obs);
        }


        // Pregunto modo de juego
        int modo = vista.pedirModoDeJuego();

        if (modo == 1) {
            jugarModoExpress();
        } else {
            int limite = vista.pedirLimiteDePuntos();
            juego.setLimitePuntos(limite);
            jugarModoLimite();
        }
    }

    // ================= MODOS DE JUEGO =================

    private void jugarModoExpress() {

        juego.iniciarNuevaRonda();
        vista.mostrarMensaje("\n────────── RONDA " + juego.getNumeroRonda() + " ──────────");

        Jugador ganadorRonda = jugarRonda();
        vista.mostrarGanadorRonda(ganadorRonda);
        vista.mostrarGanadorFinal(ganadorRonda);
    }

    private void jugarModoLimite() {

        while (juego.getCantidadJugadoresActivos() > 1) {

            juego.iniciarNuevaRonda();
            vista.mostrarMensaje("\n────────── RONDA " + juego.getNumeroRonda() + " ──────────");

            Jugador ganadorRonda = jugarRonda();
            vista.mostrarGanadorRonda(ganadorRonda);

            // Puntuar y eliminar según límite
            juego.puntuarPerdedores(ganadorRonda);

            List<Jugador> eliminados = juego.getJugadoresEliminadosPorPuntos();

            for (Jugador j : eliminados) {
                // Si hay 3 o más jugadores, ofrecer reenganche
                if (juego.getCantidadJugadoresActivos() >= 3) {

                    boolean quiere = vista.pedirReenganche(j);

                    if (quiere) {
                        j.resetearPuntos();
                        vista.mostrarMensaje(j.getNombre() + " se reenganchó al juego!");
                    } else {
                        juego.eliminarJugador(j);  // notifica con Evento.JUGADOR_ELIMINADO
                        vista.mostrarMensaje(j.getNombre() + " quedó eliminado.");


                    }

                } else {
                    // Si quedan solo 2 jugadores, NO hay reenganche
                    juego.eliminarJugador(j);
                    vista.mostrarMensaje(j.getNombre() + " quedó eliminado (sin opción a reenganche).");
                }
            }


        }

        Jugador ganadorFinal = juego.getUltimoJugadorActivo();
        if (ganadorFinal != null) {
            vista.mostrarGanadorFinal(ganadorFinal);
        }
    }

    // ================= RONDA COMPLETA =================

    private Jugador jugarRonda() {

        while (true) {
            for (Jugador j : juego.getJugadores()) {

                if (!juego.esJugadorActivo(j)) continue;

                ejecutarTurno(j);

                if (j.sinCartas()) {
                    return j;
                }
            }
        }
    }

    // ================= EJECUCIÓN DE UN TURNO =================

    private void ejecutarTurno(Jugador jugador) {

        vista.mostrarEstadoJugador(jugador);
        vista.mostrarCartaDescarteActual(juego.getCartaDescarteSuperior());

        int opcionRobo = vista.pedirOpcionRobo(jugador);

        if (opcionRobo == 1 || juego.descarteEstaVacio()) {
            juego.robarDelMazo(jugador);
        } else {
            juego.robarDeDescarte(jugador);
        }

        // Mostrar la mano después de robar
        vista.mostrarEstadoJugador(jugador);

        // ---------------- MENÚ DE JUGADAS ----------------

        while (!jugador.sinCartas()) {

            vista.mostrarMensaje("\n--- ¿Qué querés hacer? ---");
            vista.mostrarMensaje("1) Bajar nueva combinación");
            vista.mostrarMensaje("2) Agregar carta a una combinación existente");
            vista.mostrarMensaje("3) Terminar turno (ir a descartar)");

            int opcionJugada = vista.leerEnteroSimple();

            if (opcionJugada == 1) {
                // BAJAR COMBINACIÓN
                List<Integer> indices = vista.pedirIndicesCombinacion(jugador);

                if (indices.size() < 3) {
                    vista.mostrarMensaje("Necesitás al menos 3 cartas.");
                    continue;
                }

                boolean combok = juego.bajarCombinacion(jugador, indices);

                if (!combok) {
                    vista.mostrarMensaje("Combinación inválida.");
                } else {
                    vista.mostrarMensaje("¡Combinación bajada!");
                    vista.mostrarEstadoJugador(jugador);
                }
            }
            else if (opcionJugada == 2) {

                vista.mostrarTapete(juego.getTapete());

                int cualComb = vista.pedirCombinacionATocar(juego.getTapete());

                // Si eligió volver (-1), volvemos al menú de jugadas
                if (cualComb == -1) {
                    vista.mostrarMensaje("Volviendo al menú de jugadas...");
                    continue; // ← Vuelve al while de jugadas
                }

                int carta = vista.pedirCartaParaAgregar(jugador);

                boolean combinacionOk = juego.agregarCartaACombinacion(jugador, cualComb, carta);

                if (!combinacionOk) {
                    vista.mostrarMensaje("La carta no encaja en esa combinación.");
                } else {
                    vista.mostrarMensaje("¡Carta agregada!");
                    vista.mostrarEstadoJugador(jugador);
                }
            }

            else if (opcionJugada == 3) {
                break; // pasa a descartar
            }
            else {
                vista.mostrarMensaje("Opción inválida.");
            }

            if (jugador.sinCartas()) break;
        }

        // ---------------- DESCARTAR ----------------

        if (!jugador.sinCartas()) {
            int indiceDescartar = vista.pedirIndiceCartaADescartar(jugador);
            juego.descartar(jugador, indiceDescartar);
        }
    }
}
