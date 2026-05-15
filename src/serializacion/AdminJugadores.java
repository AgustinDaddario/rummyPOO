package serializacion;

import java.util.ArrayList;
import java.util.List;
import modelo.Jugador;

public class AdminJugadores {

    private static AdminJugadores instance;
    private List<Jugador> jugadores;
    private Serializador serializador;
    private static final String ARCHIVO = "jugadores.dat";

    // Constructor privado (Singleton)
    private AdminJugadores() {
        this.jugadores = new ArrayList<>();
        this.serializador = new Serializador(ARCHIVO);
        cargar();
    }

    public static AdminJugadores getInstance() {
        if (instance == null) {
            instance = new AdminJugadores();
        }
        return instance;
    }

    // --- LÓGICA DE NEGOCIO ---

    public void agregarJugador(String nombre) {
        // Solo agrega si no existe
        if (getJugador(nombre) == null) {
            jugadores.add(new Jugador(nombre));
            guardar(); // Persistencia inmediata
        }
    }

    public Jugador getJugador(String nombre) {
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombre)) {
                return j;
            }
        }
        return null;
    }

    public List<Jugador> getJugadoresRegistrados() {
        return jugadores;
    }

    // Metodo para actualizar datos (ej: puntos) y forzar guardado
    public void actualizar() {
        guardar();
    }

    // --- PERSISTENCIA ---

    private void guardar() {
        serializador.writeOneObject(this.jugadores);
    }


    private void cargar() {
        Object datos = serializador.readFirstObject();
        if (datos != null && datos instanceof List) {
            this.jugadores = (List<Jugador>) datos;
        } else {
            this.jugadores = new ArrayList<>();
        }
    }
}