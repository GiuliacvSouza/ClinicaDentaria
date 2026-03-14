package app;

import bll.UtilizadorService;
import bll.PacienteService;
import bll.SeguroService;

import model.Utilizador;
import model.Paciente;
import model.Seguro;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"bll", "dal", "model"})
public class MainApplication implements CommandLineRunner {

    private final UtilizadorService utilizadorService;
    private final PacienteService pacienteService;
    private final SeguroService seguroService;

    public MainApplication(
            UtilizadorService utilizadorService,
            PacienteService pacienteService,
            SeguroService seguroService) {

        this.utilizadorService = utilizadorService;
        this.pacienteService = pacienteService;
        this.seguroService = seguroService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) {

        try {

            System.out.println("=== TESTE DO SISTEMA ===");

            Utilizador u = new Utilizador();
            u.setPrimeiroNome("Joao");
            u.setUltimoNome("Silva");
            u.setEmail("joao@email.com");

            utilizadorService.salvar(u);

            System.out.println("Utilizador criado");

            Paciente p = new Paciente();
            p.setUtilizador(u);

            pacienteService.salvar(p);

            System.out.println("Paciente criado");

            Seguro s = new Seguro();
            s.setNomeSeguro("Medicare");

            seguroService.salvar(s);

            System.out.println("Seguro criado");

            System.out.println("=== TESTE FINALIZADO ===");

        } catch (Exception e) {

            System.out.println("Erro: " + e.getMessage());

        }

    }
}