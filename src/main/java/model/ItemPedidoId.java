package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ItemPedidoId implements Serializable {

    private static final long serialVersionUID = 9148983576092388547L;

    @Column(name = "id_material", nullable = false)
    private Integer idMaterial;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    public ItemPedidoId() {}

    public ItemPedidoId(Integer idMaterial, Integer idPedido) {
        this.idMaterial = idMaterial;
        this.idPedido   = idPedido;
    }

    public Integer getIdMaterial()          { return idMaterial; }
    public void setIdMaterial(Integer v)    { this.idMaterial = v; }

    public Integer getIdPedido()            { return idPedido; }
    public void setIdPedido(Integer v)      { this.idPedido = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPedidoId that)) return false;
        return Objects.equals(idMaterial, that.idMaterial)
                && Objects.equals(idPedido, that.idPedido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMaterial, idPedido);
    }
}
