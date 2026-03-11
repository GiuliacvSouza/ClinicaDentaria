package model;

import jakarta.persistence.*;

@Entity
@Table(name = "contatoEmergencia")
public class ContatoEmergencia {
    @Id
    @Column(name = "contatoEmergencia", nullable = false)
    private Integer id;

    // faltava isso
    @ManyToOne
    @JoinColumn(name = "idPaciente")
    private Paciente paciente;

    @Column(name = "primeiroNome", length = 100)
    private String primeiroNome;

    @Column(name = "ultimoNome", length = 100)
    private String ultimoNome;

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

    public String getPrimeiroNome() {
        return primeiroNome;
    }

    public void setPrimeiroNome(String primeiroNome) {
        this.primeiroNome = primeiroNome;
    }

    public String getUltimoNome() {
        return ultimoNome;
    }

    public void setUltimoNome(String ultimoNome) {
        this.ultimoNome = ultimoNome;
    }
}