package bll;

import dal.DentistaRepository;
import dal.PacienteRepository;
import dal.PrescricaoRepository;
import model.Prescricao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescricaoService {

    private final PrescricaoRepository repository;
    private final PacienteRepository pacienteRepository;
    private final DentistaRepository dentistaRepository;

    public PrescricaoService(PrescricaoRepository repository,
                             PacienteRepository pacienteRepository,
                             DentistaRepository dentistaRepository) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.dentistaRepository = dentistaRepository;
    }

    public List<Prescricao> listarTodas() {
        return repository.findAllComDetalhes();
    }

    public List<Prescricao> listarPorPaciente(Integer pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return repository.findByPacienteIdComDetalhes(pacienteId);
    }

    public List<Prescricao> listarPorDentista(Integer dentistaId) {
        if (dentistaId == null) {
            return List.of();
        }
        return repository.findByDentista_IdOrderByDataDesc(dentistaId);
    }

    @Transactional
    public Prescricao salvar(Prescricao prescricao) {
        if (prescricao.getPaciente() == null || prescricao.getPaciente().getId() == null) {
            throw new RuntimeException("Selecione o paciente.");
        }
        if (prescricao.getDentista() == null || prescricao.getDentista().getId() == null) {
            throw new RuntimeException("Dentista obrigatorio.");
        }
        if (prescricao.getMedicamento() == null || prescricao.getMedicamento().isBlank()) {
            throw new RuntimeException("Informe o medicamento.");
        }
        if (prescricao.getPosologia() == null || prescricao.getPosologia().isBlank()) {
            throw new RuntimeException("Informe a posologia.");
        }

        prescricao.setPaciente(pacienteRepository.findById(prescricao.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado.")));
        prescricao.setDentista(dentistaRepository.findById(prescricao.getDentista().getId())
                .orElseThrow(() -> new RuntimeException("Dentista nao encontrado.")));
        if (prescricao.getData() == null) {
            prescricao.setData(LocalDate.now());
        }
        if (prescricao.getAssinatura() == null || prescricao.getAssinatura().isBlank()) {
            var u = prescricao.getDentista().getUtilizador();
            String nome = u != null ? ((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim()) : "";
            prescricao.setAssinatura(nome.isBlank() ? "Dentista" : nome);
        }

        return repository.save(prescricao);
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
