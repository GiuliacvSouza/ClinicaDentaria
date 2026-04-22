package bll;

import dal.AtendimentoRepository;
import model.Atendimento;
import model.Consulta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendimentoService {

    private final AtendimentoRepository repository;

    public AtendimentoService(AtendimentoRepository repository) {
        this.repository = repository;
    }

    public Atendimento salvar(Atendimento atendimento) {

        if (atendimento.getIdConsulta() == null) {
            throw new RuntimeException("O atendimento deve estar associado a uma consulta.");
        }

        if (Boolean.TRUE.equals(atendimento.getRetorno())
                && atendimento.getPeriodoRetorno() == null) {

            throw new RuntimeException("Período de retorno deve ser informado.");
        }

        return repository.save(atendimento);
    }

    public List<Atendimento> listarTodos() {
        return repository.findAll();
    }

    public Atendimento buscarPorId(Integer id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado"));
    }

    public Atendimento buscarPorConsulta(Consulta consulta) {
        if (consulta == null || consulta.getId() == null) {
            return null;
        }

        return repository.findByIdConsulta_Id(consulta.getId())
                .orElse(null);
    }

    public void excluir(Integer id) {

        Atendimento atendimento = buscarPorId(id);

        repository.delete(atendimento);
    }
}
