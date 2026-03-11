package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AtendimentoProcedimentoId implements Serializable {
    private static final long serialVersionUID = 8035184220406930580L;
    @Column(name = "idProcedimento", nullable = false)
    private Integer idProcedimento;

    @Column(name = "idAtendimento", nullable = false)
    private Integer idAtendimento;

    public Integer getIdProcedimento() {
        return idProcedimento;
    }

    public void setIdProcedimento(Integer idProcedimento) {
        this.idProcedimento = idProcedimento;
    }

    public Integer getIdAtendimento() {
        return idAtendimento;
    }

    public void setIdAtendimento(Integer idAtendimento) {
        this.idAtendimento = idAtendimento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtendimentoProcedimentoId entity = (AtendimentoProcedimentoId) o;
        return Objects.equals(this.idProcedimento, entity.idProcedimento) &&
                Objects.equals(this.idAtendimento, entity.idAtendimento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProcedimento, idAtendimento);
    }
}