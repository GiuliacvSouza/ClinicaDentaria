package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EspecialidadexAssistenteId implements Serializable {
    private static final long serialVersionUID = 7462183055867298465L;
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
        EspecialidadexAssistenteId entity = (EspecialidadexAssistenteId) o;
        return Objects.equals(this.idUtilizador, entity.idUtilizador) &&
                Objects.equals(this.idEspecialidade, entity.idEspecialidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtilizador, idEspecialidade);
    }
}