package modelo;

import java.util.ArrayList;
import java.util.List;

public class Mano {

    private final List<Carta> cartas = new ArrayList<>();

    public List<Carta> getCartas() {
        return cartas;
    }

    public void agregarCarta(Carta c) {
        cartas.add(c);
    }

    public Carta quitarCarta(int index) {
        if (index < 0 || index >= cartas.size()) return null;
        return cartas.remove(index);
    }

    public boolean estaVacia() {
        return cartas.isEmpty();
    }

    public int size() {
        return cartas.size();
    }

    public Carta get(int index) {
        return cartas.get(index);
    }

    public Carta robarDelMazo(Mazo mazo) {
        Carta c = mazo.robarCarta();
        if (c != null) cartas.add(c);
        return c;
    }
}
