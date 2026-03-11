package model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "atendimentoProcedimento")
public class AtendimentoProcedimento {
    @EmbeddedId
    private AtendimentoProcedimentoId id;

    @MapsId("idProcedimento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProcedimento", nullable = false)
    private Procedimento idProcedimento;

    @MapsId("idAtendimento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idAtendimento", nullable = false)
    private Atendimento idAtendimento;

    @Column(name = "quantidade")
    private Integer quantidade;

    @Column(name = "desconto", precision = 10, scale = 2)
    private BigDecimal desconto;

    public AtendimentoProcedimentoId getId() {
        return id;
    }

    public void setId(AtendimentoProcedimentoId id) {
        this.id = id;
    }

    public Procedimento getIdProcedimento() {
        return idProcedimento;
    }

    public void setIdProcedimento(Procedimento idProcedimento) {
        this.idProcedimento = idProcedimento;
    }

    public Atendimento getIdAtendimento() {
        return idAtendimento;
    }

    public void setIdAtendimento(Atendimento idAtendimento) {
        this.idAtendimento = idAtendimento;
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

}