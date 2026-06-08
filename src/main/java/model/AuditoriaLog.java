package model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_log")
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria", nullable = false)
    private Integer id;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "utilizador_nome", length = 200)
    private String utilizadorNome;

    @Column(name = "utilizador_perfil", length = 50)
    private String utilizadorPerfil;

    @Column(name = "operacao", length = 100)
    private String operacao;

    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;

    public AuditoriaLog() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getUtilizadorNome() {
        return utilizadorNome;
    }

    public void setUtilizadorNome(String utilizadorNome) {
        this.utilizadorNome = utilizadorNome;
    }

    public String getUtilizadorPerfil() {
        return utilizadorPerfil;
    }

    public void setUtilizadorPerfil(String utilizadorPerfil) {
        this.utilizadorPerfil = utilizadorPerfil;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}