package model;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidadeDentista")
public class EspecialidadeDentista {
    @EmbeddedId
    private EspecialidadeDentistaId id;

    @MapsId("idUtilizador")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUtilizador", nullable = false)
    private Dentista idUtilizador;

    @MapsId("idEspecialidade")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idEspecialidade", nullable = false)
    private Especialidade idEspecialidade;

    public EspecialidadeDentistaId getId() {
        return id;
    }

    public void setId(EspecialidadeDentistaId id) {
        this.id = id;
    }

    public Dentista getIdUtilizador() {
        return idUtilizador;
    }

    public void setIdUtilizador(Dentista idUtilizador) {
        this.idUtilizador = idUtilizador;
    }

    public Especialidade getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Especialidade idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

}