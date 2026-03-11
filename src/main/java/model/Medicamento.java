package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicamento")
public class Medicamento {
    @Id
    @Column(name = "idMedicamento", nullable = false)
    private Integer id;

    @Column(name = "nome", length = 100)
    private String nome;

    @Column(name = "fabricante", length = 100)
    private String fabricante;

    @Column(name = "tempoUso", length = 100)
    private String tempoUso;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getTempoUso() {
        return tempoUso;
    }

    public void setTempoUso(String tempoUso) {
        this.tempoUso = tempoUso;
    }

}