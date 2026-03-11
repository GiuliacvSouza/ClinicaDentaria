package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AnamneseAlergiaId implements Serializable {
    private static final long serialVersionUID = -6904347169111923944L;
    @Column(name = "idAnamnese", nullable = false)
    private Integer idAnamnese;

    @Column(name = "idAlergia", nullable = false)
    private Integer idAlergia;

    public Integer getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Integer idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Integer getIdAlergia() {
        return idAlergia;
    }

    public void setIdAlergia(Integer idAlergia) {
        this.idAlergia = idAlergia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnamneseAlergiaId entity = (AnamneseAlergiaId) o;
        return Objects.equals(this.idAnamnese, entity.idAnamnese) &&
                Objects.equals(this.idAlergia, entity.idAlergia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnamnese, idAlergia);
    }
}