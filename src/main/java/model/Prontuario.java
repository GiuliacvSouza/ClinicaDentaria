package model;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDate;

@Entity
@Table(name = "prontuario")
public class Prontuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUtilizador", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Paciente paciente;

    @Column(name = "dataCriacao")
    private LocalDate dataCriacao;

    @Column(name = "ultimaAtualizacao")
    private LocalDate ultimaAtualizacao;

    @Column(name = "grupoSanguineo", length = 5)
    private String grupoSanguineo;

    @Column(name = "historicoMedico", length = Integer.MAX_VALUE)
    private String historicoMedico;

    @Column(name = "alergias", length = Integer.MAX_VALUE)
    private String alergias;

    @Column(name = "medicamentosUso", length = Integer.MAX_VALUE)
    private String medicamentosUso;

    @Column(name = "observacoesClinicas", length = Integer.MAX_VALUE)
    private String observacoesClinicas;

    @Column(name = "historicoOdontologico", length = Integer.MAX_VALUE)
    private String historicoOdontologico;

    @Column(name = "observacoes", length = Integer.MAX_VALUE)
    private String observacoes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public LocalDate getDatacriacao() {
        return dataCriacao;
    }

    public void setDatacriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDate ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public String getHistoricoMedico() {
        return historicoMedico;
    }

    public void setHistoricoMedico(String historicoMedico) {
        this.historicoMedico = historicoMedico;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getMedicamentosUso() {
        return medicamentosUso;
    }

    public void setMedicamentosUso(String medicamentosUso) {
        this.medicamentosUso = medicamentosUso;
    }

    public String getObservacoesClinicas() {
        return observacoesClinicas;
    }

    public void setObservacoesClinicas(String observacoesClinicas) {
        this.observacoesClinicas = observacoesClinicas;
    }

    public String getHistoricoOdontologico() {
        return historicoOdontologico;
    }

    public void setHistoricoOdontologico(String historicoOdontologico) {
        this.historicoOdontologico = historicoOdontologico;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

}
