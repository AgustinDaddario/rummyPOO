import controlador.Controlador;
import vista.IVistaRummy;
import vista.VistaConsola;

public class Main {
    public static void main(String[] args) {

        // Creo la vista que se usa por consola
        IVistaRummy vista = new VistaConsola();

        // Creo el controlador con la vista
        Controlador controlador = new Controlador(vista);

        // Arranco el juego
        controlador.jugar();
    }
}
