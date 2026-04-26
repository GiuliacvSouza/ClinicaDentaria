package model.dto;

import java.time.Instant;
import model.enums.EstadoConsulta;

public class ConsultaAgendadaDTO {
    private Integer idConsulta;
    private String nomePaciente;
    private String nomeDentista;
    private String procedimento;
    private Instant dataHoraInicio;
    private EstadoConsulta status;
    private String nifPaciente;

    public ConsultaAgendadaDTO() {}

    public ConsultaAgendadaDTO(Integer idConsulta, String nomePaciente, String nomeDentista, 
                              String procedimento, Instant dataHoraInicio, EstadoConsulta status) {
        this.idConsulta = idConsulta;
        this.nomePaciente = nomePaciente;
        this.nomeDentista = nomeDentista;
        this.procedimento = procedimento;
        this.dataHoraInicio = dataHoraInicio;
        this.status = status;
    }

    public ConsultaAgendadaDTO(Integer idConsulta, String nomePaciente, String nomeDentista,
                               String procedimento, Instant dataHoraInicio, EstadoConsulta status, String nifPaciente) {
        this(idConsulta, nomePaciente, nomeDentista, procedimento, dataHoraInicio, status);
        this.nifPaciente = nifPaciente;
    }

    public String getNifPaciente() {
        return nifPaciente;
    }

    public void setNifPaciente(String nifPaciente) {
        this.nifPaciente = nifPaciente;
    }

    public Integer getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Integer idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }

    public String getNomeDentista() {
        return nomeDentista;
    }

    public void setNomeDentista(String nomeDentista) {
        this.nomeDentista = nomeDentista;
    }

    public String getProcedimento() {
        return procedimento;
    }

    public void setProcedimento(String procedimento) {
        this.procedimento = procedimento;
    }

    public Instant getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(Instant dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public EstadoConsulta getStatus() {
        return status;
    }

    public void setStatus(EstadoConsulta status) {
        this.status = status;
    }
}
