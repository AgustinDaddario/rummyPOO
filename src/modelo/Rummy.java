package modelo;

import serializacion.AdminJugadores;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

public class Rummy extends ObservableRemoto implements IRummy {


    private final List<Jugador> jugadores = new ArrayList<>();
    private static final int MAX_JUGADORES = 4;
    private Mazo mazo;
    private PilaDescarte descarte;

    // Número de ronda
    private int numeroRonda = 0;

    // Límite de puntos (modo límite)
    private int limitePuntos = 0;

    //Si es una partida cargada
    private boolean partidaCargada = false;

    private int turnoActual = 0;

    // Combinaciones en mesa
    private final List<Combinacion> tapete = new ArrayList<>();


    // ================= CONSTRUCTOR =================

    public Rummy() {
        //Arranca vacio
    }

 
    private final List<String> jugadoresConectados = new ArrayList<>();


    @Override
    public List<Jugador> getJugadores() throws RemoteException{ return jugadores; }

    @Override
    public synchronized boolean registrarJugador(String nombre) throws RemoteException {
        // SI SE CARGÓ UNA PARTIDA, SOLO DEJA ENTRAR A LOS QUE YA ESTABAN
        if (partidaCargada) {
            for (Jugador j : jugadores) {

                if (j.getNombre().equalsIgnoreCase(nombre)) {
                    // Lo anota como conectado
                    if (!jugadoresConectados.contains(nombre)) {
                        jugadoresConectados.add(nombre);
                    }
                    return true;
                }

                if (j.getNombre().equalsIgnoreCase(nombre)) return true;

            }
            return false;
        }


        // LÓGICA PARA PARTIDAS NUEVAS:

        // LÓGICA ORIGINAL:
        if (jugadores.size() >= MAX_JUGADORES) return false;
        AdminJugadores admin = AdminJugadores.getInstance();
        admin.agregarJugador(nombre);
        Jugador jugadorReal = admin.getJugador(nombre);

        if (jugadores.contains(jugadorReal)) return false;

        jugadores.add(jugadorReal);
        jugadoresConectados.add(nombre); // Lo anotamos como conectado
        return true;
    }

    @Override
    public synchronized boolean partidaLista() throws RemoteException {
        if (partidaCargada) {
            // Si recuperamos la partida, NO ARRANCA hasta que la cantidad
            // de conectados coincida con los que estaban jugando.
            return jugadoresConectados.size() == jugadores.size() && jugadores.size() > 0;
        }
        // Lógica original para partidas nuevas
        return jugadores.size() >= 2 && jugadores.size() <= MAX_JUGADORES;
    }

    @Override
    public int getNumeroRonda() throws RemoteException{ return numeroRonda; }

    @Override
    public void setLimitePuntos(int limitePuntos) { this.limitePuntos = limitePuntos; }

    @Override
    public int getLimitePuntos() throws RemoteException {
        return this.limitePuntos;
    }

    @Override
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


    @Override
    public void iniciarNuevaRonda() throws RemoteException {
        numeroRonda++;
        tapete.clear();
        this.turnoActual = 0;

        // Reiniciar estados de ronda
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

        notificarObservadores(Evento.NUEVA_RONDA);
        autoGuardar();
    }


    // ============================================================
    //                     ACCIONES DE TURNO
    // ============================================================

    @Override
    public Carta getCartaDescarteSuperior() throws RemoteException{
        return descarte.verSuperior();
    }

    @Override
    public boolean descarteEstaVacio() throws RemoteException{
        return descarte.estaVacia();
    }

