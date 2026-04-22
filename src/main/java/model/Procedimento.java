package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;

@Entity
@Table(name = "procedimento")
public class Procedimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProcedimento", nullable = false)
    private Integer id;

    @Column(name = "nome", length = 100)
    private String nome;

    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "duracaoEstimada")
    private Integer duracaoEstimada;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "taxa_iva", precision = 5, scale = 2)
    private BigDecimal taxaIva;

    public BigDecimal getTaxaIva() { return taxaIva; }
    public void setTaxaIva(BigDecimal taxaIva) { this.taxaIva = taxaIva; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getDuracaoEstimada() {
        return duracaoEstimada;
    }

    public void setDuracaoEstimada(Integer duracaoEstimada) {
        this.duracaoEstimada = duracaoEstimada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}