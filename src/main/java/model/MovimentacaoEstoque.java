package model;

import jakarta.persistence.*;
import model.enums.TipoMovimentacao;

import java.time.LocalDate;

@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    private Material idMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilizador")
    private Assistente idUtilizador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false, length = 20)
    private TipoMovimentacao tipoMovimentacao;

    @Column(name = "quantidade")
    private Integer quantidade;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "motivo", length = Integer.MAX_VALUE)
    private String motivo;

    @Column(name = "observacao", length = Integer.MAX_VALUE)
    private String observacao;

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Integer getId()                              { return id; }
    public void setId(Integer id)                       { this.id = id; }

    public Material getIdMaterial()                     { return idMaterial; }
    public void setIdMaterial(Material m)               { this.idMaterial = m; }

    public Assistente getIdUtilizador()                 { return idUtilizador; }
    public void setIdUtilizador(Assistente a)           { this.idUtilizador = a; }

    public TipoMovimentacao getTipoMovimentacao()       { return tipoMovimentacao; }
    public void setTipoMovimentacao(TipoMovimentacao t) { this.tipoMovimentacao = t; }

    public Integer getQuantidade()                      { return quantidade; }
    public void setQuantidade(Integer q)                { this.quantidade = q; }

    public LocalDate getData()                          { return data; }
    public void setData(LocalDate d)                    { this.data = d; }

    public String getMotivo()                           { return motivo; }
    public void setMotivo(String m)                     { this.motivo = m; }

    public String getObservacao()                       { return observacao; }
    public void setObservacao(String o)                 { this.observacao = o; }
}
