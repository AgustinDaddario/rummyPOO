package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Mazo implements Serializable{

    private static final long serialVersionUID = 1L;


    //Necesito una lista para almacenar las cartas (mazo)
    private final List<Carta> cartas = new ArrayList<>();

    public Mazo() {
        // Generar las 52 cartas
        for (Palo p : Palo.values()) {
            for (Valor v : Valor.values()) {
                cartas.add(new Carta(v, p));
            }
        }
        // Mezclamos las cartas en el mazo
        Collections.shuffle(cartas);
    }

    public boolean estaVacio() {
        return cartas.isEmpty();
    }

    public Carta robarCarta() {
        if (cartas.isEmpty()) return null;
        return cartas.remove(cartas.size() - 1); // saca la última carta
    }

    public void barajar() {
        Collections.shuffle(cartas);
    }

    //En este caso se usara cuando no queden cartas en el mazo original, entonces se agregan desde la pila de descartes por ej
    public void agregarCarta(Carta carta) {
        cartas.add(carta);
    }

}
