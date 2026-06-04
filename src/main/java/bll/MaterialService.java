package bll;

import dal.MaterialRepository;
import model.Material;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository repository;

    public MaterialService(MaterialRepository repository) {
        this.repository = repository;
    }

    public Material criarMaterial(Material material) {
        validarMaterial(material);
        normalizar(material);
        return repository.save(material);
    }

    public Material salvar(Material material) {
        validarMaterial(material);
        normalizar(material);
        return repository.save(material);
    }

    public List<Material> listarTodos() {
        return repository.findAll();
    }

    public Material buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado."));
    }

    public Material atualizarEstoque(Integer id, Integer novaQuantidade) {
        Material material = buscarPorId(id);
        material.setQuantidadeAtual(novaQuantidade);
        return repository.save(material);
    }

    private void validarMaterial(Material material) {
        if (material.getCodigoInterno() == null || material.getCodigoInterno().isBlank()) {
            throw new RuntimeException("O código interno é obrigatório.");
        }

        String codigoInterno = material.getCodigoInterno().trim();
        boolean codigoDuplicado = material.getId() == null
                ? repository.existsByCodigoInternoIgnoreCase(codigoInterno)
                : repository.existsByCodigoInternoIgnoreCaseAndIdNot(codigoInterno, material.getId());

        if (codigoDuplicado) {
            throw new RuntimeException("Já existe um material com este código interno.");
        }

        if (material.getNome() == null || material.getNome().isBlank()) {
            throw new RuntimeException("O nome do material é obrigatório.");
        }

        if (material.getIdFornecedor() == null) {
            throw new RuntimeException("Selecione um fornecedor.");
        }

        if (material.getUnidadeMedida() == null || material.getUnidadeMedida().isBlank()) {
            throw new RuntimeException("A unidade de medida é obrigatória.");
        }

        if (material.getQuantidadeAtual() != null && material.getQuantidadeAtual() < 0) {
            throw new RuntimeException("A quantidade atual não pode ser negativa.");
        }

        if (material.getQuantidadeMinima() != null && material.getQuantidadeMinima() < 0) {
            throw new RuntimeException("A quantidade mínima não pode ser negativa.");
        }

        if (material.getValorUnitario() != null
                && material.getValorUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("O valor unitário não pode ser negativo.");
        }
    }

    private void normalizar(Material material) {
        material.setCodigoInterno(material.getCodigoInterno().trim());
        material.setNome(material.getNome().trim());
        material.setUnidadeMedida(material.getUnidadeMedida().trim());
        material.setDescricao(material.getDescricao() == null || material.getDescricao().isBlank()
                ? null
                : material.getDescricao().trim());

        if (material.getQuantidadeAtual() == null) {
            material.setQuantidadeAtual(0);
        }
        if (material.getQuantidadeMinima() == null) {
            material.setQuantidadeMinima(0);
        }
        if (material.getValorUnitario() == null) {
            material.setValorUnitario(BigDecimal.ZERO);
        }
        if (material.getAtivo() == null) {
            material.setAtivo(true);
        }
    }
}
