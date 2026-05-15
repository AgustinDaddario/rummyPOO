package cliente;

import javax.swing.JOptionPane;
import java.util.ArrayList;

import ar.edu.unlu.rmimvc.cliente.Cliente;
import ar.edu.unlu.rmimvc.Util;
import controlador.Controlador;
import vista.IVistaRummy;
import vista.VistaGrafica;
import vista.VistaConsola;

public class AppCliente {

    public static void main(String[] args) {

        // 1. CONFIGURACIÓN DE RED
        ArrayList<String> ips = Util.getIpDisponibles();
        String ip = (String) JOptionPane.showInputDialog(
                null, "Seleccione la IP del cliente", "IP Cliente",
                JOptionPane.QUESTION_MESSAGE, null, ips.toArray(), null
        );
        String port = (String) JOptionPane.showInputDialog(
                null, "Seleccione el puerto del cliente", "Puerto Cliente",
                JOptionPane.QUESTION_MESSAGE, null, null, 9999
        );
        String ipServidor = (String) JOptionPane.showInputDialog(
                null, "Seleccione la IP del servidor", "IP Servidor",
                JOptionPane.QUESTION_MESSAGE, null, null, null
        );
        String portServidor = (String) JOptionPane.showInputDialog(
                null, "Seleccione el puerto del servidor", "Puerto Servidor",
                JOptionPane.QUESTION_MESSAGE, null, null, 8888
        );

        // 2. SELECCIÓN DE VISTA
        String[] opciones = {"Gráfica (Ventanas)", "Consola (Texto)"};
        int eleccionVista = JOptionPane.showOptionDialog(
                null,
                "¿Con qué interfaz querés jugar?",
                "Seleccionar Vista",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        IVistaRummy vista;
        boolean esConsola = false;

        if (eleccionVista == 0) {
            vista = new VistaGrafica();
        } else {
            // AVISO IMPORTANTE PARA MODO CONSOLA
            JOptionPane.showMessageDialog(null,
                    "Modo Consola seleccionado.\nMirá el OUTPUT de tu IDE para jugar.",
                    "Modo Consola", JOptionPane.INFORMATION_MESSAGE);
            vista = new VistaConsola();
            esConsola = true;
        }

        // 3. INICIALIZACIÓN
        Controlador controlador = new Controlador(vista);
        vista.setControlador(controlador);

        Cliente c = new Cliente(ip, Integer.parseInt(port), ipServidor, Integer.parseInt(portServidor));

        try {
            c.iniciar(controlador);
            vista.iniciar();

            //
            // 4. SELECCIÓN DE ROL (Host vs Invitado)
            //

            if (esConsola) {
                System.out.println(">>> ¡ATENCIÓN! REVISÁ LAS VENTANAS EMERGENTES <<<");
                System.out.println(">>> EL JUEGO ESTÁ ESPERANDO QUE CONFIRMES SI SOS ANFITRIÓN <<<");
            }

            int respuesta = JOptionPane.showConfirmDialog(null,
                    "¿Sos el anfitrión (Host) de la partida?\n(Solo UNO debe decir que SÍ)",
                    "Configuración de Rol",
                    JOptionPane.YES_NO_OPTION);

            boolean soyAnfitrion = (respuesta == JOptionPane.YES_OPTION);

            if (esConsola) {
                System.out.println(">>> Rol confirmado. Iniciando lógica... <<<");
            }

            // 5. LANZAMIENTO DEL JUEGO
            new Thread(() -> {
                try {
                    if (!soyAnfitrion) {
                        Thread.sleep(1000);
                    }
                    controlador.jugar(soyAnfitrion);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al conectar: " + e.getMessage());
        }
    }
}