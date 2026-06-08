package model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "prescricao")
public class Prescricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPrescricao", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDentista")
    private Dentista dentista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idConsulta")
    private Consulta consulta;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "medicamento", length = 150)
    private String medicamento;

    @Column(name = "posologia", length = Integer.MAX_VALUE)
    private String posologia;

    @Column(name = "tempoTratamento", length = 100)
    private String tempoTratamento;

    @Column(name = "observacoes", length = Integer.MAX_VALUE)
    private String observacoes;

    @Column(name = "assinatura", length = 150)
    private String assinatura;

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

    public Dentista getDentista() {
        return dentista;
    }

    public void setDentista(Dentista dentista) {
        this.dentista = dentista;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public String getPosologia() {
        return posologia;
    }

    public void setPosologia(String posologia) {
        this.posologia = posologia;
    }

    public String getTempoTratamento() {
        return tempoTratamento;
    }

    public void setTempoTratamento(String tempoTratamento) {
        this.tempoTratamento = tempoTratamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getAssinatura() {
        return assinatura;
    }

    public void setAssinatura(String assinatura) {
        this.assinatura = assinatura;
    }
}
