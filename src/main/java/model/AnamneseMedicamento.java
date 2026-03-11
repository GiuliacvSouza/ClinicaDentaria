package model;

import jakarta.persistence.*;

@Entity
@Table(name = "anamneseMedicamento")
public class AnamneseMedicamento {
    @EmbeddedId
    private AnamneseMedicamentoId id;

    @MapsId("idAnamnese")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idAnamnese", nullable = false)
    private Anamnese idAnamnese;

    @MapsId("idMedicamento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idMedicamento", nullable = false)
    private Medicamento idMedicamento;

    @Column(name = "dosagem", length = 100)
    private String dosagem;

    public AnamneseMedicamentoId getId() {
        return id;
    }

    public void setId(AnamneseMedicamentoId id) {
        this.id = id;
    }

    public Anamnese getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Anamnese idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Medicamento getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(Medicamento idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

}