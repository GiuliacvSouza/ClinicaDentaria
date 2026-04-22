package bll;

import dal.AtendimentoProcedimentoRepository;
import model.AtendimentoProcedimento;
import model.AtendimentoProcedimentoId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendimentoProcedimentoService {

    private final AtendimentoProcedimentoRepository repository;

    public AtendimentoProcedimentoService(AtendimentoProcedimentoRepository repository) {
        this.repository = repository;
    }

    public AtendimentoProcedimento salvar(AtendimentoProcedimento ap) {

        if (ap.getIdAtendimento() == null) {
            throw new RuntimeException("Atendimento é obrigatório");
        }

        if (ap.getIdProcedimento() == null) {
            throw new RuntimeException("Procedimento é obrigatório");
        }

        if (ap.getId() == null) {
            AtendimentoProcedimentoId id = new AtendimentoProcedimentoId();
            id.setIdAtendimento(ap.getIdAtendimento().getId());
            id.setIdProcedimento(ap.getIdProcedimento().getId());
            ap.setId(id);
        }

        if (repository.existsById(ap.getId())) {
            throw new RuntimeException("Procedimento já registado neste atendimento.");
        }

        return repository.save(ap);
    }

    public List<AtendimentoProcedimento> listarTodos() {
        return repository.findAll();
    }

    public void excluir(AtendimentoProcedimentoId id) {
        repository.deleteById(id);
    }
}
