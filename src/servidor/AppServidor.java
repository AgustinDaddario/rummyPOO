package servidor;
import java.rmi.RemoteException;
import java.util.ArrayList;

import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import modelo.Rummy;
import javax.swing.*;
import javax.swing.JOptionPane;


public class AppServidor {

    public static void main(String[] args) throws RemoteException{
        ArrayList<String> ips = Util.getIpDisponibles();
        String ip = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione la IP en la que escuchará peticiones el servidor", "IP del servidor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ips.toArray(),
                null
        );
        String port = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione el puerto en el que escuchará peticiones el servidor", "Puerto del servidor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                8888
        );
        Rummy modelo = new Rummy();

        // PREGUNTO SI SE QUIERE CARGAR PARTIDA PREVIA
        int cargar = JOptionPane.showConfirmDialog(null,
                "¿Desea recuperar la última partida guardada?",
                "Cargar Partida", JOptionPane.YES_NO_OPTION);

        if (cargar == JOptionPane.YES_OPTION) {
            modelo.cargarEstado();
            System.out.println("Partida recuperada correctamente.");
        }

        Servidor servidor = new Servidor("127.0.0.1", 8888);
        try {
            servidor.iniciar(modelo);
            System.out.println("Servidor levantado correctamente!");
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RMIMVCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}