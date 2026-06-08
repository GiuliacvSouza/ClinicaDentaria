package model.enums;

public enum EstadoPlanoTratamento {
    PLANEADO("Planeado"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDO("Concluido");

    private final String descricao;

    EstadoPlanoTratamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
