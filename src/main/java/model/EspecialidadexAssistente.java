package model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "especialidadexassistente")
public class EspecialidadexAssistente {
    @EmbeddedId
    private EspecialidadexAssistenteId id;

    @MapsId("idUtilizador")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Assistente idUtilizador;

    @MapsId("idEspecialidade")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "idEspecialidade", nullable = false)
    private Especialidade idEspecialidade;

    public EspecialidadexAssistenteId getId() {
        return id;
    }

    public void setId(EspecialidadexAssistenteId id) {
        this.id = id;
    }

    public Assistente getIdUtilizador() {
        return idUtilizador;
    }

    public void setIdUtilizador(Assistente idUtilizador) {
        this.idUtilizador = idUtilizador;
    }

    public Especialidade getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Especialidade idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

}