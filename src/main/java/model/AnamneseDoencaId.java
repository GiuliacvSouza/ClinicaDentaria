package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AnamneseDoencaId implements Serializable {
    private static final long serialVersionUID = 8077075600272446396L;
    @Column(name = "idAnamnese", nullable = false)
    private Integer idAnamnese;

    @Column(name = "idDoenca", nullable = false)
    private Integer idDoenca;

    public Integer getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Integer idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Integer getIdDoenca() {
        return idDoenca;
    }

    public void setIdDoenca(Integer idDoenca) {
        this.idDoenca = idDoenca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnamneseDoencaId entity = (AnamneseDoencaId) o;
        return Objects.equals(this.idAnamnese, entity.idAnamnese) &&
                Objects.equals(this.idDoenca, entity.idDoenca);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnamnese, idDoenca);
    }
}