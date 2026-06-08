package bll;

import dal.DentistaIndisponibilidadeRepository;
import model.Consulta;
import model.Dentista;
import model.DentistaIndisponibilidade;
import model.enums.TipoIndisponibilidade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DentistaIndisponibilidadeService {

    private final DentistaIndisponibilidadeRepository repository;

    public DentistaIndisponibilidadeService(DentistaIndisponibilidadeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DentistaIndisponibilidade criar(DentistaIndisponibilidade item) {
        if (item.getDentista() == null || item.getDentista().getId() == null) {
            throw new RuntimeException("Dentista é obrigatório.");
        }
        if (item.getTipo() == null) {
            throw new RuntimeException("Tipo de indisponibilidade é obrigatório.");
        }
        if (item.getDataInicio() == null) {
            throw new RuntimeException("Data de início é obrigatória.");
        }
        if (item.getAtiva() == null) {
            item.setAtiva(true);
        }

        switch (item.getTipo()) {
            case DIA_COMPLETO:
                item.setDataFim(item.getDataInicio());
                item.setHoraInicio(null);
                item.setHoraFim(null);
                break;
            case INTERVALO_HORAS:
                item.setDataFim(item.getDataInicio());
                if (item.getHoraInicio() == null || item.getHoraFim() == null) {
                    throw new RuntimeException("Intervalo de horas requer hora início e hora fim.");
                }
                if (!item.getHoraFim().isAfter(item.getHoraInicio())) {
                    throw new RuntimeException("Hora fim deve ser após hora início.");
                }
                break;
            case PERIODO:
                if (item.getDataFim() == null) {
                    throw new RuntimeException("Período requer data fim.");
                }
                if (item.getDataFim().isBefore(item.getDataInicio())) {
                    throw new RuntimeException("Data fim não pode ser anterior à data início.");
                }
                item.setHoraInicio(null);
                item.setHoraFim(null);
                break;
        }

        return repository.save(item);
    }

    public List<DentistaIndisponibilidade> listarPorDentista(Integer dentistaId) {
        return repository.findByDentistaIdAndAtivaTrueOrderByDataInicioAsc(dentistaId);
    }

    @Transactional
    public void cancelar(Integer id) {
        DentistaIndisponibilidade item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indisponibilidade não encontrada."));
        item.setAtiva(false);
        repository.save(item);
    }

    public boolean verificarConflito(Consulta consulta) {
        if (consulta == null || consulta.getIdDentista() == null || consulta.getDataHoraInicio() == null) {
            return false;
        }
        Integer dentistaId = consulta.getIdDentista().getId();
        LocalDate data = consulta.getDataHoraInicio().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalTime hora = consulta.getDataHoraInicio().atZone(java.time.ZoneId.systemDefault()).toLocalTime();

        List<DentistaIndisponibilidade> indisponibilidades = repository
                .findByDentistaIdAndAtivaTrueOrderByDataInicioAsc(dentistaId);

        for (DentistaIndisponibilidade ind : indisponibilidades) {
            switch (ind.getTipo()) {
                case DIA_COMPLETO:
                    if (data.equals(ind.getDataInicio())) return true;
                    break;
                case INTERVALO_HORAS:
                    if (data.equals(ind.getDataInicio())
                            && !hora.isBefore(ind.getHoraInicio())
                            && !hora.isAfter(ind.getHoraFim())) return true;
                    break;
                case PERIODO:
                    if (!data.isBefore(ind.getDataInicio()) && !data.isAfter(ind.getDataFim())) return true;
                    break;
            }
        }
        return false;
    }
}