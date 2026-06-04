package bll;

import dal.AssistenteRepository;
import model.Assistente;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class AssistenteService {

    private final AssistenteRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public AssistenteService(AssistenteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Assistente salvar(Assistente assistente) {
        if (assistente.getUtilizador() == null)
            throw new RuntimeException("Assistente deve estar associado a um utilizador.");
        if (assistente.getDataAdmissao() != null && assistente.getDataAdmissao().isAfter(LocalDate.now()))
            throw new RuntimeException("Data de admissão não pode ser futura.");

        assistente.setUtilizador(entityManager.merge(assistente.getUtilizador())); //  
        return repository.save(assistente);
    }

    public List<Assistente> listarTodos() { return repository.findAll(); }

    public Assistente buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assistente não encontrado"));
    }

    public Assistente buscarPorUtilizadorId(Integer utilizadorId) {
        return repository.findByUtilizadorId(utilizadorId)
                .orElseThrow(() -> new RuntimeException("Assistente não encontrado para o utilizador: " + utilizadorId));
    }

    public void desativar(Integer id) {
        Assistente assistente = buscarPorId(id);
        assistente.setAtivo(false);
        repository.save(assistente);
    }
}