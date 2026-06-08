package bll;

import dal.DentistaRepository;
import model.Dentista;
import model.Utilizador;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DentistaService {

    private final DentistaRepository repository;
    private final UtilizadorService utilizadorService;

    @PersistenceContext
    private EntityManager entityManager;

    public DentistaService(DentistaRepository repository, UtilizadorService utilizadorService) {
        this.repository = repository;
        this.utilizadorService = utilizadorService;
    }

    @Transactional
    public Dentista salvar(Dentista dentista) {
        if (dentista.getUtilizador() == null) {
            throw new RuntimeException("Dentista deve estar associado a um utilizador.");
        }
        if (dentista.getNumeroOmd() == null || dentista.getNumeroOmd().isBlank()) {
            throw new RuntimeException("Número OMD é obrigatório.");
        }
        if (dentista.getDataAdmissao() != null && dentista.getDataAdmissao().isAfter(LocalDate.now())) {
            throw new RuntimeException("Data de admissão não pode ser futura.");
        }
        if (dentista.getHorarioEntrada() != null && dentista.getHorarioSaida() != null
                && !dentista.getHorarioSaida().isAfter(dentista.getHorarioEntrada())) {
            throw new RuntimeException("Horário de saída deve ser após horário de entrada.");
        }

        dentista.setUtilizador(entityManager.merge(dentista.getUtilizador()));
        return repository.save(dentista);
    }

    public List<Dentista> listarTodos() {
        return repository.findAllComUtilizador();
    }

    @Transactional
    public List<Dentista> listarAtivosOuCriarPadrao() {
        List<Dentista> ativos = listarDentistasAtivosUnicos();
        if (!ativos.isEmpty()) {
            return ativos;
        }

        criarDentistaPadrao("Mariana", "Pereira", "927451320", "OMD-21001", LocalTime.of(8, 30), LocalTime.of(17, 30));
        criarDentistaPadrao("Tiago", "Almeida", "927451321", "OMD-21002", LocalTime.of(9, 0), LocalTime.of(18, 0));
        criarDentistaPadrao("Ines", "Carvalho", "927451322", "OMD-21003", LocalTime.of(10, 0), LocalTime.of(19, 0));

        return listarDentistasAtivosUnicos();
    }

    public Dentista buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dentista não encontrado."));
    }

    public Dentista buscarPorUtilizadorId(Integer idUtilizador) {
        if (idUtilizador == null) {
            return null;
        }
        return repository.findByUtilizadorId(idUtilizador)
                .orElse(null);
    }

    public Dentista desativar(Integer id) {
        Dentista dentista = buscarPorId(id);
        dentista.setAtivo(false);
        return repository.save(dentista);
    }

    private List<Dentista> listarDentistasAtivosUnicos() {
        Map<String, Dentista> unicosPorNome = new LinkedHashMap<>();
        for (Dentista dentista : repository.findAllComUtilizador()) {
            if (dentista == null || Boolean.FALSE.equals(dentista.getAtivo())) {
                continue;
            }

            String chave = normalizar(formatarNome(dentista.getUtilizador()));
            if (chave.isBlank()) {
                continue;
            }
            unicosPorNome.putIfAbsent(chave, dentista);
        }

        List<Dentista> dentistas = new ArrayList<>(unicosPorNome.values());
        dentistas.sort(Comparator.comparing(d -> normalizar(formatarNome(d.getUtilizador()))));
        return dentistas;
    }

    private void criarDentistaPadrao(String primeiroNome, String ultimoNome, String nif, String numeroOmd,
                                     LocalTime horarioEntrada, LocalTime horarioSaida) {
        Utilizador utilizador = new Utilizador();
        utilizador.setPrimeiroNome(primeiroNome);
        utilizador.setUltimoNome(ultimoNome);
        utilizador.setTipoUtilizador("DENTISTA");
        utilizador.setEmail(("dentista." + nif + "@clinica.pt").toLowerCase());
        utilizador.setNif(nif);
        utilizador.setTelemovel(nif);
        utilizador.setSenha("Clinica2026!");
        utilizador.setStatus("ATIVO");

        Utilizador utilizadorGuardado = utilizadorService.salvar(utilizador);

        Dentista dentista = new Dentista();
        dentista.setUtilizador(utilizadorGuardado);
        dentista.setNumeroOmd(numeroOmd);
        dentista.setDataAdmissao(LocalDate.now().minusMonths(6));
        dentista.setHorarioEntrada(horarioEntrada);
        dentista.setHorarioSaida(horarioSaida);
        dentista.setAtivo(true);
        salvar(dentista);
    }

    private String formatarNome(Utilizador utilizador) {
        if (utilizador == null) {
            return "";
        }
        String primeiroNome = utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome().trim() : "";
        String ultimoNome = utilizador.getUltimoNome() != null ? utilizador.getUltimoNome().trim() : "";
        return (primeiroNome + " " + ultimoNome).trim();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }
}
