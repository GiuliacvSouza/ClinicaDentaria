package app;

import model.Assistente;
import model.Dentista;
import model.Recepcionista;
import model.Utilizador;

public final class SessionContext {

    private static Utilizador utilizadorLogado;
    private static Recepcionista recepcionistaLogado;
    private static Assistente assistenteLogado;
    private static Dentista dentistaLogado;

    private SessionContext() {
    }

    // ─── Rececionista ───────────────────────────────────────────────────────────

    public static void iniciarSessao(Utilizador utilizador, Recepcionista recepcionista) {
        utilizadorLogado = utilizador;
        recepcionistaLogado = recepcionista;
        assistenteLogado = null;
        dentistaLogado = null;
    }

    // ─── Assistente ─────────────────────────────────────────────────────────────

    public static void iniciarSessaoAssistente(Utilizador utilizador, Assistente assistente) {
        utilizadorLogado = utilizador;
        assistenteLogado = assistente;
        recepcionistaLogado = null;
        dentistaLogado = null;
    }

    // Dentista

    public static void iniciarSessaoDentista(Utilizador utilizador, Dentista dentista) {
        utilizadorLogado = utilizador;
        dentistaLogado = dentista;
        recepcionistaLogado = null;
        assistenteLogado = null;
    }

    // ─── Administrador ─────────────────────────────────────────────────────────

    public static void iniciarSessaoAdministrador(Utilizador utilizador) {
        utilizadorLogado = utilizador;
        recepcionistaLogado = null;
        assistenteLogado = null;
        dentistaLogado = null;
    }

    public static boolean isAdministrador() {
        return utilizadorLogado != null
                && "ADMINISTRADOR".equalsIgnoreCase(utilizadorLogado.getTipoUtilizador());
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

    public static Dentista getDentistaLogado() {
        return dentistaLogado;
    }

    public static boolean isRececionista() {
        return recepcionistaLogado != null;
    }

    public static boolean isAssistente() {
        return assistenteLogado != null;
    }

    public static boolean isDentista() {
        return dentistaLogado != null;
    }

    public static void limparSessao() {
        utilizadorLogado = null;
        recepcionistaLogado = null;
        assistenteLogado = null;
        dentistaLogado = null;
        currentQuery = null;
    }

    private static String currentQuery;

    public static String getCurrentQuery() {
        return currentQuery;
    }

    public static void setCurrentQuery(String query) {
        currentQuery = query;
    }

    public static void limparQuery() {
        currentQuery = null;
    }
}
