package bll;

import dal.ConsultaRepository;
import model.Consulta;
import model.enums.EstadoConsulta;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;

    public ConsultaService(ConsultaRepository repository) {
        this.repository = repository;
    }

    public Consulta agendarConsulta(Consulta consulta) {

        if (consulta.getIdPaciente() == null) {
            throw new RuntimeException("Consulta deve ter um paciente.");
        }

        if (consulta.getIdDentista() == null) {
            throw new RuntimeException("Consulta deve ter um dentista.");
        }

        if (consulta.getDataHoraInicio().isBefore(Instant.now())) {
            throw new RuntimeException("Não é possível agendar consulta no passado.");
        }

        return repository.save(consulta);
    }

    public List<Consulta> listarTodas() {
        return repository.findAll();
    }

    public List<Consulta> listarPorStatus(EstadoConsulta status) {
        return repository.findByStatus(status);
    }

    public Consulta buscarPorId(Integer id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
    }

    public Consulta atualizar(Consulta consulta) {

        buscarPorId(consulta.getId());

        return repository.save(consulta);
    }

    public void cancelar(Integer id) {

        Consulta consulta = buscarPorId(id);

        repository.delete(consulta);
    }
}
