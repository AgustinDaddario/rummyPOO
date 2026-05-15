package serializacion;

import modelo.Combinacion;
import modelo.Jugador;
import modelo.Mazo;
import modelo.PilaDescarte;
import java.io.Serializable;
import java.util.List;

public class EstadoPartida implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<Jugador> jugadores;
    public Mazo mazo;
    public PilaDescarte descarte;
    public int numeroRonda;
    public int limitePuntos;
    public List<Combinacion> tapete;
    public int turnoActual;

    public EstadoPartida(List<Jugador> jugadores, Mazo mazo, PilaDescarte descarte, int numeroRonda, int limitePuntos, List<Combinacion> tapete, int turnoActual) {
        this.jugadores = jugadores;
        this.mazo = mazo;
        this.descarte = descarte;
        this.numeroRonda = numeroRonda;
        this.limitePuntos = limitePuntos;
        this.tapete = tapete;
        this.turnoActual = turnoActual;
    }
}