    @Override
    public Carta robarDelMazo(Jugador jugador) throws RemoteException {
        regenerarMazoSiEsNecesario();
        //Buscamos al jugador en la lista local del servidor
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(jugador.getNombre())) {
                // Le damos la carta a ESTA instancia (la real)
                Carta c = j.getMano().robarDelMazo(mazo);
                notificarObservadores(Evento.ROBAR_MAZO);
                autoGuardar();
                return c; // Devolvemos la carta para que el controlador la muestre
            }
        }
        return null;
    }

    @Override
    public void robarDeDescarte(Jugador jugador) throws RemoteException {
        //Buscamos al jugador real
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(jugador.getNombre())) {
                Carta c = descarte.tomarSuperior();
                if (c != null) j.agregarCarta(c);
                notificarObservadores(Evento.ROBAR_DESCARTE);
                autoGuardar();
                return;
            }
        }
    }


    @Override
    public boolean descartar(Jugador jugador, int indiceCarta) throws RemoteException {
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            if (j.getNombre().equals(jugador.getNombre())) {
                // SI EL ÍNDICE ES INVÁLIDO, AVISA QUE FALLÓ
                if (indiceCarta < 0 || indiceCarta >= j.getMano().size()) return false;

                Carta c = j.quitarCarta(indiceCarta);
                if (c != null) descarte.descartar(c);

                notificarObservadores(Evento.DESCARTAR);

                // AVANZA EL TURNO Y GUARDO
                this.turnoActual = (i + 1) % jugadores.size();
                autoGuardar();
                return true;
            }
        }
        return false;
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

    @Override
    public void puntuarPerdedores(Jugador ganador) throws RemoteException {
        // 1. Busco al ganador REAL en la lista del servidor para ver sus flags (si hizo Rummy POR EJ)
        Jugador ganadorReal = null;
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(ganador.getNombre())) {
                ganadorReal = j;
                break;
            }
        }
        if (ganadorReal == null) return; // Seguridad

        for (Jugador j : jugadores) {

            if (!esJugadorActivo(j)) continue;
            // Uso referencia directa porque ambos objetos vienen de la misma lista ahora
            if (j == ganadorReal) continue;

            int puntos = 0;

            for (Carta c : j.getMano().getCartas()) {
                puntos += c.getPuntos();
            }

            if (ganadorReal.hizoRummyEnEstaRonda()) {
                puntos *= 2;
            }

            j.sumarPuntos(puntos);
        }
        // Guardo los cambios en el archivo
        AdminJugadores.getInstance().actualizar();
        notificarObservadores(Evento.PUNTAJE_ACTUALIZADO);
    }


    @Override
    public boolean esJugadorActivo(Jugador j) throws RemoteException{
        if (limitePuntos == 0) return true;
        return !j.estaEliminado();
    }

    @Override
    public int getCantidadJugadoresActivos() throws RemoteException{
        int contador = 0;
        for (Jugador j : jugadores) {
            if (!j.estaEliminado()) contador++;
        }
        return contador;
    }

    @Override
    public Jugador getUltimoJugadorActivo() throws RemoteException{
        for (Jugador j : jugadores) {
            if (esJugadorActivo(j)) return j;
        }
        return null;
    }

    @Override
    public List<Jugador> getJugadoresEliminadosPorPuntos() throws RemoteException{
        List<Jugador> jugadoresFuera = new ArrayList<>();
        if (limitePuntos == 0) return jugadoresFuera;

        for (Jugador j : jugadores) {
            if (j.getPuntos() >= limitePuntos) jugadoresFuera.add(j);
        }
        return jugadoresFuera;
    }

    @Override
    public void eliminarJugador(Jugador j) throws RemoteException {
        //Busco al real para eliminarlo
        for (Jugador jugadorReal : jugadores) {
            if (jugadorReal.getNombre().equals(j.getNombre())) {
                jugadorReal.eliminar();
                break;
            }
        }
        notificarObservadores(Evento.JUGADOR_ELIMINADO);
    }

    // ============================================================
    //                     COMBINACIONES
    // ============================================================

    @Override
    public boolean bajarCombinacion(Jugador jugador, List<Integer> indices) throws RemoteException {
        //Busco al jugador real
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(jugador.getNombre())) {
                Combinacion comb = j.bajarCombinacion(indices);

                if (comb == null) return false;

                tapete.add(comb);

                // Detecto Rummy sobre la instancia real
                if (j.sinCartas() && !j.yaBajoEnEstaRonda()) {
                    j.marcarRummy();
                }

                notificarObservadores(Evento.BAJAR_COMBINACION);
                autoGuardar();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean agregarCartaACombinacion(Jugador jugador, int indiceCombinacion, int indiceCarta) throws RemoteException {

        if (indiceCombinacion < 0 || indiceCombinacion >= tapete.size()) return false;

        Combinacion comb = tapete.get(indiceCombinacion);

        //Busco al jugador real
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(jugador.getNombre())) {
                boolean ok = j.agregarCartaA(comb, indiceCarta);
                if (ok) notificarObservadores(Evento.AGREGAR_A_COMBINACION);
                autoGuardar();
                return ok;
            }
        }
        return false;
    }

    //SECCION DE GUARDADO DE PARTIDA

    @Override
    public boolean esPartidaCargada() throws RemoteException {
        return this.partidaCargada;
    }

    public void cargarEstado() {
        serializacion.Serializador s = new serializacion.Serializador("partida_guardada.dat");
        Object obj = s.readFirstObject();
        if (obj instanceof serializacion.EstadoPartida estado) {
            this.jugadores.clear();
            this.jugadores.addAll(estado.jugadores);
            this.mazo = estado.mazo;
            this.descarte = estado.descarte;
            this.numeroRonda = estado.numeroRonda;
            this.limitePuntos = estado.limitePuntos;
            this.tapete.clear();
            this.tapete.addAll(estado.tapete);
            this.turnoActual = estado.turnoActual;
            this.partidaCargada = true;
        }
    }

    @Override
    public void guardarEstado() throws RemoteException {
        serializacion.EstadoPartida estado = new serializacion.EstadoPartida(
                new ArrayList<>(jugadores),
                mazo,
                descarte,
                numeroRonda,
                limitePuntos,
                new ArrayList<>(tapete),
                this.turnoActual
        );
        serializacion.Serializador s = new serializacion.Serializador("partida_guardada.dat");
        s.writeOneObject(estado);
    }

    private void autoGuardar() {
        try { guardarEstado(); } catch (Exception ignored) {}
    }


    @Override
    public int getTurnoActual() throws RemoteException {
        return this.turnoActual;
    }
}
