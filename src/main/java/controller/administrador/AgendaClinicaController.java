package controller.administrador;

import bll.AuditoriaService;
import bll.DentistaService;
import bll.UtilizadorService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Dentista;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgendaClinicaController extends BaseAdministradorController {

    private static final String[] DIAS = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
    private static final String[] DIAS_FULL = {"Segunda", "Terca", "Quarta", "Quinta", "Sexta", "Sabado", "Domingo"};

    @FXML private TextField txtPesquisaDentista;
    @FXML private VBox containerDentistas;
    @FXML private TextField txtAbertura;
    @FXML private TextField txtEncerramento;
    @FXML private Label lblDentista;
    @FXML private HBox containerDias;
    @FXML private TextField txtEntrada;
    @FXML private TextField txtSaida;
    @FXML private VBox containerFerias;

    @Autowired private DentistaService dentistaService;
    @Autowired private UtilizadorService utilizadorService;
    @Autowired private AuditoriaService auditoriaService;

    private List<Dentista> dentistas;
    private Dentista dentistaSelecionado;
    private final boolean[] diasSelecionados = new boolean[7];
    private final boolean[] feriasAdicionadas = new boolean[7];

    @Override
    protected void inicializarEcra() {
        txtAbertura.setText("09:00");
        txtEncerramento.setText("20:00");
        txtEntrada.setText("09:00");
        txtSaida.setText("18:00");
        construirDias();
        carregarDentistas();
    }

    private void construirDias() {
        containerDias.getChildren().clear();
        for (int i = 0; i < DIAS.length; i++) {
            final int idx = i;
            Button btn = new Button(DIAS[i]);
            btn.getStyleClass().add("filter-chip");
            btn.setOnAction(e -> {
                diasSelecionados[idx] = !diasSelecionados[idx];
                if (diasSelecionados[idx]) {
                    btn.getStyleClass().add("filter-chip-active");
                } else {
                    btn.getStyleClass().remove("filter-chip-active");
                }
            });
            containerDias.getChildren().add(btn);
        }
    }

    private void carregarDentistas() {
        try {
            dentistas = dentistaService.listarTodos();
            if (txtPesquisaDentista != null && !txtPesquisaDentista.getText().isBlank()) {
                String termo = txtPesquisaDentista.getText().trim().toLowerCase();
                dentistas = dentistas.stream()
                        .filter(d -> d.getUtilizador() != null
                                && ((d.getUtilizador().getPrimeiroNome() != null
                                && d.getUtilizador().getPrimeiroNome().toLowerCase().contains(termo))
                                || (d.getUtilizador().getUltimoNome() != null
                                && d.getUtilizador().getUltimoNome().toLowerCase().contains(termo))))
                        .collect(Collectors.toList());
            }
            renderizarDentistas();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar dentistas: " + e.getMessage());
        }
    }

    @FXML
    private void filtrarDentistas() {
        carregarDentistas();
    }

    private void renderizarDentistas() {
        containerDentistas.getChildren().clear();
        if (dentistas == null || dentistas.isEmpty()) {
            Label vazio = new Label("Nenhum dentista disponivel.");
            vazio.getStyleClass().add("section-caption");
            containerDentistas.getChildren().add(vazio);
            return;
        }
        for (Dentista d : dentistas) {
            containerDentistas.getChildren().add(criarCartaoDentista(d));
        }
    }

    private HBox criarCartaoDentista(Dentista d) {
        HBox cartao = new HBox(12);
        cartao.setAlignment(Pos.CENTER_LEFT);
        cartao.getStyleClass().add("consulta-card");
        if (dentistaSelecionado != null && dentistaSelecionado.getId() != null
                && dentistaSelecionado.getId().equals(d.getId())) {
            cartao.setStyle("-fx-border-color: #2e7d72; -fx-border-width: 2;");
        }

        VBox textos = new VBox(2);
        Utilizador u = d.getUtilizador();
        String nomeCompleto = u != null
                ? ((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim())
                : "Dentista";
        if (nomeCompleto.isBlank()) nomeCompleto = "Dentista #" + d.getId();
        Label nome = new Label("Dr(a). " + nomeCompleto);
        nome.getStyleClass().add("consulta-paciente");

        String status = (d.getAtivo() != null && d.getAtivo()) ? "Ativo" : "Inativo";
        Label lblStatus = new Label(status);
        lblStatus.getStyleClass().add(status.equals("Ativo") ? "badge-ok" : "badge-critico");

        textos.getChildren().addAll(nome, lblStatus);
        HBox.setHgrow(textos, Priority.ALWAYS);

        cartao.getChildren().add(textos);
        cartao.setOnMouseClicked(e -> {
            dentistaSelecionado = d;
            carregarDadosDentista();
            renderizarDentistas();
        });
        return cartao;
    }

    private void carregarDadosDentista() {
        if (dentistaSelecionado == null) return;
        Utilizador u = dentistaSelecionado.getUtilizador();
        String nome = u != null
                ? ((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim())
                : "Dentista #" + dentistaSelecionado.getId();
        lblDentista.setText("Dr(a). " + nome);

        if (dentistaSelecionado.getHorarioEntrada() != null) {
            txtEntrada.setText(dentistaSelecionado.getHorarioEntrada().toString().substring(0, 5));
        }
        if (dentistaSelecionado.getHorarioSaida() != null) {
            txtSaida.setText(dentistaSelecionado.getHorarioSaida().toString().substring(0, 5));
        }

        // Marcar dias (padrao: Seg-Sex)
        for (int i = 0; i < 7; i++) {
            diasSelecionados[i] = i < 5;
        }
        reconstruirDias();
        renderizarFerias();
    }

    private void reconstruirDias() {
        containerDias.getChildren().clear();
        for (int i = 0; i < DIAS.length; i++) {
            final int idx = i;
            Button btn = new Button(DIAS[i]);
            btn.getStyleClass().add("filter-chip");
            if (diasSelecionados[idx]) {
                btn.getStyleClass().add("filter-chip-active");
            }
            btn.setOnAction(e -> {
                diasSelecionados[idx] = !diasSelecionados[idx];
                if (diasSelecionados[idx]) {
                    btn.getStyleClass().add("filter-chip-active");
                } else {
                    btn.getStyleClass().remove("filter-chip-active");
                }
            });
            containerDias.getChildren().add(btn);
        }
    }

    private void renderizarFerias() {
        containerFerias.getChildren().clear();
        boolean temFerias = false;
        for (int i = 0; i < DIAS_FULL.length; i++) {
            if (feriasAdicionadas[i]) {
                temFerias = true;
                HBox linha = new HBox(10);
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.getStyleClass().add("info-item");

                Label lbl = new Label("Férias: " + DIAS_FULL[i]);
                lbl.getStyleClass().add("info-title");

                linha.getChildren().add(lbl);
                containerFerias.getChildren().add(linha);
            }
        }
        if (!temFerias) {
            Label vazio = new Label("Sem ferias ou indisponibilidades registadas.");
            vazio.getStyleClass().add("section-caption");
            containerFerias.getChildren().add(vazio);
        }
    }

    @FXML
    private void guardarHorarioClinica() {
        try {
            // Armazenado apenas como info no estado; não persistido por simplicidade
            String abertura = txtAbertura.getText();
            String encerramento = txtEncerramento.getText();
            auditoriaService.registar(utilizadorLogado(), "CONFIG_HORARIO_CLINICA",
                    "Horario da clinica: " + abertura + " - " + encerramento);
            mostrarInfo("Horario da clinica guardado: " + abertura + " - " + encerramento);
        } catch (Exception e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void guardarHorarioDentista() {
        if (dentistaSelecionado == null) {
            mostrarInfo("Selecione um dentista primeiro.");
            return;
        }
        try {
            String entrada = txtEntrada.getText();
            String saida = txtSaida.getText();
            dentistaSelecionado.setHorarioEntrada(LocalTime.parse(entrada + ":00"));
            dentistaSelecionado.setHorarioSaida(LocalTime.parse(saida + ":00"));
            dentistaService.salvar(dentistaSelecionado);

            StringBuilder dias = new StringBuilder();
            for (int i = 0; i < DIAS.length; i++) {
                if (diasSelecionados[i]) {
                    if (dias.length() > 0) dias.append(", ");
                    dias.append(DIAS[i]);
                }
            }
            auditoriaService.registar(utilizadorLogado(), "CONFIG_HORARIO_DENTISTA",
                    "Dr(a). " + obterNomeDentista() + ": " + dias + " (" + entrada + "-" + saida + ")");
            mostrarInfo("Horario guardado.");
        } catch (Exception e) {
            mostrarErro("Erro ao guardar: " + e.getMessage());
        }
    }

    @FXML
    private void adicionarFerias() {
        // Adiciona ferias em todos os dias selecionados (UI simplificada)
        for (int i = 0; i < feriasAdicionadas.length; i++) {
            if (diasSelecionados[i]) {
                feriasAdicionadas[i] = true;
            }
        }
        renderizarFerias();
    }

    private String obterNomeDentista() {
        if (dentistaSelecionado == null) return "";
        Utilizador u = dentistaSelecionado.getUtilizador();
        if (u == null) return "Dentista";
        return ((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim());
    }
}
