package vista;

import java.util.ArrayList;
import java.util.List;


import controlador.Controlador;
import modelo.Carta;
import modelo.Combinacion;
import modelo.Evento;
import modelo.Jugador;

public class VistaConsola implements IVistaRummy {

    private Controlador controlador;

    @Override
    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    //Lectura de datos
    private String leerLinea() {
        if (System.console() != null) {
            return System.console().readLine();
        } else {
            try {
                return new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine();
            } catch (Exception e) {
                return "";
            }
        }
    }

    @Override
    public void iniciar() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("      RUMMY - MODO CONSOLA INICIADO    ");
        System.out.println("═══════════════════════════════════════");
        System.out.flush();
    }

    @Override
    public String pedirNombreJugador(int numero) {
        System.out.print("\nIngresá el nombre del jugador " + numero + ": ");
        System.out.flush();
        return this.leerLinea();
    }

    @Override
    public int pedirModoDeJuego() {
        System.out.println("\n--- CONFIGURACIÓN (Solo Anfitrión) ---");
        System.out.println("1) Modo Exprés (1 ronda)");
        System.out.println("2) Modo Límite de puntos");
        System.out.print("Opción: ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public int pedirCantidadJugadores() {
        System.out.print("\n¿Con cuántos jugadores vas a jugar? (2-4): ");
        System.out.flush();
        int cant;
        for(cant = this.leerEntero(); cant < 2 || cant > 4; cant = this.leerEntero()) {
            System.out.print("Cantidad inválida. Ingresá 2, 3 o 4: ");
            System.out.flush();
        }
        return cant;
    }

    @Override
    public List<Integer> pedirIndicesCombinacion(Jugador jugador) {
        System.out.println("\nIngresá los índices separados por espacio (ej: 0 2 5): ");
        String linea = this.leerLinea();
        List<Integer> indices = new ArrayList<>();
        if (linea.trim().isEmpty()) {
            return indices;
        } else {
            String[] nums = linea.split("\\s+");
            for(String n : nums) {
                try {
                    indices.add(Integer.parseInt(n));
                } catch (Exception e) {
                    System.out.println("Ignorando: " + n);
                }
            }
            return indices;
        }
    }

    @Override
    public int pedirLimiteDePuntos() {
        System.out.print("Ingresá el límite de puntos: ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public int pedirOpcionRobo(Jugador jugador) {
        System.out.println("\n--- TU TURNO: " + jugador.getNombre() + " ---");
        System.out.println("1) Robar del Mazo");
        System.out.println("2) Robar del Descarte");
        System.out.print("Opción: ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public int pedirIndiceCartaADescartar(Jugador jugador) {
        int max = Math.max(0, jugador.getMano().size() - 1);
        System.out.print("\nElegí el ÍNDICE de la carta a descartar (0 a " + max + "): ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public int pedirCartaParaAgregar(Jugador jugador) {
        int max = Math.max(0, jugador.getMano().size() - 1);
        System.out.print("Elegí el índice de tu carta para agregar (0 a " + max + "): ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public boolean pedirReenganche(Jugador jugador) {
        System.out.println("\n¡Perdiste! ¿Querés reengancharte con 0 puntos?");
        System.out.println("1) Sí");
        System.out.println("2) No");
        System.out.print("Opción: ");
        System.out.flush();
        int op = this.leerEntero();
        return op == 1;
    }

    @Override
    public int pedirCombinacionATocar(List<Combinacion> tapete) {
        if (tapete.isEmpty()) {
            System.out.println("No hay juegos en la mesa.");
            return -1;
        }
        System.out.print("Elegí el número del juego en la mesa (-1 para volver): ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public void mostrarEstadoJugador(Jugador jugador) {
        System.out.println("\n--- TUS CARTAS (" + jugador.getNombre() + ") ---");
        List<Carta> mano = jugador.getMano().getCartas();
        for(int i = 0; i < mano.size(); ++i) {
            System.out.println("[" + i + "] " + mano.get(i));
        }
        System.out.println("---------------------------");
    }

    @Override
    public void mostrarCartaDescarteActual(Carta carta) {
        System.out.println("POZO / DESCARTE: " + (carta == null ? "(Vacío)" : carta));
    }

    @Override
    public void mostrarMensaje(String msg) {
        System.out.println("[INFO]: " + msg);
    }

    @Override
    public void mostrarGanadorRonda(Jugador ganador) {
        System.out.println("\n*** GANÓ LA RONDA: " + ganador.getNombre() + " ***\n");
    }

    @Override
    public void mostrarGanadorFinal(Jugador ganador) {
        System.out.println("\n════════════════════════════════════");
        System.out.println("   ¡¡¡ GANADOR DEL PARTIDO: " + ganador.getNombre() + " !!!");
        System.out.println("════════════════════════════════════");
    }

    @Override
    public void mostrarTapete(List<Combinacion> tapete) {
        System.out.println("\n════════ MESA ════════");
        if (tapete.isEmpty()) {
            System.out.println("   (Vacía)");
        } else {
            for(int i = 0; i < tapete.size(); ++i) {
                System.out.print("Juego [" + i + "]: ");
                for(Carta c : tapete.get(i).getCartas()) {
                    System.out.print(c + "  ");
                }
                System.out.println();
            }
        }
        System.out.println("══════════════════════");
    }

    @Override
    public int leerEnteroSimple() {

        System.out.println("\n--- ¿QUÉ QUERÉS HACER? ---");
        System.out.println("1) Bajar una combinación nueva");
        System.out.println("2) Agregar carta a un juego en la mesa");
        System.out.println("3) Terminar turno (ir a descartar)");

        System.out.print("Opción: ");
        System.out.flush();
        return this.leerEntero();
    }

    @Override
    public void actualizar(Evento evento) {
        switch (evento) {
            case NUEVA_RONDA -> System.out.println("\n>>> SE INICIÓ UNA NUEVA RONDA <<<");
            case ROBAR_MAZO -> System.out.println("> Alguien robó del mazo.");
            case ROBAR_DESCARTE -> System.out.println("> Alguien robó del descarte.");
            case DESCARTAR -> System.out.println("> Alguien descartó. (Fin de turno)");
            case BAJAR_COMBINACION -> System.out.println("> ¡Se bajó juego nuevo!");
            case AGREGAR_A_COMBINACION -> System.out.println("> ¡Se agregó carta a juego!");
            case PUNTAJE_ACTUALIZADO -> System.out.println("> Fin de ronda. Calculando puntos...");
            case JUGADOR_ELIMINADO -> System.out.println("> Un jugador ha sido eliminado.");
        }
        System.out.flush();
    }

    private int leerEntero() {
        while(true) {
            try {
                String input = this.leerLinea();
                return Integer.parseInt(input);
            } catch (NumberFormatException var2) {
                System.out.print("Entrada inválida. Ingresá un número: ");
                System.out.flush();
            }
        }
    }
}