package model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "pacientexseguro")
public class PacientexSeguro {
    @EmbeddedId
    private PacientexSeguroId id;

    @MapsId("idUtilizador")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Paciente idUtilizador;

    @MapsId("idSeguro")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idSeguro", nullable = false)
    private Seguro idSeguro;

    @Column(name = "numeroApolice", length = 50)
    private String numeroApolice;

    @Column(name = "dataInicioCobertura")
    private LocalDate dataInicioCobertura;

    @Column(name = "dataFimCobertura")
    private LocalDate dataFimCobertura;

    public PacientexSeguroId getId() {
        return id;
    }

    public void setId(PacientexSeguroId id) {
        this.id = id;
    }

    public Paciente getIdUtilizador() {
        return idUtilizador;
    }

    public void setIdUtilizador(Paciente idUtilizador) {
        this.idUtilizador = idUtilizador;
    }

    public Seguro getIdSeguro() {
        return idSeguro;
    }

    public void setIdSeguro(Seguro idSeguro) {
        this.idSeguro = idSeguro;
    }

    public String getNumeroApolice() {
        return numeroApolice;
    }

    public void setNumeroApolice(String numeroApolice) {
        this.numeroApolice = numeroApolice;
    }

    public LocalDate getDataInicioCobertura() {
        return dataInicioCobertura;
    }

    public void setDataInicioCobertura(LocalDate dataInicioCobertura) {
        this.dataInicioCobertura = dataInicioCobertura;
    }

    public LocalDate getDataFimCobertura() {
        return dataFimCobertura;
    }

    public void setDataFimCobertura(LocalDate dataFimCobertura) {
        this.dataFimCobertura = dataFimCobertura;
    }
}