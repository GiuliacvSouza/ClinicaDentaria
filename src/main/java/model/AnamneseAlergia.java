package model;

import jakarta.persistence.*;
import model.enums.Gravidade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "anamneseAlergia")
public class AnamneseAlergia {
    @EmbeddedId
    private AnamneseAlergiaId id;

    @MapsId("idAnamnese")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idAnamnese", nullable = false)
    private Anamnese idAnamnese;

    @MapsId("idAlergia")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idAlergia", nullable = false)
    private Alergia idAlergia;

    @Enumerated(EnumType.STRING)
    private Gravidade gravidade;

    public AnamneseAlergiaId getId() {
        return id;
    }

    public void setId(AnamneseAlergiaId id) {
        this.id = id;
    }

    public Anamnese getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Anamnese idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Alergia getIdAlergia() {
        return idAlergia;
    }

    public void setIdAlergia(Alergia idAlergia) {
        this.idAlergia = idAlergia;
    }

    public Gravidade getGravidade() {
        return gravidade;
    }

    public void setGravidade(Gravidade gravidade) {
        this.gravidade = gravidade;
    }
}