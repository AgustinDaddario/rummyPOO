package vista;

import java.util.List;
import modelo.Carta;
import modelo.Combinacion;
import modelo.Jugador;

public interface IVistaRummy {

    String pedirNombreJugador(int numero);

    int pedirModoDeJuego();

    int pedirCantidadJugadores();

    int pedirLimiteDePuntos();

    int pedirOpcionRobo(Jugador jugador);

    int pedirIndiceCartaADescartar(Jugador jugador);

    void mostrarEstadoJugador(Jugador jugador);

    void mostrarCartaDescarteActual(Carta carta);

    void mostrarMensaje(String msg);

    void mostrarGanadorRonda(Jugador ganador);

    void mostrarGanadorFinal(Jugador ganador);

    List<Integer> pedirIndicesCombinacion(Jugador jugador);

    int pedirCombinacionATocar(List<Combinacion> tapete);

    int pedirCartaParaAgregar(Jugador jugador);

    boolean pedirReenganche(Jugador jugador);

    void mostrarTapete(List<Combinacion> tapete);

    int leerEnteroSimple();

}
