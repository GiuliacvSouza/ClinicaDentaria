package model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "anamnese")
public class Anamnese {
    @Id
    @Column(name = "idAnamnese", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAtendimento")
    private Atendimento idAtendimento;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "motivo", length = Integer.MAX_VALUE)
    private String motivo;

    @Column(name = "queixaPrincipal", length = Integer.MAX_VALUE)
    private String queixaPrincipal;

    @Column(name = "diabetes")
    private Boolean diabetes;

    @Column(name = "hipertensao")
    private Boolean hipertensao;

    @Column(name = "doencagrave")
    private Boolean doencagrave;

    @Column(name = "hepatite")
    private Boolean hepatite;

    @Column(name = "outrasDoencas", length = Integer.MAX_VALUE)
    private String outrasDoencas;

    @Column(name = "usaMedicamento")
    private Boolean usaMedicamento;

    @Column(name = "temAlergia")
    private Boolean temAlergia;

    @Column(name = "eFumante")
    private Boolean eFumante;

    @Column(name = "observacoes", length = Integer.MAX_VALUE)
    private String observacoes;

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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getQueixaPrincipal() {
        return queixaPrincipal;
    }

    public void setQueixaPrincipal(String queixaPrincipal) {
        this.queixaPrincipal = queixaPrincipal;
    }

    public Boolean getDiabetes() {
        return diabetes;
    }

    public void setDiabetes(Boolean diabetes) {
        this.diabetes = diabetes;
    }

    public Boolean getHipertensao() {
        return hipertensao;
    }

    public void setHipertensao(Boolean hipertensao) {
        this.hipertensao = hipertensao;
    }

    public Boolean getDoencagrave() {
        return doencagrave;
    }

    public void setDoencagrave(Boolean doencagrave) {
        this.doencagrave = doencagrave;
    }

    public Boolean getHepatite() {
        return hepatite;
    }

    public void setHepatite(Boolean hepatite) {
        this.hepatite = hepatite;
    }

    public String getOutrasDoencas() {
        return outrasDoencas;
    }

    public void setOutrasDoencas(String outrasDoencas) {
        this.outrasDoencas = outrasDoencas;
    }

    public Boolean getUsaMedicamento() {
        return usaMedicamento;
    }

    public void setUsaMedicamento(Boolean usaMedicamento) {
        this.usaMedicamento = usaMedicamento;
    }

    public Boolean getTemAlergia() {
        return temAlergia;
    }

    public void setTemAlergia(Boolean temAlergia) {
        this.temAlergia = temAlergia;
    }

    public Boolean geteFumante() {
        return eFumante;
    }

    public void seteFumante(Boolean eFumante) {
        this.eFumante = eFumante;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}