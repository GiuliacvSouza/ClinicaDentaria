package app;

import model.Assistente;
import model.Recepcionista;
import model.Utilizador;

public final class SessionContext {

    private static Utilizador utilizadorLogado;
    private static Recepcionista recepcionistaLogado;
    private static Assistente assistenteLogado;

    private SessionContext() {
    }

    // ─── Rececionista ───────────────────────────────────────────────────────────

    public static void iniciarSessao(Utilizador utilizador, Recepcionista recepcionista) {
        utilizadorLogado = utilizador;
        recepcionistaLogado = recepcionista;
        assistenteLogado = null;
    }

    // ─── Assistente ─────────────────────────────────────────────────────────────

    public static void iniciarSessaoAssistente(Utilizador utilizador, Assistente assistente) {
        utilizadorLogado = utilizador;
        assistenteLogado = assistente;
        recepcionistaLogado = null;
    }

    // ─── Acesso ─────────────────────────────────────────────────────────────────

    public static Utilizador getUtilizadorLogado() {
        return utilizadorLogado;
    }

    public static Recepcionista getRecepcionistaLogado() {
        return recepcionistaLogado;
    }

    public static Assistente getAssistenteLogado() {
        return assistenteLogado;
    }

    public static boolean isRececionista() {
        return recepcionistaLogado != null;
    }

    public static boolean isAssistente() {
        return assistenteLogado != null;
    }

    public static void limparSessao() {
        utilizadorLogado = null;
        recepcionistaLogado = null;
        assistenteLogado = null;
    }
}
