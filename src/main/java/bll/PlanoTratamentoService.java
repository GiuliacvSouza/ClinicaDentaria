package bll;

import dal.DentistaRepository;
import dal.PacienteRepository;
import dal.PlanoTratamentoRepository;
import model.PlanoTratamento;
import model.enums.EstadoPlanoTratamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlanoTratamentoService {

    private final PlanoTratamentoRepository repository;
    private final PacienteRepository pacienteRepository;
    private final DentistaRepository dentistaRepository;

    public PlanoTratamentoService(PlanoTratamentoRepository repository,
                                  PacienteRepository pacienteRepository,
                                  DentistaRepository dentistaRepository) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.dentistaRepository = dentistaRepository;
    }

    public List<PlanoTratamento> listarTodos() {
        return repository.findAllComDetalhes();
    }

    public List<PlanoTratamento> listarPorPaciente(Integer pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return repository.findByPacienteIdComDetalhes(pacienteId);
    }

    public List<PlanoTratamento> listarPorDentista(Integer dentistaId) {
        if (dentistaId == null) {
            return List.of();
        }
        return repository.findByDentista_IdOrderByIdDesc(dentistaId);
    }

    @Transactional
    public PlanoTratamento salvar(PlanoTratamento plano) {
        if (plano.getPaciente() == null || plano.getPaciente().getId() == null) {
            throw new RuntimeException("Selecione o paciente.");
        }
        if (plano.getDentista() == null || plano.getDentista().getId() == null) {
            throw new RuntimeException("Dentista obrigatorio.");
        }
        if (plano.getObjetivo() == null || plano.getObjetivo().isBlank()) {
            throw new RuntimeException("Informe o objetivo do tratamento.");
        }

        plano.setPaciente(pacienteRepository.findById(plano.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado.")));
        plano.setDentista(dentistaRepository.findById(plano.getDentista().getId())
                .orElseThrow(() -> new RuntimeException("Dentista nao encontrado.")));
        if (plano.getEstado() == null) {
            plano.setEstado(EstadoPlanoTratamento.PLANEADO);
        }
        if (plano.getValorEstimado() == null) {
            plano.setValorEstimado(BigDecimal.ZERO);
        }

        return repository.save(plano);
    }
}
