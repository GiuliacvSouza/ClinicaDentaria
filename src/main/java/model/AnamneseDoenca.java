package model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "anamneseDoenca")
public class AnamneseDoenca {
    @EmbeddedId
    private AnamneseDoencaId id;

    @MapsId("idAnamnese")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idAnamnese", nullable = false)
    private Anamnese idAnamnese;

    @MapsId("idDoenca")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idDoenca", nullable = false)
    private Doenca idDoenca;

    @Column(name = "descricaoPaciente", length = Integer.MAX_VALUE)
    private String descricaoPaciente;

    public AnamneseDoencaId getId() {
        return id;
    }

    public void setId(AnamneseDoencaId id) {
        this.id = id;
    }

    public Anamnese getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Anamnese idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Doenca getIdDoenca() {
        return idDoenca;
    }

    public void setIdDoenca(Doenca idDoenca) {
        this.idDoenca = idDoenca;
    }

    public String getDescricaoPaciente() {
        return descricaoPaciente;
    }

    public void setDescricaoPaciente(String descricaoPaciente) {
        this.descricaoPaciente = descricaoPaciente;
    }

}