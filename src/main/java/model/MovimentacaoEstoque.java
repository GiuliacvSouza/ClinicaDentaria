package model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "movimentacaoEstoque")
public class MovimentacaoEstoque {
    @Id
    @Column(name = "idMovimentacao", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMaterial")
    private Material idMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtilizador")
    private Assistente idUtilizador;

    @Column(name = "quantidade")
    private Integer quantidade;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "motivo", length = Integer.MAX_VALUE)
    private String motivo;

    @Column(name = "observacao", length = Integer.MAX_VALUE)
    private String observacao;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Material getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Material idMaterial) {
        this.idMaterial = idMaterial;
    }

    public Assistente getIdUtilizador() {
        return idUtilizador;
    }

    public void setIdUtilizador(Assistente idUtilizador) {
        this.idUtilizador = idUtilizador;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}