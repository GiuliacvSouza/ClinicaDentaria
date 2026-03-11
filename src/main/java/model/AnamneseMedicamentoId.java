package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AnamneseMedicamentoId implements Serializable {
    private static final long serialVersionUID = -3797682857376391926L;
    @Column(name = "idAnamnese", nullable = false)
    private Integer idAnamnese;

    @Column(name = "idMedicamento", nullable = false)
    private Integer idMedicamento;

    public Integer getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Integer idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Integer getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(Integer idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnamneseMedicamentoId entity = (AnamneseMedicamentoId) o;
        return Objects.equals(this.idAnamnese, entity.idAnamnese) &&
                Objects.equals(this.idMedicamento, entity.idMedicamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnamnese, idMedicamento);
    }
}