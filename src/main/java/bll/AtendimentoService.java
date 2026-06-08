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

            throw new RuntimeException("Periodo de retorno deve ser informado.");
        }

        return repository.save(atendimento);
    }

    public List<Atendimento> listarTodos() {
        return repository.findAll();
    }

    public List<Atendimento> listarPorPaciente(Integer pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return repository.findByPacienteIdComDetalhes(pacienteId);
    }

    public Atendimento buscarPorId(Integer id) {

        return repository.findByIdComDetalhes(id)
                .orElseThrow(() -> new RuntimeException("Atendimento nao encontrado"));
    }

    public Atendimento buscarPorConsulta(Consulta consulta) {
        if (consulta == null || consulta.getId() == null) {
            return null;
        }

        List<Atendimento> atendimentos = repository.findByConsultaIdComDetalhes(consulta.getId());
        if (atendimentos == null || atendimentos.isEmpty()) {
            return null;
        }

        // Se houver multiplos atendimentos para a mesma consulta, retorna o mais recente (ordenado no repo)
        return atendimentos.get(0);
    }

    public Atendimento obterOuCriarPorConsulta(Consulta consulta) {
        Atendimento existente = buscarPorConsulta(consulta);
        if (existente != null) {
            return existente;
        }

        Atendimento atendimento = new Atendimento();
        atendimento.setIdConsulta(consulta);
        atendimento.setDataAtendimento(java.time.LocalDate.now());
        atendimento.setRetorno(false);
        return salvar(atendimento);
    }

    public void excluir(Integer id) {

        Atendimento atendimento = buscarPorId(id);

        repository.delete(atendimento);
    }
}
