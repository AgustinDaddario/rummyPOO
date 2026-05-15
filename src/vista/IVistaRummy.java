package vista;

import java.util.List;

import controlador.Controlador;
import modelo.Carta;
import modelo.Combinacion;
import modelo.Evento;
import modelo.Jugador;

public interface IVistaRummy {
    void iniciar();

    String pedirNombreJugador(int var1);

    int pedirModoDeJuego();

    int pedirCantidadJugadores();

    int pedirLimiteDePuntos();

    int pedirOpcionRobo(Jugador var1);

    int pedirIndiceCartaADescartar(Jugador var1);

    void mostrarEstadoJugador(Jugador var1);

    void mostrarCartaDescarteActual(Carta var1);

    void mostrarMensaje(String var1);

    void mostrarGanadorRonda(Jugador var1);

    void mostrarGanadorFinal(Jugador var1);

    List<Integer> pedirIndicesCombinacion(Jugador var1);

    int pedirCombinacionATocar(List<Combinacion> var1);

    int pedirCartaParaAgregar(Jugador var1);

    boolean pedirReenganche(Jugador var1);

    void mostrarTapete(List<Combinacion> var1);

    int leerEnteroSimple();

    void actualizar(Evento evento);

    void setControlador(Controlador controlador);
}