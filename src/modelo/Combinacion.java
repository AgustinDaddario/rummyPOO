package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Combinacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Carta> cartas = new ArrayList<>();

    // -------------------------------------------------------
    // CONSTRUCTOR PRINCIPAL
    // -------------------------------------------------------
    public Combinacion(List<Carta> iniciales) {
        cartas.addAll(iniciales);
    }

    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }


    // CREAR COMBINACIÓN DESDE LA MANO DEL JUGADOR

    public static Combinacion crearDesdeIndices(Jugador jugador, List<Integer> indices) {

        if (indices.size() < 3) return null;

        // Ordeno para quitar sin romper índices
        indices.sort((a, b) -> b - a);

        List<Carta> seleccionadas = new ArrayList<>();

        for (int pos : indices) {
            if (pos < 0 || pos >= jugador.getMano().size()) return null;
            seleccionadas.add(jugador.getMano().get(pos));
        }

        Combinacion comb = new Combinacion(seleccionadas);

        // Validar si es grupo/escalera
        if (!comb.esValida()) return null;

        // Sacar cartas del jugador
        for (int pos : indices) {
            jugador.quitarCarta(pos);
        }

        // Marcar que bajó
        jugador.marcarBajoCombinacion();

        // Hizo Rummy?
        if (jugador.sinCartas() && !jugador.yaBajoEnEstaRonda()) {
            jugador.marcarRummy();
        }

        return comb;
    }

    // AGREGAR CARTA A UNA COMBINACIÓN EXISTENTE

    public boolean intentarAgregarCarta(Jugador jugador, int indiceCarta) {

        if (indiceCarta < 0 || indiceCarta >= jugador.getMano().size())
            return false;

        Carta carta = jugador.getMano().get(indiceCarta);
        cartas.add(carta);

        // Si rompe la combinación, la saco
        if (!esValida()) {
            cartas.remove(cartas.size() - 1);
            return false;
        }

        // Si es válida → se quita de la mano
        jugador.quitarCarta(indiceCarta);

        return true;
    }


    //VALIDAR COMBINACION

    public boolean esValida() {
        return esGrupo() || esEscalera();
    }


    // ES GRUPO (mismo valor)

    public boolean esGrupo() {
        if (cartas.size() < 3) return false;

        Valor valor = cartas.get(0).getValor();
        for (Carta c : cartas) {
            if (c.getValor() != valor) return false;
        }
        return true;
    }


    //ES ESCALERA (mismo palo + consecutivas)

    public boolean esEscalera() {
        if (cartas.size() < 3) return false;

        Palo palo = cartas.get(0).getPalo();
        for (Carta c : cartas) {
            if (c.getPalo() != palo) return false;
        }

        // Ordenar por valor
        List<Carta> ordenadas = new ArrayList<>(cartas);
        ordenadas.sort((a, b) -> a.getValor().ordinal() - b.getValor().ordinal());

        // Consecutiva normal
        boolean consecutiva = true;
        for (int i = 0; i < ordenadas.size() - 1; i++) {
            int v1 = ordenadas.get(i).getValor().ordinal();
            int v2 = ordenadas.get(i + 1).getValor().ordinal();
            if (v2 != v1 + 1) {
                consecutiva = false;
                break;
            }
        }
        if (consecutiva) return true;

        // Caso AS al final
        Carta as = null;
        for (Carta c : ordenadas) {
            if (c.getValor().name().equals("AS")) {
                as = c;
                break;
            }
        }
        if (as == null) return false;

        // mover AS al final y probar de nuevo
        List<Carta> ajustada = new ArrayList<>(ordenadas);
        ajustada.remove(as);
        ajustada.add(as);

        for (int i = 0; i < ajustada.size() - 1; i++) {
            int v1 = ajustada.get(i).getValor().ordinal();
            int v2 = ajustada.get(i + 1).getValor().ordinal();
            if (v2 != v1 + 1) return false;
        }

        return true;
    }
}
