package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EspecialidadeDentistaId implements Serializable {
    private static final long serialVersionUID = 7716173453720548128L;
    @Column(name = "idUtilizador", nullable = false)
    private Integer idUtilizador;

    @Column(name = "idEspecialidade", nullable = false)
    private Integer idEspecialidade;

    public Integer getIdUtilizador() {
        return idUtilizador;
    }

    public void setIdUtilizador(Integer idUtilizador) {
        this.idUtilizador = idUtilizador;
    }

    public Integer getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Integer idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EspecialidadeDentistaId entity = (EspecialidadeDentistaId) o;
        return Objects.equals(this.idUtilizador, entity.idUtilizador) &&
                Objects.equals(this.idEspecialidade, entity.idEspecialidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtilizador, idEspecialidade);
    }
}