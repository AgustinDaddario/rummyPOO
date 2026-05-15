package modelo;

import java.io.Serializable;

public class Carta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Valor valor;
    private final Palo palo;

    public Carta(Valor valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;
    }

    public Valor getValor() {
        return valor;
    }
    public Palo getPalo() {
        return palo;
    }

    //El punto que vale la carta.

    public int getPuntos() {

        switch (valor) {
            case DOS:
                return 2;
            case TRES:
                return 3;
            case CUATRO:
                return 4;
            case CINCO:
                return 5;
            case SEIS:
                return 6;
            case SIETE:
                return 7;
            case OCHO:
                return 8;
            case NUEVE:
                return 9;
            case DIEZ:
            case J:
            case Q:
            case K:
                return 10;
            case AS:
                return 15;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return valor + " de " + palo;
    }

}
