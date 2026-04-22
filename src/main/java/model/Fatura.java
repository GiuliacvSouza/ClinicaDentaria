package model;

import jakarta.persistence.*;
import model.enums.EstadoFatura;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fatura")
public class Fatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFatura", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAtendimento")
    private Atendimento idAtendimento;

    @Column(name = "dataEmissao")
    private LocalDate dataEmissao;

    @Column(name = "valor_final", precision = 10, scale = 2)
    @Generated(GenerationTime.INSERT)
    private BigDecimal valorFinal;

    @Column(name = "valor_base", nullable = false)
    private BigDecimal valorBase;

    @Column(name = "taxa_iva", nullable = false)
    private BigDecimal taxaIva = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstadoFatura estado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Atendimento getIdAtendimento() {
        return idAtendimento;
    }

    public void setIdAtendimento(Atendimento idAtendimento) {
        this.idAtendimento = idAtendimento;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public EstadoFatura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFatura estado) {
        this.estado = estado;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    public BigDecimal getTaxaIva() {
        return taxaIva;
    }

    public void setTaxaIva(BigDecimal taxaIva) {
        this.taxaIva = taxaIva;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public BigDecimal getValorFinal() {
        if (valorFinal == null && valorBase != null && taxaIva != null) {
            return valorBase.multiply(
                    BigDecimal.ONE.add(taxaIva.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
            ).setScale(2, RoundingMode.HALF_UP);
        }
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public void setAtendimento(Atendimento atendimento) {
        this.idAtendimento = atendimento;
    }

}
