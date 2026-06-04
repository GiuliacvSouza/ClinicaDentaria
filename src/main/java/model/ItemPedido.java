package model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @EmbeddedId
    private ItemPedidoId id;

    /**
     * insertable=false / updatable=false porque a coluna já é gerida pelo @EmbeddedId.
     * Sem isso o Hibernate gera a coluna duas vezes.
     */
    @MapsId("idMaterial")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_material", nullable = false,
                insertable = false, updatable = false)
    private Material idMaterial;

    @MapsId("idPedido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_pedido", nullable = false,
                insertable = false, updatable = false)
    private PedidoCompra idPedido;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public ItemPedidoId getId()                { return id; }
    public void setId(ItemPedidoId id)         { this.id = id; }

    public Material getIdMaterial()            { return idMaterial; }
    public void setIdMaterial(Material m)      { this.idMaterial = m; }

    public PedidoCompra getIdPedido()          { return idPedido; }
    public void setIdPedido(PedidoCompra p)    { this.idPedido = p; }

    public Integer getQuantidade()             { return quantidade; }
    public void setQuantidade(Integer q)       { this.quantidade = q; }

    public BigDecimal getValor()               { return valor; }
    public void setValor(BigDecimal v)         { this.valor = v; }
}
