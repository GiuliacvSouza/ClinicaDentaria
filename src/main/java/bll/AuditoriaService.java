package bll;

import dal.AuditoriaLogRepository;
import model.AuditoriaLog;
import model.Utilizador;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaLogRepository repository;

    public AuditoriaService(AuditoriaLogRepository repository) {
        this.repository = repository;
    }

    public void registar(Utilizador utilizador, String operacao, String descricao) {
        if (utilizador == null || operacao == null || operacao.isBlank()) {
            return;
        }

        AuditoriaLog log = new AuditoriaLog();
        log.setDataHora(LocalDateTime.now());
        String nome = (utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome() : "")
                + " " + (utilizador.getUltimoNome() != null ? utilizador.getUltimoNome() : "");
        log.setUtilizadorNome(nome.trim());
        log.setUtilizadorPerfil(utilizador.getTipoUtilizador() != null ? utilizador.getTipoUtilizador() : "Desconhecido");
        log.setOperacao(operacao.trim().toUpperCase());
        log.setDescricao(descricao != null ? descricao.trim() : "");

        repository.save(log);
    }

    public List<AuditoriaLog> listarTodos() {
        return repository.findAllByOrderByDataHoraDesc();
    }

    public List<AuditoriaLog> listarPorUtilizador(String nomeUtilizador) {
        if (nomeUtilizador == null || nomeUtilizador.isBlank()) {
            return listarTodos();
        }
        return repository.findByUtilizadorNomeContainingIgnoreCase(nomeUtilizador.trim());
    }

    public List<AuditoriaLog> listarPorOperacao(String operacao) {
        if (operacao == null || operacao.isBlank()) {
            return listarTodos();
        }
        return repository.findByOperacaoContainingIgnoreCase(operacao.trim());
    }

    public List<AuditoriaLog> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null && fim == null) {
            return listarTodos();
        }
        if (inicio == null) {
            inicio = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if (fim == null) {
            fim = LocalDateTime.now();
        }
        return repository.findByDataHoraBetween(inicio, fim);
    }
}