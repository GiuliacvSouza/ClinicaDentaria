package bll;

import dal.ItemPedidoRepository;
import dal.MaterialRepository;
import dal.MovimentacaoEstoqueRepository;
import dal.PedidoCompraRepository;
import model.Assistente;
import model.ItemPedido;
import model.ItemPedidoId;
import model.Material;
import model.MovimentacaoEstoque;
import model.PedidoCompra;
import model.enums.EstadoPedidoCompra;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoRepo;
    private final ItemPedidoRepository   itemRepo;
    private final MaterialRepository     materialRepo;
    private final MovimentacaoEstoqueRepository movRepo;

    public PedidoCompraService(PedidoCompraRepository pedidoRepo,
                               ItemPedidoRepository itemRepo,
                               MaterialRepository materialRepo,
                               MovimentacaoEstoqueRepository movRepo) {
        this.pedidoRepo   = pedidoRepo;
        this.itemRepo     = itemRepo;
        this.materialRepo = materialRepo;
        this.movRepo      = movRepo;
    }

    // ─── Listagem ─────────────────────────────────────────────────────────────

    public List<PedidoCompra> listarTodos() {
        return pedidoRepo.findAllOrderByDataDesc();
    }

    public List<PedidoCompra> listarPorEstado(EstadoPedidoCompra estado) {
        return pedidoRepo.findByEstado(estado);
    }

    public PedidoCompra buscarPorId(Integer id) {
        return pedidoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido de compra não encontrado."));
    }

    public List<ItemPedido> listarItensDoPedido(Integer idPedido) {
        return itemRepo.findByIdPedido_Id(idPedido);
    }

    // ─── Criação ──────────────────────────────────────────────────────────────

    @Transactional
    public PedidoCompra criarPedido(PedidoCompra pedido, List<ItemPedido> itens) {
        if (pedido.getIdFornecedor() == null)
            throw new RuntimeException("Fornecedor é obrigatório.");
        if (pedido.getIdAssistente() == null)
            throw new RuntimeException("Assistente responsável é obrigatório.");
        if (itens == null || itens.isEmpty())
            throw new RuntimeException("O pedido deve ter pelo menos um item.");

        validarItens(itens);

        pedido.setEstado(EstadoPedidoCompra.PENDENTE);
        pedido.setDataPedido(LocalDate.now());

        PedidoCompra salvo = pedidoRepo.save(pedido);

        for (ItemPedido item : itens) {
            ItemPedidoId itemId = new ItemPedidoId(
                    item.getIdMaterial().getId(), salvo.getId());
            item.setId(itemId);
            item.setIdPedido(salvo);
            itemRepo.save(item);
        }

        return salvo;
    }

    private void validarItens(List<ItemPedido> itens) {
        for (int i = 0; i < itens.size(); i++) {
            ItemPedido item = itens.get(i);
            if (item.getIdMaterial() == null)
                throw new RuntimeException("Item " + (i + 1) + ": material é obrigatório.");
            if (item.getQuantidade() == null || item.getQuantidade() <= 0)
                throw new RuntimeException("Item " + (i + 1) + ": quantidade deve ser maior que zero.");
            if (item.getValor() == null || item.getValor().compareTo(BigDecimal.ZERO) < 0)
                throw new RuntimeException("Item " + (i + 1) + ": valor não pode ser negativo.");
        }

        long distinct = itens.stream()
                .map(i -> i.getIdMaterial().getId())
                .distinct().count();
        if (distinct < itens.size())
            throw new RuntimeException("Não é permitido repetir o mesmo material no mesmo pedido.");
    }

    // ─── Cancelar ─────────────────────────────────────────────────────────────

    @Transactional
    public PedidoCompra cancelarPedido(Integer id) {
        PedidoCompra pedido = buscarPorId(id);
        if (pedido.getEstado() == EstadoPedidoCompra.RECEBIDO)
            throw new RuntimeException("Não é possível cancelar um pedido já recebido.");
        if (pedido.getEstado() == EstadoPedidoCompra.CANCELADO)
            throw new RuntimeException("Este pedido já se encontra cancelado.");
        pedido.setEstado(EstadoPedidoCompra.CANCELADO);
        return pedidoRepo.save(pedido);
    }

    // ─── Marcar como recebido ─────────────────────────────────────────────────

    @Transactional
    public PedidoCompra marcarComoRecebido(Integer id) {
        PedidoCompra pedido = buscarPorId(id);
        if (pedido.getEstado() == EstadoPedidoCompra.CANCELADO)
            throw new RuntimeException("Não é possível receber um pedido cancelado.");
        if (pedido.getEstado() == EstadoPedidoCompra.RECEBIDO)
            throw new RuntimeException("Este pedido já foi recebido.");

        pedido.setEstado(EstadoPedidoCompra.RECEBIDO);
        pedidoRepo.save(pedido);

        // Atualizar stock e registar movimentação por cada item
        List<ItemPedido> itens = itemRepo.findByIdPedido_Id(id);
        Assistente assistente  = pedido.getIdAssistente();

        for (ItemPedido item : itens) {
            Material material = item.getIdMaterial();
            if (material == null) continue;

            int atual = material.getQuantidadeAtual() != null ? material.getQuantidadeAtual() : 0;
            material.setQuantidadeAtual(atual + item.getQuantidade());
            materialRepo.save(material);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setIdMaterial(material);
            mov.setIdUtilizador(assistente);
            mov.setQuantidade(item.getQuantidade());
            mov.setData(LocalDate.now());
            mov.setMotivo("Receção do pedido de compra nº " + id);
            mov.setObservacao("Entrada de stock por pedido de compra");
            movRepo.save(mov);
        }

        return pedido;
    }

    // ─── Cálculo de total ─────────────────────────────────────────────────────

    public BigDecimal calcularTotal(List<ItemPedido> itens) {
        if (itens == null) return BigDecimal.ZERO;
        return itens.stream()
                .filter(i -> i.getValor() != null && i.getQuantidade() != null)
                .map(i -> i.getValor().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
