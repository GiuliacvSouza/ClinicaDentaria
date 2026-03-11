package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "seguro")
public class Seguro {
    @Id
    @Column(name = "idSeguro", nullable = false)
    private Integer id;

    @Column(name = "nomeSeguro", length = 100)
    private String nomeSeguro;

    @Column(name = "tipoPlano", length = 100)
    private String tipoPlano;

    @Column(name = "codigoPlano", length = 100)
    private String codigoPlano;

    @Column(name = "contactoSeguradora", length = 150)
    private String contactoSeguradora;

    @Column(name = "validoAte")
    private LocalDate validoAte;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeSeguro() {
        return nomeSeguro;
    }

    public void setNomeSeguro(String nomeSeguro) {
        this.nomeSeguro = nomeSeguro;
    }

    public String getTipoPlano() {
        return tipoPlano;
    }

    public void setTipoPlano(String tipoPlano) {
        this.tipoPlano = tipoPlano;
    }

    public String getCodigoPlano() {
        return codigoPlano;
    }

    public void setCodigoPlano(String codigoPlano) {
        this.codigoPlano = codigoPlano;
    }

    public String getContactoSeguradora() {
        return contactoSeguradora;
    }

    public void setContactoSeguradora(String contactoSeguradora) {
        this.contactoSeguradora = contactoSeguradora;
    }

    public LocalDate getValidoAte() {
        return validoAte;
    }

    public void setValidoAte(LocalDate validoAte) {
        this.validoAte = validoAte;
    }
}