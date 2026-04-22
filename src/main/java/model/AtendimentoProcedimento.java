package model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "atendimento_procedimento")
public class AtendimentoProcedimento {

    @EmbeddedId
    private AtendimentoProcedimentoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_procedimento", nullable = false)
    private Procedimento idProcedimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_atendimento", nullable = false)
    private Atendimento idAtendimento;

    @Column(name = "quantidade")
    private Integer quantidade;

    @Column(name = "desconto", precision = 10, scale = 2)
    private BigDecimal desconto;

    private void garantirId() {
        if (id == null) {
            id = new AtendimentoProcedimentoId();
        }

        if (idAtendimento != null && idAtendimento.getId() != null) {
            id.setIdAtendimento(idAtendimento.getId());
        }

        if (idProcedimento != null && idProcedimento.getId() != null) {
            id.setIdProcedimento(idProcedimento.getId());
        }
    }

    @PrePersist
    @PreUpdate
    private void sincronizarChaves() {
        garantirId();
    }

    public AtendimentoProcedimentoId getId() {
        return id;
    }

    public void setId(AtendimentoProcedimentoId id) {
        this.id = id;
        garantirId();
    }

    public Procedimento getIdProcedimento() {
        return idProcedimento;
    }

    public void setIdProcedimento(Procedimento idProcedimento) {
        this.idProcedimento = idProcedimento;
        garantirId();
    }

    public Atendimento getIdAtendimento() {
        return idAtendimento;
    }

    public void setIdAtendimento(Atendimento idAtendimento) {
        this.idAtendimento = idAtendimento;
        garantirId();
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public Procedimento getProcedimento() {
        return idProcedimento;
    }

    public void setProcedimento(Procedimento procedimento) {
        this.idProcedimento = procedimento;
        garantirId();
    }
}
