package model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "utilizador")
public class Utilizador {
    @Id
    @Column(name = "idUtilizador", nullable = false)
    private Integer id;

    @Column(name = "primeiroNome", length = 100)
    private String primeiroNome;

    @Column(name = "ultimoNome", length = 100)
    private String ultimoNome;

    @Column(name = "tipoUtilizador", length = 50)
    private String tipoUtilizador;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "telemovel", length = 20)
    private String telemovel;

    @Column(name = "dataNascimento")
    private LocalDate dataNascimento;

    @Column(name = "ultimoAcesso")
    private Instant ultimoAcesso;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "rua", length = 150)
    private String rua;

    @Column(name = "numeroPorta", length = 10)
    private String numeroPorta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigoPostal")
    private CodigoPostal codigoPostal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getTipoUtilizador() {
        return tipoUtilizador;
    }

    public void setTipoUtilizador(String tipoUtilizador) {
        this.tipoUtilizador = tipoUtilizador;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTelemovel() {
        return telemovel;
    }

    public void setTelemovel(String telemovel) {
        this.telemovel = telemovel;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Instant getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(Instant ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumeroPorta() {
        return numeroPorta;
    }

    public void setNumeroPorta(String numeroPorta) {
        this.numeroPorta = numeroPorta;
    }

    public CodigoPostal getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(CodigoPostal codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}