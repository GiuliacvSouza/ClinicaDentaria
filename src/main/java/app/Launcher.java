package app;

public class Launcher {
    public static void main(String[] args) {
        // Ao iniciar via Launcher, marcar para pular a inserção automática de dados
        System.setProperty("app.skipSeed", "true");
        MainFX.main(args);
    }
}