package modelo;

import java.util.ArrayList;
import java.util.List;

public class Rummy implements Observable {

    private final List<Jugador> jugadores = new ArrayList<>();
    private Mazo mazo;
    private PilaDescarte descarte;

    // Número de ronda
    private int numeroRonda = 0;

    // Límite de puntos (modo límite)
    private int limitePuntos = 0;

    // Combinaciones en mesa
    private final List<Combinacion> tapete = new ArrayList<>();

    // Observadores
    private final List<Observador> observadores = new ArrayList<>();

    @Override
    public void enlazarObservador(Observador o) {
        if (o != null) observadores.add(o);
    }

    @Override
    public void notificar(Evento evento) {
        for (Observador o : observadores) {
            o.actualizarRummy(this, evento);
        }
    }

    // ================= CONSTRUCTOR =================

    public Rummy(List<String> nombresJugadores) {
        for (String nombre : nombresJugadores) {
            jugadores.add(new Jugador(nombre));
        }
    }

    public List<Jugador> getJugadores() { return jugadores; }

    public int getNumeroRonda() { return numeroRonda; }

    public void setLimitePuntos(int limitePuntos) { this.limitePuntos = limitePuntos; }

    public List<Combinacion> getTapete() { return tapete; }

    // ============================================================
    //                    RONDAS
    // ============================================================

    private void reiniciarEstadoEnRonda() {
        for (Jugador j : jugadores) {
            j.setBajoEnEstaRonda(false);
            j.setHizoRummyEnEstaRonda(false);
        }
    }


    public void iniciarNuevaRonda() {
        numeroRonda++;
        tapete.clear();

        // Reiniciar estados de ronda (NUEVO)
        reiniciarEstadoEnRonda();

        // Vaciar manos
        for (Jugador j : jugadores) {
            j.getMano().getCartas().clear();
        }

        descarte = new PilaDescarte();
        mazo = new Mazo();

        int activos = getCantidadJugadoresActivos();
        int cartasPorJugador = (activos == 2 ? 10 : 7);

        for (int i = 0; i < cartasPorJugador; i++) {
            for (Jugador j : jugadores) {
                if (!esJugadorActivo(j)) continue;
                Carta c = mazo.robarCarta();
                if (c != null) j.getMano().agregarCarta(c);
            }
        }

        Carta primera = mazo.robarCarta();
        if (primera != null) {
            descarte.descartar(primera);
        }

        notificar(Evento.NUEVA_RONDA);
    }


    // ============================================================
    //                     ACCIONES DE TURNO
    // ============================================================

    public Carta getCartaDescarteSuperior() {
        return descarte.verSuperior();
    }

    public boolean descarteEstaVacio() {
        return descarte.estaVacia();
    }

    public void robarDelMazo(Jugador jugador) {
        regenerarMazoSiEsNecesario();
        jugador.getMano().robarDelMazo(mazo);
        notificar(Evento.ROBAR_MAZO);
    }

    public void robarDeDescarte(Jugador jugador) {
        Carta c = descarte.tomarSuperior();
        if (c != null) jugador.agregarCarta(c);
        notificar(Evento.ROBAR_DESCARTE);
    }

    public void descartar(Jugador jugador, int indiceCarta) {
        if (indiceCarta < 0 || indiceCarta >= jugador.getMano().size()) return;

        Carta c = jugador.quitarCarta(indiceCarta);

        if (c != null) descarte.descartar(c);

        notificar(Evento.DESCARTAR);
    }

    // ============================================================
    //                       MAZO
    // ============================================================

    private void regenerarMazoSiEsNecesario() {
        if (!mazo.estaVacio()) return;

        descarte.pasarCartasAlMazo(mazo);
        mazo.barajar();
    }

    // ============================================================
    //                     PUNTAJE / ELIMINACIÓN
    // ============================================================

    public void puntuarPerdedores(Jugador ganador) {
        for (Jugador j : jugadores) {

            if (!esJugadorActivo(j)) continue;
            if (j == ganador) continue;

            int puntos = 0;

            for (Carta c : j.getMano().getCartas()) {
                puntos += c.getPuntos();
            }

            if (ganador.hizoRummyEnEstaRonda()) {
                puntos *= 2;
            }

            j.sumarPuntos(puntos);
        }

        notificar(Evento.PUNTAJE_ACTUALIZADO);
    }

    public boolean esJugadorActivo(Jugador j) {
        if (limitePuntos == 0) return true;
        return !j.estaEliminado();
    }

    public int getCantidadJugadoresActivos() {
        int contador = 0;
        for (Jugador j : jugadores) {
            if (!j.estaEliminado()) contador++;
        }
        return contador;
    }

    public Jugador getUltimoJugadorActivo() {
        for (Jugador j : jugadores) {
            if (esJugadorActivo(j)) return j;
        }
        return null;
    }

    public List<Jugador> getJugadoresEliminadosPorPuntos() {
        List<Jugador> jugadoresFuera = new ArrayList<>();
        if (limitePuntos == 0) return jugadoresFuera;

        for (Jugador j : jugadores) {
            if (j.getPuntos() >= limitePuntos) jugadoresFuera.add(j);
        }
        return jugadoresFuera;
    }

    // Metodo para eliminar un jugador
    public void eliminarJugador(Jugador j) {
        j.eliminar();
        notificar(Evento.JUGADOR_ELIMINADO);
    }

    // ============================================================
    //                     COMBINACIONES
    // ============================================================

    public boolean bajarCombinacion(Jugador jugador, List<Integer> indices) {

        Combinacion comb = jugador.bajarCombinacion(indices);

        if (comb == null) return false;

        tapete.add(comb);

        //Esto detecta si hizo rummy
        if (jugador.sinCartas() && !jugador.yaBajoEnEstaRonda()) {
            jugador.marcarRummy();
        }

        notificar(Evento.BAJAR_COMBINACION);
        return true;
    }

    public boolean agregarCartaACombinacion(Jugador jugador, int indiceCombinacion, int indiceCarta) {

        if (indiceCombinacion < 0 || indiceCombinacion >= tapete.size()) return false;

        Combinacion comb = tapete.get(indiceCombinacion);

        boolean ok = jugador.agregarCartaA(comb, indiceCarta);

        if (ok) notificar(Evento.AGREGAR_A_COMBINACION);

        return ok;
    }
}
