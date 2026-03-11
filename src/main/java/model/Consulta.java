package model;

import jakarta.persistence.*;
import model.enums.EstadoConsulta;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "consulta")
public class Consulta {
    @Id
    @Column(name = "idConsulta", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente")
    private Paciente idPaciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDentista")
    private Dentista idDentista;

    @Column(name = "dataHoraInicio")
    private Instant dataHoraInicio;

    @Column(name = "duracao")
    private Integer duracao;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Enumerated(EnumType.STRING)
    private EstadoConsulta status;

    @Column(name = "observacoes", length = Integer.MAX_VALUE)
    private String observacoes;

    @Column(name = "dataMarcacao")
    private LocalDate dataMarcacao;

    @Column(name = "motivoCancelamento", length = Integer.MAX_VALUE)
    private String motivoCancelamento;

    @Column(name = "dataCancelamento")
    private LocalDate dataCancelamento;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Paciente getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Paciente idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Dentista getIdDentista() {
        return idDentista;
    }

    public void setIdDentista(Dentista idDentista) {
        this.idDentista = idDentista;
    }

    public Instant getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(Instant dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public EstadoConsulta getStatus() {
        return status;
    }

    public void setStatus(EstadoConsulta status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getDataMarcacao() {
        return dataMarcacao;
    }

    public void setDataMarcacao(LocalDate dataMarcacao) {
        this.dataMarcacao = dataMarcacao;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public LocalDate getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDate dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }
}