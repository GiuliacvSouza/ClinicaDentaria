package model;

import jakarta.persistence.*;
import model.enums.EstadoPedidoCompra;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido_compra")
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor idFornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assistente")
    private Assistente idAssistente;

    @Column(name = "data_pedido")
    private LocalDate dataPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPedidoCompra estado = EstadoPedidoCompra.PENDENTE;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @OneToMany(mappedBy = "idPedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> itens = new ArrayList<>();

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Integer getId()                          { return id; }
    public void setId(Integer id)                   { this.id = id; }

    public Fornecedor getIdFornecedor()             { return idFornecedor; }
    public void setIdFornecedor(Fornecedor f)       { this.idFornecedor = f; }

    public Assistente getIdAssistente()             { return idAssistente; }
    public void setIdAssistente(Assistente a)       { this.idAssistente = a; }

    public LocalDate getDataPedido()                { return dataPedido; }
    public void setDataPedido(LocalDate d)          { this.dataPedido = d; }

    public EstadoPedidoCompra getEstado()           { return estado; }
    public void setEstado(EstadoPedidoCompra e)     { this.estado = e; }

    public String getObservacoes()                  { return observacoes; }
    public void setObservacoes(String o)            { this.observacoes = o; }

    public List<ItemPedido> getItens()              { return itens; }
    public void setItens(List<ItemPedido> i)        { this.itens = i; }
}
