package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ItemPedidoId implements Serializable {
    private static final long serialVersionUID = 9148983576092388547L;
    @Column(name = "idMaterial", nullable = false)
    private Integer idMaterial;

    @Column(name = "idPedido", nullable = false)
    private Integer idPedido;

    public Integer getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Integer idMaterial) {
        this.idMaterial = idMaterial;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedidoId entity = (ItemPedidoId) o;
        return Objects.equals(this.idMaterial, entity.idMaterial) &&
                Objects.equals(this.idPedido, entity.idPedido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMaterial, idPedido);
    }
}