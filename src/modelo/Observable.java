package modelo;

public interface Observable {
    void notificar(Evento evento);
    public void enlazarObservador(Observador o);
}
