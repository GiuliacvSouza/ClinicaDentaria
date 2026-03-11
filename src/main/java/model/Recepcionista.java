package model;

import jakarta.persistence.*;
import model.enums.Turno;

import java.time.LocalDate;

@Entity
@Table(name = "recepcionista")
public class Recepcionista {
    @Id
    @Column(name = "idUtilizador", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Utilizador utilizador;

    @Column(name = "dataAdmissao")
    private LocalDate dataAdmissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno", length = 50)
    private Turno turno;

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

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataadmissao) {
        this.dataAdmissao = dataadmissao;
    }

    public Turno getTurno() { return turno; }

    public void setTurno(Turno turno) {this.turno = turno;}
}