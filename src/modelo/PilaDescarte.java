package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PilaDescarte implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Carta> PDcartas = new ArrayList<>();

    public void descartar(Carta carta) {
        PDcartas.add(carta); // agrega al final
    }

    public Carta verSuperior() {
        if (PDcartas.isEmpty()) return null;
        return PDcartas.get(PDcartas.size() - 1); // última carta
    }

    public Carta tomarSuperior() {
        if (PDcartas.isEmpty()) return null;
        return PDcartas.remove(PDcartas.size() - 1); // saca la última carta
    }

    public boolean estaVacia() {
        return PDcartas.isEmpty();
    }

    // Mueve todas las cartas menos la superior al mazo
    public void pasarCartasAlMazo(Mazo mazo) {

        // Si hay 0 o 1, no hay nada para pasar
        if (PDcartas.size() <= 1) return;

        // Guardamos la superior
        Carta superior = PDcartas.get(PDcartas.size() - 1);

        // Movemos todas las demás al mazo
        for (int i = 0; i < PDcartas.size() - 1; i++) {
            mazo.agregarCarta(PDcartas.get(i));
        }

        // Limpiamos y dejamos solo la superior
        PDcartas.clear();
        PDcartas.add(superior);
    }


}
