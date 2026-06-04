package controller.assistente;

import app.MainFX;
import bll.ConsultaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.dto.ConsultaAgendadaDTO;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConsultasDiaController extends BaseAssistenteController {

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField  txtPesquisa;
    @FXML private DatePicker dpFiltroData;
    @FXML private Button     btnHoje;
    @FXML private Button     btnTodas;
    @FXML private Button     btnAgendadas;
    @FXML private Button     btnEmEspera;
    @FXML private Button     btnEmConsulta;
    @FXML private Button     btnConcluidas;
    @FXML private Label      lblTotalConsultas;
    @FXML private VBox       containerConsultas;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private ConsultaService consultaService;

    private EstadoConsulta filtroStatus = null;
    private LocalDate      filtroData;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        filtroData = LocalDate.now();
        if (dpFiltroData != null) {
            dpFiltroData.setConverter(new StringConverter<>() {
                @Override
                public String toString(LocalDate value) {
                    return value != null ? value.format(DATA_FMT) : "";
                }

                @Override
                public LocalDate fromString(String value) {
                    return value == null || value.isBlank() ? null : LocalDate.parse(value.trim(), DATA_FMT);
                }
            });
            dpFiltroData.setValue(filtroData);
        }

        if (txtPesquisa != null)
            txtPesquisa.textProperty().addListener((obs, o, n) -> carregarConsultas());

        if (dpFiltroData != null)
            dpFiltroData.valueProperty().addListener((obs, o, n) -> { filtroData = n; carregarConsultas(); });

        atualizarEstilosChip(btnTodas);
        carregarConsultas();
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarHoje() {
        filtroData = LocalDate.now();
        if (dpFiltroData != null) dpFiltroData.setValue(filtroData);
        carregarConsultas();
    }

    @FXML private void filtrarTodas()      { filtroStatus = null;                       atualizarEstilosChip(btnTodas);      carregarConsultas(); }
    @FXML private void filtrarAgendadas()  { filtroStatus = EstadoConsulta.AGENDADA;    atualizarEstilosChip(btnAgendadas);  carregarConsultas(); }
    @FXML private void filtrarEmEspera()   { filtroStatus = EstadoConsulta.EM_ESPERA;   atualizarEstilosChip(btnEmEspera);   carregarConsultas(); }
    @FXML private void filtrarEmConsulta() { filtroStatus = EstadoConsulta.EM_CONSULTA; atualizarEstilosChip(btnEmConsulta); carregarConsultas(); }
    @FXML private void filtrarConcluidas() { filtroStatus = EstadoConsulta.CONCLUIDA;   atualizarEstilosChip(btnConcluidas); carregarConsultas(); }

    private void atualizarEstilosChip(Button ativo) {
        List<Button> chips = List.of(btnTodas, btnAgendadas, btnEmEspera, btnEmConsulta, btnConcluidas);
        for (Button b : chips) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active")) ativo.getStyleClass().add("filter-chip-active");
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarConsultas() {
        containerConsultas.getChildren().clear();
        try {
            List<ConsultaAgendadaDTO> lista = filtroStatus == null
                    ? consultaService.listarTodasAgendadas()
                    : consultaService.listarPorStatusAgendadas(filtroStatus);

            if (filtroData != null) {
                lista = lista.stream()
                        .filter(c -> c.getDataHoraInicio() != null
                                && LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault())
                                .toLocalDate().equals(filtroData))
                        .toList();
            }

            String pesquisa = txtPesquisa != null ? txtPesquisa.getText() : "";
            if (pesquisa != null && !pesquisa.isBlank()) {
                String t = pesquisa.trim().toLowerCase();
                lista = lista.stream()
                        .filter(c -> contem(c.getNomePaciente(), t)
                                || contem(c.getNomeDentista(), t)
                                || contem(c.getProcedimento(), t))
                        .toList();
            }

            lista = lista.stream()
                    .sorted(Comparator.comparing(ConsultaAgendadaDTO::getDataHoraInicio,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            if (lblTotalConsultas != null) lblTotalConsultas.setText(lista.size() + " consultas");

            if (lista.isEmpty()) {
                Label vazio = new Label("Nenhuma consulta encontrada.");
                vazio.getStyleClass().add("section-caption");
                containerConsultas.getChildren().add(vazio);
                return;
            }

            for (ConsultaAgendadaDTO c : lista) {
                containerConsultas.getChildren().add(criarCardConsulta(c));
            }

        } catch (Exception e) {
            Label erro = new Label("Não foi possível carregar as consultas.");
            erro.getStyleClass().add("section-caption");
            containerConsultas.getChildren().add(erro);
        }
    }

    // ─── Card de consulta ─────────────────────────────────────────────────────

    private VBox criarCardConsulta(ConsultaAgendadaDTO c) {
        VBox card = new VBox(8);
        card.getStyleClass().add("consulta-card");

        String hora = c.getDataHoraInicio() != null
                ? LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault()).format(HORA_FMT)
                : "--:--";

        // Linha principal: hora | info | estado | botão
        HBox linha = new HBox(12);
        linha.setAlignment(Pos.CENTER_LEFT);

        Label lblHora = new Label(hora);
        lblHora.getStyleClass().add("consulta-hora");
        lblHora.setMinWidth(72);

        VBox info = new VBox(2);
        Label lblPaciente = new Label(c.getNomePaciente() != null ? c.getNomePaciente() : "Paciente");
        lblPaciente.getStyleClass().add("consulta-paciente");

        String meta = "";
        if (c.getNomeDentista() != null) meta += "Dr(a). " + c.getNomeDentista();
        if (c.getProcedimento() != null) meta += (meta.isBlank() ? "" : " · ") + c.getProcedimento();
        Label lblMeta = new Label(meta);
        lblMeta.getStyleClass().add("consulta-meta");
        lblMeta.setWrapText(true);

        info.getChildren().addAll(lblPaciente, lblMeta);
        HBox.setHgrow(info, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(textoEstado(c.getStatus()));
        badge.getStyleClass().addAll("status-pill", classeEstado(c.getStatus()));

        Button btnDetalhes = new Button("Ver pormenores");
        btnDetalhes.getStyleClass().add("table-action-button");
        btnDetalhes.setMinWidth(112);
        btnDetalhes.setPrefWidth(112);
        btnDetalhes.setOnAction(e -> abrirModalDetalhes(c.getIdConsulta()));

        linha.getChildren().addAll(lblHora, info, spacer, badge, btnDetalhes);
        card.getChildren().add(linha);
        return card;
    }

    // ─── Modal de detalhes ────────────────────────────────────────────────────

    private void abrirModalDetalhes(Integer idConsulta) {
        if (idConsulta == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/assistente/detalhe-consulta-modal.fxml"));
            if (MainFX.getSpringContext() != null)
                loader.setControllerFactory(MainFX.getSpringContext()::getBean);

            Parent root = loader.load();
            DetalheConsultaController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(containerConsultas.getScene().getWindow());
            modal.setResizable(false);
            modal.setTitle("Detalhes da Consulta");

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/assistente-style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            ctrl.setStage(modal);
            modal.setScene(scene);

            ctrl.carregarConsulta(idConsulta);

            modal.showAndWait();

        } catch (Exception ex) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            a.setTitle("Erro");
            a.setHeaderText(null);
            a.setContentText("Não foi possível abrir os detalhes da consulta.");
            a.showAndWait();
        }
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private boolean contem(String campo, String termo) {
        return campo != null && campo.toLowerCase().contains(termo);
    }

    private String textoEstado(EstadoConsulta e) {
        if (e == null) return "-";
        return switch (e) {
            case AGENDADA    -> "Agendada";
            case CONFIRMADA  -> "Confirmada";
            case EM_ESPERA   -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA   -> "Concluída";
            case CANCELADA   -> "Cancelada";
            default          -> e.getDescricao();
        };
    }

    private String classeEstado(EstadoConsulta e) {
        if (e == null) return "status-agendado";
        return switch (e) {
            case AGENDADA    -> "status-agendado";
            case CONFIRMADA  -> "status-confirmado";
            case EM_ESPERA   -> "status-em-espera";
            case EM_CONSULTA -> "status-em-consulta";
            case CONCLUIDA   -> "status-concluido";
            case CANCELADA   -> "status-cancelado";
            default          -> "status-agendado";
        };
    }
}
