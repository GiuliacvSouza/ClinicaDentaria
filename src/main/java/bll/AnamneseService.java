package bll;

import dal.AnamneseRepository;
import model.Anamnese;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;

    public AnamneseService(AnamneseRepository anamneseRepository) {
        this.anamneseRepository = anamneseRepository;
    }

    public List<Anamnese> listarTodas() {
        return anamneseRepository.findAll();
    }

    public Optional<Anamnese> buscarPorId(Integer id) {
        return anamneseRepository.findById(id);
    }

    public Optional<Anamnese> buscarPorAtendimento(Integer idAtendimento) {
        if (idAtendimento == null) {
            return Optional.empty();
        }
        return anamneseRepository.findByIdAtendimento_Id(idAtendimento);
    }

    public Anamnese salvar(Anamnese anamnese) {

        if (anamnese.getIdAtendimento() == null)
            throw new RuntimeException("Anamnese deve estar associada a um atendimento.");

        if (anamnese.getData() == null)
            throw new RuntimeException("Data da anamnese é obrigatória.");

        if (anamnese.getData().isAfter(LocalDate.now()))
            throw new RuntimeException("Data da anamnese não pode ser futura.");

        if (anamnese.getMotivo() == null || anamnese.getMotivo().isBlank())
            throw new RuntimeException("Motivo da anamnese é obrigatório.");

        return anamneseRepository.save(anamnese);
    }

    public void deletar(Integer id) {

        if (!anamneseRepository.existsById(id))
            throw new RuntimeException("Anamnese não encontrada.");

        anamneseRepository.deleteById(id);
    }
}
