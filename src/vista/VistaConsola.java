package vista;

import modelo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class VistaConsola implements IVistaRummy, Observador {

    private final Scanner sc = new Scanner(System.in);

    // ----------------------------- PEDIDOS -----------------------------

    @Override
    public String pedirNombreJugador(int numero) {
        System.out.print("Ingresá el nombre del jugador " + numero + ": ");
        return sc.nextLine();
    }

    @Override
    public int pedirModoDeJuego() {
        System.out.println("\nElegí el modo de juego:");
        System.out.println("1) Modo Exprés (1 ronda)");
        System.out.println("2) Modo Límite de puntos");
        System.out.print("Opción: ");
        return leerEntero();
    }

    @Override
    public int pedirCantidadJugadores() {
        System.out.println("\t\t\t\t ---- BIENVENIDOS AL RUMMY ---- \t\t\t\t");
        System.out.print("\n¿Con cuántos jugadores vas a jugar? (2, 3 o 4): ");
        int cant = leerEntero();
        while (cant < 2 || cant > 4) {
            System.out.print("Cantidad inválida. Ingresá 2, 3 o 4: ");
            cant = leerEntero();
        }
        return cant;
    }


    @Override
    public List<Integer> pedirIndicesCombinacion(Jugador jugador) {
        System.out.println("\nIngresá los índices separados por espacio (ej: 0 2 5): ");
        String linea = sc.nextLine();

        List<Integer> indices = new ArrayList<>();

        if (linea.trim().isEmpty()) return indices;

        String[] nums = linea.split(" ");

        for (String n : nums) {
            try {
                indices.add(Integer.parseInt(n));
            } catch (Exception e) {
                System.out.println("Índice inválido: " + n);
            }
        }
        return indices;
    }

    @Override
    public int pedirLimiteDePuntos() {
        System.out.print("Ingresá el límite de puntos: ");
        return leerEntero();
    }

    @Override
    public int pedirOpcionRobo(Jugador jugador) {
        System.out.println("\n" + jugador.getNombre() + ", desde dónde querés robar?");
        System.out.println("1) Mazo");
        System.out.println("2) Pila de descarte");
        System.out.print("Opción: ");
        return leerEntero();
    }

    @Override
    public int pedirIndiceCartaADescartar(Jugador jugador) {
        System.out.print("\n" + jugador.getNombre() + ", elegí la carta a descartar: ");
        return leerEntero();
    }


    @Override
    public int pedirCartaParaAgregar(Jugador jugador) {
        System.out.print("Elegí índice de carta para agregar: ");
        return leerEntero();
    }

    @Override
    public boolean pedirReenganche(Jugador jugador) {
        System.out.println("\n" + jugador.getNombre() + " superó el límite de puntos.");
        System.out.println("¿Querés reengancharte?");
        System.out.println("1) Sí");
        System.out.println("2) No");
        System.out.print("Opción: ");
        int op = leerEntero();
        return op == 1;
    }

    @Override
    public int pedirCombinacionATocar(List<Combinacion> tapete) {

        if (tapete.isEmpty()) {
            System.out.println("No hay combinaciones. Ingresá -1 para volver.");
        }

        while (true) {
            System.out.print("Elegí número de combinación (-1 para volver): ");
            int n = leerEntero();

            if (n == -1) return -1; // salir

            if (n >= 0 && n < tapete.size()) return n;

            System.out.println("Número inválido, intentá de nuevo.");
        }
    }


    // ----------------------------- MOSTRAR -----------------------------

    @Override
    public void mostrarEstadoJugador(Jugador jugador) {
        System.out.println("\nTurno de: " + jugador.getNombre());
        System.out.println("Cartas en mano:");
        List<Carta> mano = jugador.getMano().getCartas();
        for (int i = 0; i < mano.size(); i++) {
            System.out.println("[" + i + "] " + mano.get(i));
        }
    }

    @Override
    public void mostrarCartaDescarteActual(Carta carta) {
        System.out.println("Carta en el descarte: " + (carta == null ? "(vacío)" : carta));
    }

    @Override
    public void mostrarMensaje(String msg) {
        System.out.println(msg);
    }

    @Override
    public void mostrarGanadorRonda(Jugador ganador) {
        System.out.println("─── Ganó la ronda: " + ganador.getNombre() + " ───");
    }

    @Override
    public void mostrarGanadorFinal(Jugador ganador) {
        System.out.println("\n════════════════════");
        System.out.println(" GANADOR FINAL: " + ganador.getNombre());
        System.out.println("════════════════════");
    }

    @Override
    public void mostrarTapete(List<Combinacion> tapete) {
        System.out.println("\n════════════════ TAPETE ════════════════");

        if (tapete.isEmpty()) {
            System.out.println("   (Todavía no hay combinaciones en mesa)");
            System.out.println("════════════════════════════════════════");
            return;
        }

        for (int i = 0; i < tapete.size(); i++) {
            System.out.print("[" + i + "]  ");
            for (Carta c : tapete.get(i).getCartas()) {
                System.out.print(c + "  ");
            }
            System.out.println();
        }

        System.out.println("════════════════════════════════════════");
    }

    @Override
    public int leerEnteroSimple() {
        return leerEntero();
    }

    // ----------------------------- OBSERVER -----------------------------

    @Override
    public void actualizarRummy(Rummy r, Evento evento) {

        switch (evento) {

            case NUEVA_RONDA -> {
                System.out.println("\nNueva ronda iniciada");
            }

            case ROBAR_MAZO -> {
                System.out.println("Se robó del mazo.");
            }

            case ROBAR_DESCARTE -> {
                System.out.println("Se robó del descarte.");
            }

            case BAJAR_COMBINACION -> {
                System.out.println("Se bajó una combinación nueva al tapete.");
            }

            case AGREGAR_A_COMBINACION -> {
                System.out.println("Se agregó una carta a una combinación.");
            }

            case DESCARTAR -> {
                System.out.println("Se descartó una carta.");
            }

            case PUNTAJE_ACTUALIZADO -> {
                System.out.println("El puntaje ha sido actualizado.");
            }

            case JUGADOR_ELIMINADO -> {
                System.out.println("Un jugador ha sido eliminado.");
            }
        }
    }


    // --------------------------- UTIL ----------------------------

    private int leerEntero() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print("Número inválido, intentá de nuevo: ");
            }
        }
    }
}
