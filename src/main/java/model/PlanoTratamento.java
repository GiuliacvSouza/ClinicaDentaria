package model;

import jakarta.persistence.*;
import model.enums.EstadoPlanoTratamento;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "plano_tratamento")
public class PlanoTratamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPlanoTratamento", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDentista")
    private Dentista dentista;

    @Column(name = "objetivo", length = Integer.MAX_VALUE)
    private String objetivo;

    @Column(name = "etapas", length = Integer.MAX_VALUE)
    private String etapas;

    @Column(name = "procedimentosPrevistos", length = Integer.MAX_VALUE)
    private String procedimentosPrevistos;

    @Column(name = "valorEstimado", precision = 10, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "dataPrevistaInicio")
    private LocalDate dataPrevistaInicio;

    @Column(name = "dataPrevistaFim")
    private LocalDate dataPrevistaFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30)
    private EstadoPlanoTratamento estado;

    @Column(name = "progresso", length = Integer.MAX_VALUE)
    private String progresso;

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

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getEtapas() {
        return etapas;
    }

    public void setEtapas(String etapas) {
        this.etapas = etapas;
    }

    public String getProcedimentosPrevistos() {
        return procedimentosPrevistos;
    }

    public void setProcedimentosPrevistos(String procedimentosPrevistos) {
        this.procedimentosPrevistos = procedimentosPrevistos;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public LocalDate getDataPrevistaInicio() {
        return dataPrevistaInicio;
    }

    public void setDataPrevistaInicio(LocalDate dataPrevistaInicio) {
        this.dataPrevistaInicio = dataPrevistaInicio;
    }

    public LocalDate getDataPrevistaFim() {
        return dataPrevistaFim;
    }

    public void setDataPrevistaFim(LocalDate dataPrevistaFim) {
        this.dataPrevistaFim = dataPrevistaFim;
    }

    public EstadoPlanoTratamento getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanoTratamento estado) {
        this.estado = estado;
    }

    public String getProgresso() {
        return progresso;
    }

    public void setProgresso(String progresso) {
        this.progresso = progresso;
    }
}
