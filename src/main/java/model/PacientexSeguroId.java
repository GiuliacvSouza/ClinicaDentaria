package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PacientexSeguroId implements Serializable {
    private static final long serialVersionUID = 5454738706395719972L;
    @Column(name = "idUtilizador", nullable = false)
    private Integer idUtilizador;

    @Column(name = "idSeguro", nullable = false)
    private Integer idSeguro;

    public Integer getIdUtilizador() {
        return idUtilizador;
    }

    public void setIdUtilizador(Integer idUtilizador) {
        this.idUtilizador = idUtilizador;
    }

    public Integer getIdSeguro() {
        return idSeguro;
    }

    public void setIdSeguro(Integer idSeguro) {
        this.idSeguro = idSeguro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacientexSeguroId entity = (PacientexSeguroId) o;
        return Objects.equals(this.idUtilizador, entity.idUtilizador) &&
                Objects.equals(this.idSeguro, entity.idSeguro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtilizador, idSeguro);
    }
}