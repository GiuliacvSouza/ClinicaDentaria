package bll;

import dal.AtendimentoRepository;
import dal.MaterialRepository;
import dal.MaterialUtilizadoRepository;
import model.Material;
import model.MaterialUtilizado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MaterialUtilizadoService {

    private final MaterialUtilizadoRepository repository;
    private final MaterialRepository materialRepository;
    private final AtendimentoRepository atendimentoRepository;

    public MaterialUtilizadoService(MaterialUtilizadoRepository repository,
                                    MaterialRepository materialRepository,
                                    AtendimentoRepository atendimentoRepository) {
        this.repository = repository;
        this.materialRepository = materialRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    public List<MaterialUtilizado> listarPorAtendimento(Integer atendimentoId) {
        if (atendimentoId == null) {
            return List.of();
        }
        return repository.findByAtendimentoIdComMaterial(atendimentoId);
    }

    @Transactional
    public MaterialUtilizado registar(Integer atendimentoId, Integer materialId, Integer quantidade) {
        if (atendimentoId == null) {
            throw new RuntimeException("Atendimento obrigatorio.");
        }
        if (materialId == null) {
            throw new RuntimeException("Selecione o material.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new RuntimeException("A quantidade deve ser superior a zero.");
        }

        var atendimento = atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new RuntimeException("Atendimento nao encontrado."));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material nao encontrado."));

        int stockAtual = material.getQuantidadeAtual() != null ? material.getQuantidadeAtual() : 0;
        if (stockAtual < quantidade) {
            throw new RuntimeException("Stock insuficiente. Stock atual: " + stockAtual);
        }

        BigDecimal valorUnitario = material.getValorUnitario() != null
                ? material.getValorUnitario()
                : BigDecimal.ZERO;

        MaterialUtilizado item = new MaterialUtilizado();
        item.setAtendimento(atendimento);
        item.setMaterial(material);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorUnitario);
        item.setValorTotal(valorUnitario.multiply(BigDecimal.valueOf(quantidade)));

        material.setQuantidadeAtual(stockAtual - quantidade);
        materialRepository.save(material);

        return repository.save(item);
    }
}
