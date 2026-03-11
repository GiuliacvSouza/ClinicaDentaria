package model;

import jakarta.persistence.*;
import model.enums.NivelFormacao;

import java.time.LocalDate;

@Entity
@Table(name = "assistente")
public class Assistente {
    @Id
    @Column(name = "idUtilizador", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Utilizador utilizador;

    @Enumerated(EnumType.STRING)
    private NivelFormacao nivelFormacao;

    @Column(name = "dataAdmissao")
    private LocalDate dataAdmissao;

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

    public NivelFormacao getNivelformacao() {
        return nivelFormacao;
    }

    public void setNivelFormacao(NivelFormacao nivelFormacao) {
        this.nivelFormacao = nivelFormacao;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

}