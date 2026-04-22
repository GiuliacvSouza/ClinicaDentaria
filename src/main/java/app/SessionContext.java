package app;

import model.Recepcionista;
import model.Utilizador;

public final class SessionContext {

    private static Utilizador utilizadorLogado;
    private static Recepcionista recepcionistaLogado;

    private SessionContext() {
    }

    public static void iniciarSessao(Utilizador utilizador, Recepcionista recepcionista) {
        utilizadorLogado = utilizador;
        recepcionistaLogado = recepcionista;
    }

    public static Utilizador getUtilizadorLogado() {
        return utilizadorLogado;
    }

    public static Recepcionista getRecepcionistaLogado() {
        return recepcionistaLogado;
    }

    public static void limparSessao() {
        utilizadorLogado = null;
        recepcionistaLogado = null;
    }
}
