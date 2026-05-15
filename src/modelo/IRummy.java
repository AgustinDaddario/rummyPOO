package modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.List;

public interface IRummy extends IObservableRemoto {

    List<Jugador> getJugadores() throws RemoteException;

    boolean partidaLista() throws RemoteException; // true si hay 2..4

    boolean registrarJugador(String nombre) throws RemoteException;

    int getNumeroRonda() throws RemoteException;

    void setLimitePuntos(int limitePuntos) throws RemoteException;

    // En IRummy.java
    int getLimitePuntos() throws RemoteException;

    List<Combinacion> getTapete() throws RemoteException;

    void iniciarNuevaRonda() throws RemoteException;

    Carta getCartaDescarteSuperior() throws RemoteException;

    boolean descarteEstaVacio() throws RemoteException;

    Carta robarDelMazo(Jugador jugador) throws RemoteException;

    void robarDeDescarte(Jugador jugador) throws RemoteException;

    boolean descartar(Jugador jugador, int indiceCarta) throws RemoteException;

    void puntuarPerdedores(Jugador ganador) throws RemoteException;

    boolean esJugadorActivo(Jugador j) throws RemoteException;

    int getCantidadJugadoresActivos() throws RemoteException;

    Jugador getUltimoJugadorActivo() throws RemoteException;

    List<Jugador> getJugadoresEliminadosPorPuntos() throws RemoteException;

    // Metodo para eliminar un jugador
    void eliminarJugador(Jugador j) throws RemoteException;

    boolean bajarCombinacion(Jugador jugador, List<Integer> indices) throws RemoteException;

    boolean agregarCartaACombinacion(Jugador jugador, int indiceCombinacion, int indiceCarta) throws RemoteException;

    boolean esPartidaCargada() throws RemoteException;

    void guardarEstado() throws RemoteException;

    int getTurnoActual() throws RemoteException;


}