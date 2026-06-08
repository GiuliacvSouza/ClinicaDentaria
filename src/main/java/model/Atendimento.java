package model;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atendimento")
public class Atendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAtendimento", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idConsulta")
    private Consulta idConsulta;

    @Column(name = "diagnostico", length = Integer.MAX_VALUE)
    private String diagnostico;

    @Column(name = "dataAtendimento")
    private LocalDate dataAtendimento;

    @Column(name = "procedimentosRealizados", length = Integer.MAX_VALUE)
    private String procedimentosRealizados;

    @Column(name = "assinaturaDentista", length = 150)
    private String assinaturaDentista;

    @Column(name = "retorno")
    private Boolean retorno;

    @Column(name = "periodoRetorno")
    private Integer periodoRetorno;

    @Column(name = "observacoes", length = Integer.MAX_VALUE)
    private String observacoes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Consulta getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Consulta idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public LocalDate getDataAtendimento() {
        return dataAtendimento;
    }

    public void setDataAtendimento(LocalDate dataAtendimento) {
        this.dataAtendimento = dataAtendimento;
    }

    public String getProcedimentosRealizados() {
        return procedimentosRealizados;
    }

    public void setProcedimentosRealizados(String procedimentosRealizados) {
        this.procedimentosRealizados = procedimentosRealizados;
    }

    public String getAssinaturaDentista() {
        return assinaturaDentista;
    }

    public void setAssinaturaDentista(String assinaturaDentista) {
        this.assinaturaDentista = assinaturaDentista;
    }

    public Boolean getRetorno() {
        return retorno;
    }

    public void setRetorno(Boolean retorno) {
        this.retorno = retorno;
    }

    public Integer getPeriodoRetorno() {
        return periodoRetorno;
    }

    public void setPeriodoRetorno(Integer periodoRetorno) {
        this.periodoRetorno = periodoRetorno;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @OneToMany(mappedBy = "idAtendimento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private List<AtendimentoProcedimento> procedimentos = new ArrayList<>();

    public List<AtendimentoProcedimento> getProcedimentos() {
        return procedimentos;
    }

    public void setProcedimentos(List<AtendimentoProcedimento> procedimentos) {
        this.procedimentos = procedimentos;
    }
}
