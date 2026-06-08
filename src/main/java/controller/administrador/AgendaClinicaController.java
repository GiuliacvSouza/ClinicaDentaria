package controller.administrador;

import bll.AuditoriaService;
import bll.ConsultaService;
import bll.DentistaIndisponibilidadeService;
import bll.DentistaService;
import bll.UtilizadorService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Dentista;
import model.DentistaIndisponibilidade;
import model.Utilizador;
import model.enums.TipoIndisponibilidade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgendaClinicaController extends BaseAdministradorController {

    private static final String[] DIAS = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    @FXML private TextField txtPesquisaDentista;
    @FXML private VBox containerDentistas;
    @FXML private TextField txtAbertura;
    @FXML private TextField txtEncerramento;
    @FXML private Label lblDentista;
    @FXML private HBox containerDias;
    @FXML private TextField txtEntrada;
    @FXML private TextField txtSaida;
    @FXML private VBox containerIndisponibilidades;

    @Autowired private DentistaService dentistaService;
    @Autowired private UtilizadorService utilizadorService;
    @Autowired private AuditoriaService auditoriaService;
    @Autowired private DentistaIndisponibilidadeService indisponibilidadeService;
    @Autowired private ConsultaService consultaService;

    private List<Dentista> dentistas;
    private Dentista dentistaSelecionado;
    private final boolean[] diasSelecionados = new boolean[7];

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

        for (int i = 0; i < 7; i++) {
            diasSelecionados[i] = i < 5;
        }
        reconstruirDias();
        renderizarIndisponibilidades();
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

    private void renderizarIndisponibilidades() {
        containerIndisponibilidades.getChildren().clear();
        if (dentistaSelecionado == null) return;

        try {
            List<DentistaIndisponibilidade> lista = indisponibilidadeService
                    .listarPorDentista(dentistaSelecionado.getId());

            if (lista.isEmpty()) {
                Label vazio = new Label("Sem indisponibilidades registadas.");
                vazio.getStyleClass().add("section-caption");
                containerIndisponibilidades.getChildren().add(vazio);
                return;
            }

            for (DentistaIndisponibilidade ind : lista) {
                VBox card = new VBox(4);
                card.getStyleClass().add("info-item");
                card.setPadding(new Insets(8));

                Label tipoLabel = new Label(tipoDescricao(ind.getTipo()));
                tipoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c62828;");

                String descricao;
                switch (ind.getTipo()) {
                    case DIA_COMPLETO:
                        descricao = ind.getDataInicio().toString();
                        break;
                    case INTERVALO_HORAS:
                        descricao = ind.getDataInicio() + " das " + ind.getHoraInicio() + " às " + ind.getHoraFim();
                        break;
                    case PERIODO:
                        descricao = ind.getDataInicio() + " a " + ind.getDataFim();
                        break;
                    default:
                        descricao = "";
                }

                Label detalhe = new Label(descricao);
                detalhe.getStyleClass().add("info-title");

                String motivoStr = ind.getMotivo() != null ? " - " + ind.getMotivo() : "";
                Label motivo = new Label(motivoStr);
                motivo.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");

                Button btnRemover = new Button("Remover");
                btnRemover.setStyle("-fx-text-fill: #c62828; -fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: #c62828; -fx-border-radius: 4;");
                btnRemover.setOnAction(e -> {
                    try {
                        indisponibilidadeService.cancelar(ind.getId());
                        auditoriaService.registar(utilizadorLogado(), "REMOVER_INDISPONIBILIDADE",
                                "Dentista: " + obterNomeDentista() + " - " + descricao);
                        renderizarIndisponibilidades();
                        mostrarInfo("Indisponibilidade removida.");
                    } catch (Exception ex) {
                        mostrarErro("Erro: " + ex.getMessage());
                    }
                });

                card.getChildren().addAll(tipoLabel, detalhe, motivo, btnRemover);
                containerIndisponibilidades.getChildren().add(card);
            }
        } catch (Exception e) {
            Label erro = new Label("Erro ao carregar indisponibilidades.");
            erro.getStyleClass().add("section-caption");
            containerIndisponibilidades.getChildren().add(erro);
        }
    }

    @FXML
    private void adicionarIndisponibilidade() {
        if (dentistaSelecionado == null) {
            mostrarInfo("Selecione um dentista primeiro.");
            return;
        }

        Dialog<DentistaIndisponibilidade> dialog = new Dialog<>();
        dialog.setTitle("Nova Indisponibilidade");
        dialog.setHeaderText("Adicionar indisponibilidade para " + obterNomeDentista());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefWidth(450);
        grid.setPadding(new Insets(10));

        javafx.scene.layout.ColumnConstraints colLabel = new javafx.scene.layout.ColumnConstraints();
        colLabel.setPercentWidth(35);
        javafx.scene.layout.ColumnConstraints colField = new javafx.scene.layout.ColumnConstraints();
        colField.setPercentWidth(65);
        colField.setFillWidth(true);
        colField.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(colLabel, colField);

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("Dia Completo", "Intervalo de Horas", "Período");
        cmbTipo.setValue("Dia Completo");
        cmbTipo.setPrefWidth(Double.MAX_VALUE);

        DatePicker dpInicio = new DatePicker(LocalDate.now());
        dpInicio.setPrefWidth(250);

        DatePicker dpFim = new DatePicker(LocalDate.now().plusDays(1));
        dpFim.setPrefWidth(250);
        dpFim.setDisable(true);

        TextField txtHoraInicio = new TextField("14:00");
        txtHoraInicio.setPrefWidth(120);
        txtHoraInicio.setDisable(true);

        TextField txtHoraFim = new TextField("17:00");
        txtHoraFim.setPrefWidth(120);
        txtHoraFim.setDisable(true);

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Ex: Férias, Consulta médica, etc.");
        txtMotivo.setPrefWidth(250);

        cmbTipo.setOnAction(e -> {
            String tipo = cmbTipo.getValue();
            boolean isIntervalo = "Intervalo de Horas".equals(tipo);
            boolean isPeriodo = "Período".equals(tipo);
            txtHoraInicio.setDisable(!isIntervalo);
            txtHoraFim.setDisable(!isIntervalo);
            dpFim.setDisable(!isPeriodo);
        });

        grid.add(new Label("Tipo:"), 0, 0);
        grid.add(cmbTipo, 1, 0);
        grid.add(new Label("Data Início:"), 0, 1);
        grid.add(dpInicio, 1, 1);
        grid.add(new Label("Data Fim:"), 0, 2);
        grid.add(dpFim, 1, 2);
        grid.add(new Label("Hora Início:"), 0, 3);
        grid.add(txtHoraInicio, 1, 3);
        grid.add(new Label("Hora Fim:"), 0, 4);
        grid.add(txtHoraFim, 1, 4);
        grid.add(new Label("Motivo:"), 0, 5);
        grid.add(txtMotivo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    DentistaIndisponibilidade item = new DentistaIndisponibilidade();
                    item.setDentista(dentistaSelecionado);
                    item.setDataInicio(dpInicio.getValue());
                    item.setMotivo(txtMotivo.getText().trim());

                    String tipo = cmbTipo.getValue();
                    if ("Dia Completo".equals(tipo)) {
                        item.setTipo(TipoIndisponibilidade.DIA_COMPLETO);
                    } else if ("Intervalo de Horas".equals(tipo)) {
                        item.setTipo(TipoIndisponibilidade.INTERVALO_HORAS);
                        item.setHoraInicio(LocalTime.parse(txtHoraInicio.getText().trim() + ":00"));
                        item.setHoraFim(LocalTime.parse(txtHoraFim.getText().trim() + ":00"));
                    } else {
                        item.setTipo(TipoIndisponibilidade.PERIODO);
                        item.setDataFim(dpFim.getValue());
                    }
                    return item;
                } catch (Exception ex) {
                    mostrarErro("Erro nos dados: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<DentistaIndisponibilidade> res = dialog.showAndWait();
        res.ifPresent(item -> {
            try {
                indisponibilidadeService.criar(item);
                auditoriaService.registar(utilizadorLogado(), "CRIAR_INDISPONIBILIDADE",
                        "Dentista: " + obterNomeDentista() + " - " + item.getTipo() + " em " + item.getDataInicio());
                renderizarIndisponibilidades();
                mostrarInfo("Indisponibilidade adicionada.");
            } catch (Exception e) {
                mostrarErro("Erro ao guardar: " + e.getMessage());
            }
        });
    }

    @FXML
    private void guardarHorarioClinica() {
        try {
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

    private String obterNomeDentista() {
        if (dentistaSelecionado == null) return "";
        Utilizador u = dentistaSelecionado.getUtilizador();
        if (u == null) return "Dentista";
        return ((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim());
    }

    private String tipoDescricao(TipoIndisponibilidade tipo) {
        return switch (tipo) {
            case DIA_COMPLETO -> "🔴 Dia Completo";
            case INTERVALO_HORAS -> "🟡 Intervalo de Horas";
            case PERIODO -> "🔵 Período";
        };
    }
}