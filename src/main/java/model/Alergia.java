package model;

import jakarta.persistence.*;
import model.enums.TipoAlergia;

@Entity
@Table(name = "alergia")
public class Alergia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAlergia", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 100)
    private TipoAlergia tipo;
    //EX: Medicamento, Alimento, etc..

    @Column(name = "substancia", length = 150)
    private String substancia;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoAlergia getTipo() { return tipo; }

    public void setTipo(TipoAlergia tipo) { this.tipo = tipo; }

    public String getSubstancia() {
        return substancia;
    }

    public void setSubstancia(String substancia) {
        this.substancia = substancia;
    }

}