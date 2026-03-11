package model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "dentista")
public class Dentista {
    @Id
    @Column(name = "idUtilizador", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Utilizador utilizador;

    @Column(name = "numeroOmd", length = 50)
    private String numeroOmd;

    @Column(name = "dataAdmissao")
    private LocalDate dataAdmissao;

    @Column(name = "horarioEntrada")
    private LocalTime horarioEntrada;

    @Column(name = "horarioSaida")
    private LocalTime horarioSaida;

    @Column(name = "ativo")
    private Boolean ativo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public String getNumeroOmd() {
        return numeroOmd;
    }

    public void setNumeroOmd(String numeroOmd) {
        this.numeroOmd = numeroOmd;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public LocalTime getHorarioEntrada() {
        return horarioEntrada;
    }

    public void setHorarioEntrada(LocalTime horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }

    public LocalTime getHorarioSaida() {
        return horarioSaida;
    }

    public void setHorarioSaida(LocalTime horarioSaida) {
        this.horarioSaida = horarioSaida;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

}