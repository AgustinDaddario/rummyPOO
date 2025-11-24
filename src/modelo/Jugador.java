package modelo;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

    //Atributos
    private final String nombre;
    private final Mano mano = new Mano();
    private int puntos = 0;

    private boolean bajoEnEstaRonda = false;
    private boolean hizoRummyEnEstaRonda = false;
    private boolean eliminado = false;

    // Constructor
    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public Mano getMano() {
        return mano;
    }

    public int getPuntos() {
        return puntos;
    }

    // Manejo de cartas
    public void agregarCarta(Carta c) {
        mano.agregarCarta(c);
    }

    public Carta quitarCarta(int indice) {
        return mano.quitarCarta(indice);
    }

    public boolean sinCartas() {
        return mano.estaVacia();
    }

    public void marcarBajoCombinacion() {
        bajoEnEstaRonda = true;
    }

    public boolean yaBajoEnEstaRonda() {
        return bajoEnEstaRonda;
    }

    public void marcarRummy() {
        hizoRummyEnEstaRonda = true;
    }

    public boolean hizoRummyEnEstaRonda() {
        return hizoRummyEnEstaRonda;
    }

    // Manejo de Puntos
    public void sumarPuntos(int p) {
        puntos += p;
    }

    public void resetearPuntos() {
        puntos = 0;
    }

    // Estado del jugador
    public boolean estaEliminado() {
        return eliminado;
    }

    public void eliminar() {
        eliminado = true;
    }


    //setters
    public void setBajoEnEstaRonda(boolean valor) {
        this.bajoEnEstaRonda = valor;
    }

    public void setHizoRummyEnEstaRonda(boolean valor) {
        this.hizoRummyEnEstaRonda = valor;
    }


    /*
      Crear combinación desde índices marcados en mano del jugador.
     */
    public Combinacion bajarCombinacion(List<Integer> indices) {

        if (indices.size() < 3) return null;

        // ordeno índices desc para no romper posiciones
        indices.sort((a, b) -> b - a);

        List<Carta> seleccionadas = new ArrayList<>();

        for (int pos : indices) {
            if (pos < 0 || pos >= mano.size()) return null;
            seleccionadas.add(mano.get(pos));
        }

        Combinacion comb = new Combinacion(seleccionadas);

        if (!comb.esValida()) return null;

        // remover de la mano
        for (int pos : indices) {
            quitarCarta(pos);
        }

        // marcar jugada
        marcarBajoCombinacion();

        if (sinCartas() && !yaBajoEnEstaRonda()) {
            marcarRummy();
        }

        return comb;
    }

    /*
      Intentar agregar una carta del jugador a una combinación existente.
     */
    public boolean agregarCartaA(Combinacion combinacion, int indiceCarta) {

        if (indiceCarta < 0 || indiceCarta >= mano.size())
            return false;

        // toda la lógica de validación está en Combinacion
        return combinacion.intentarAgregarCarta(this, indiceCarta);
    }
}
