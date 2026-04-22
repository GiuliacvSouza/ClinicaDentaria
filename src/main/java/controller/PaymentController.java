package controller;

import app.MainFX;
import app.SessionContext;
import bll.AtendimentoService;
import bll.ConsultaService;
import bll.FaturaService;
import bll.PacientexSeguroService;
import bll.PagamentoService;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.Duration;
import model.Atendimento;
import model.AtendimentoProcedimento;
import model.Consulta;
import model.Fatura;
import model.Paciente;
import model.PacientexSeguro;
import model.Pagamento;
import model.Procedimento;
import model.Seguro;
import model.Utilizador;
import model.enums.EstadoConsulta;
import model.enums.EstadoFatura;
import model.enums.MetodoPagamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PaymentController {

    @Autowired private ConsultaService consultaService;
    @Autowired private AtendimentoService atendimentoService;
    @Autowired private FaturaService faturaService;
    @Autowired private PacientexSeguroService pacientexSeguroService;
    @Autowired private PagamentoService pagamentoService;

    @FXML private TextField pesquisarField;
    @FXML private ListView<Consulta> consultasListView;
    @FXML private Label nomeUtilizador;
    @FXML private Label pacienteNome;
    @FXML private Label pacienteNif;
    @FXML private VBox procedimentosContainer;
    @FXML private Label valorPagarLabel;
    @FXML private ComboBox<Seguro> seguroCombo;
    @FXML private ToggleButton numerarioBtn;
    @FXML private ToggleButton multibancoBtn;
    @FXML private ToggleButton mbwayBtn;
    @FXML private ToggleGroup pagamentoGroup;
    @FXML private Button emitirReciboBtn;
    @FXML private VBox paymentSkeleton;
    @FXML private VBox paymentDetailsContent;

    private FilteredList<Consulta> filteredConsultas;
    private Atendimento atendimentoSelecionado;
    private Fatura faturaAtual;
    private FadeTransition skeletonPulse;

    @FXML
    public void initialize() {
        if (pagamentoGroup == null) {
            pagamentoGroup = new ToggleGroup();
        }

        numerarioBtn.setToggleGroup(pagamentoGroup);
        multibancoBtn.setToggleGroup(pagamentoGroup);
        mbwayBtn.setToggleGroup(pagamentoGroup);

        Utilizador utilizadorLogado = SessionContext.getUtilizadorLogado();
        if (utilizadorLogado != null) {
            nomeUtilizador.setText(utilizadorLogado.getPrimeiroNome() + " " + utilizadorLogado.getUltimoNome());
        }

        seguroCombo.setDisable(true);
        seguroCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Seguro seguro) {
                return seguro == null ? "" : seguro.getNomeSeguro();
            }

            @Override
            public Seguro fromString(String string) {
                return null;
            }
        });

        if (consultaService == null
                || atendimentoService == null
                || faturaService == null
                || pacientexSeguroService == null
                || pagamentoService == null) {
            return;
        }

        configurarSkeleton();

        List<Consulta> consultas = consultaService.listarPorStatus(EstadoConsulta.CONCLUIDA)
                .stream()
                .filter(this::consultaDisponivelParaPagamento)
                .collect(Collectors.toList());

        filteredConsultas = new FilteredList<>(FXCollections.observableArrayList(consultas), consulta -> true);
        consultasListView.setItems(filteredConsultas);

        pesquisarField.textProperty().addListener((obs, old, newVal) -> filteredConsultas.setPredicate(consulta -> {
            if (newVal == null || newVal.isBlank()) {
                return true;
            }

            Utilizador paciente = getPacienteUtilizador(consulta);
            if (paciente == null) {
                return false;
            }

            String filtro = newVal.toLowerCase();
            return paciente.getPrimeiroNome().toLowerCase().contains(filtro)
                    || paciente.getUltimoNome().toLowerCase().contains(filtro)
                    || (paciente.getNif() != null && paciente.getNif().contains(filtro));
        }));

        consultasListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                carregarDetalhesConsulta(selected);
            } else {
                limparResumo();
            }
        });

        consultasListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Consulta consulta, boolean empty) {
                super.updateItem(consulta, empty);

                if (empty || consulta == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Utilizador paciente = getPacienteUtilizador(consulta);
                Utilizador dentista = getDentistaUtilizador(consulta);

                VBox cell = new VBox(5);
                HBox topRow = new HBox();

                Label nomeLabel = new Label(paciente != null
                        ? paciente.getPrimeiroNome() + " " + paciente.getUltimoNome()
                        : "Paciente sem dados");
                nomeLabel.setStyle("-fx-font-weight: bold;");

                Label horaLabel = new Label(consulta.getDataHoraInicio()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")));
                horaLabel.setStyle("-fx-text-fill: #666;");

                topRow.getChildren().addAll(nomeLabel, new Region(), horaLabel);
                HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

                Label nifLabel = new Label("NIF: " + (paciente != null ? paciente.getNif() : "-"));
                Label dentistaLabel = new Label("Dr. " + (dentista != null ? dentista.getUltimoNome() : "-"));

                cell.getChildren().addAll(topRow, nifLabel, dentistaLabel);

                if (consulta.getTipo() != null && !consulta.getTipo().isBlank()) {
                    Label tipoLabel = new Label(consulta.getTipo());
                    tipoLabel.setStyle("-fx-background-color: #D9EBE8; -fx-background-radius: 12; -fx-padding: 4 10;");
                    cell.getChildren().add(tipoLabel);
                }

                setGraphic(cell);
            }
        });

        limparResumo();
    }

    private boolean consultaDisponivelParaPagamento(Consulta consulta) {
        Atendimento atendimento = atendimentoService.buscarPorConsulta(consulta);
        if (atendimento == null) {
            return false;
        }

        Fatura fatura = faturaService.buscarPorAtendimento(atendimento.getId());
        return fatura == null || fatura.getEstado() != EstadoFatura.PAGA;
    }

    private void carregarDetalhesConsulta(Consulta consulta) {
        mostrarSkeleton(true);

        Utilizador paciente = getPacienteUtilizador(consulta);

        pacienteNome.setText(paciente != null ? paciente.getPrimeiroNome() + " " + paciente.getUltimoNome() : "-");
        pacienteNif.setText("NIF: " + (paciente != null ? paciente.getNif() : "-"));

        atendimentoSelecionado = atendimentoService.buscarPorConsulta(consulta);
        if (atendimentoSelecionado == null) {
            limparResumoFinanceiro();
            mostrarAlerta("A consulta ainda nÃ£o tem atendimento associado.");
            return;
        }

        try {
            faturaAtual = faturaService.buscarOuEmitirPorAtendimento(atendimentoSelecionado);
        } catch (RuntimeException e) {
            limparResumoFinanceiro();
            mostrarAlerta(e.getMessage());
            return;
        }

        carregarSegurosPaciente(consulta.getIdPaciente());
        preencherProcedimentos();
        atualizarTotais();
        mostrarSkeleton(false);
    }

    private void configurarSkeleton() {
        if (paymentSkeleton == null) {
            return;
        }

        skeletonPulse = new FadeTransition(Duration.millis(950), paymentSkeleton);
        skeletonPulse.setFromValue(0.55);
        skeletonPulse.setToValue(1.0);
        skeletonPulse.setAutoReverse(true);
        skeletonPulse.setCycleCount(Animation.INDEFINITE);
        mostrarSkeleton(true);
    }

    private void mostrarSkeleton(boolean mostrar) {
        if (paymentSkeleton != null) {
            paymentSkeleton.setVisible(mostrar);
            paymentSkeleton.setManaged(mostrar);
            paymentSkeleton.setOpacity(1.0);
        }

        if (paymentDetailsContent != null) {
            paymentDetailsContent.setVisible(!mostrar);
            paymentDetailsContent.setManaged(!mostrar);
        }

        if (skeletonPulse != null) {
            if (mostrar) {
                skeletonPulse.play();
            } else {
                skeletonPulse.stop();
                paymentSkeleton.setOpacity(1.0);
            }
        }
    }

    private void preencherProcedimentos() {
        procedimentosContainer.getChildren().clear();

        for (AtendimentoProcedimento item : atendimentoSelecionado.getProcedimentos()) {
            Procedimento procedimento = item.getProcedimento();
            if (procedimento == null || procedimento.getValor() == null) {
                continue;
            }

            int quantidade = item.getQuantidade() != null ? item.getQuantidade() : 1;
            BigDecimal desconto = item.getDesconto() != null ? item.getDesconto() : BigDecimal.ZERO;
            BigDecimal taxaIva = procedimento.getTaxaIva() != null ? procedimento.getTaxaIva() : BigDecimal.ZERO;

            BigDecimal subtotal = procedimento.getValor().multiply(BigDecimal.valueOf(quantidade));
            BigDecimal comDesconto = subtotal.multiply(
                    BigDecimal.ONE.subtract(desconto.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
            );
            BigDecimal valorFinalItem = comDesconto.multiply(
                    BigDecimal.ONE.add(taxaIva.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
            ).setScale(2, RoundingMode.HALF_UP);

            GridPane linhaProcedimento = new GridPane();
            linhaProcedimento.setHgap(10);
            linhaProcedimento.getStyleClass().add("procedure-row");
            linhaProcedimento.getColumnConstraints().addAll(
                    criarColunaProcedimento(38),
                    criarColunaProcedimento(22),
                    criarColunaProcedimento(14),
                    criarColunaProcedimento(26)
            );

            Label procedimentoLabel = new Label(procedimento.getNome());
            procedimentoLabel.getStyleClass().add("procedure-cell-primary");
            procedimentoLabel.setWrapText(true);

            Label tipoLabel = new Label(procedimento.getTipo() != null && !procedimento.getTipo().isBlank()
                    ? procedimento.getTipo()
                    : "-");
            tipoLabel.getStyleClass().add("procedure-cell");
            tipoLabel.setWrapText(true);

            Label ivaLabel = new Label(formatarPercentual(taxaIva));
            ivaLabel.getStyleClass().add("procedure-cell");

            Label valorFinalLabel = new Label(formatarMoeda(valorFinalItem));
            valorFinalLabel.getStyleClass().add("procedure-cell-value");

            linhaProcedimento.add(procedimentoLabel, 0, 0);
            linhaProcedimento.add(tipoLabel, 1, 0);
            linhaProcedimento.add(ivaLabel, 2, 0);
            linhaProcedimento.add(valorFinalLabel, 3, 0);
            GridPane.setHalignment(valorFinalLabel, HPos.RIGHT);

            procedimentosContainer.getChildren().add(linhaProcedimento);
        }
    }

    private ColumnConstraints criarColunaProcedimento(double percentWidth) {
        ColumnConstraints coluna = new ColumnConstraints();
        coluna.setPercentWidth(percentWidth);
        coluna.setFillWidth(true);
        return coluna;
    }

    private void carregarSegurosPaciente(Paciente paciente) {
        if (paciente == null || paciente.getId() == null) {
            seguroCombo.getItems().clear();
            seguroCombo.setDisable(true);
            return;
        }

        LocalDate hoje = LocalDate.now();
        Set<Seguro> segurosAtivos = pacientexSeguroService.listarTodos().stream()
                .filter(Objects::nonNull)
                .filter(ps -> ps.getIdUtilizador() != null && Objects.equals(ps.getIdUtilizador().getId(), paciente.getId()))
                .filter(ps -> ps.getDataInicioCobertura() == null || !ps.getDataInicioCobertura().isAfter(hoje))
                .filter(ps -> ps.getDataFimCobertura() == null || !ps.getDataFimCobertura().isBefore(hoje))
                .map(PacientexSeguro::getIdSeguro)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        seguroCombo.setItems(FXCollections.observableArrayList(segurosAtivos));
        seguroCombo.getSelectionModel().clearSelection();
        seguroCombo.setDisable(segurosAtivos.isEmpty());
    }

    @FXML
    private void emitirRecibo() {
        if (faturaAtual == null) {
            mostrarAlerta("Selecione uma consulta primeiro.");
            return;
        }

        if (pagamentoGroup.getSelectedToggle() == null) {
            mostrarAlerta("Selecione um mÃ©todo de pagamento.");
            return;
        }

        Utilizador utilizadorLogado = SessionContext.getUtilizadorLogado();
        if (utilizadorLogado == null) {
            mostrarAlerta("SessÃ£o expirada. FaÃ§a login novamente.");
            return;
        }

        if (faturaAtual.getEstado() == EstadoFatura.PAGA) {
            mostrarAlerta("Esta fatura jÃ¡ foi paga.", Alert.AlertType.INFORMATION);
            return;
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setIdFatura(faturaAtual);
        pagamento.setIdUtilizador(utilizadorLogado);
        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setValorPago(obterValorAPagar());
        pagamento.setMetodo(getMetodoSelecionado());

        pagamentoService.registrarPagamento(pagamento);

        faturaAtual.setEstado(EstadoFatura.PAGA);
        faturaAtual = faturaService.salvar(faturaAtual);

        Consulta consultaSelecionada = consultasListView.getSelectionModel().getSelectedItem();
        if (consultaSelecionada != null) {
            filteredConsultas.getSource().remove(consultaSelecionada);
        }

        consultasListView.getSelectionModel().clearSelection();
        limparResumo();
        mostrarAlerta("Pagamento registado com sucesso! Fatura emitida.", Alert.AlertType.INFORMATION);
    }

    private MetodoPagamento getMetodoSelecionado() {
        Toggle selectedToggle = pagamentoGroup.getSelectedToggle();
        if (selectedToggle == numerarioBtn) {
            return MetodoPagamento.DINHEIRO;
        }
        if (selectedToggle == multibancoBtn) {
            return MetodoPagamento.CARTAO;
        }
        if (selectedToggle == mbwayBtn) {
            return MetodoPagamento.MBWAY;
        }
        throw new RuntimeException("Selecione um mÃ©todo de pagamento.");
    }

    private BigDecimal obterValorAPagar() {
        BigDecimal valor = faturaAtual != null ? faturaAtual.getValorFinal() : BigDecimal.ZERO;
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void atualizarTotais() {
        BigDecimal total = obterValorAPagar();
        valorPagarLabel.setText(formatarMoeda(total));
    }

    private Utilizador getPacienteUtilizador(Consulta consulta) {
        return consulta != null && consulta.getIdPaciente() != null ? consulta.getIdPaciente().getUtilizador() : null;
    }

    private Utilizador getDentistaUtilizador(Consulta consulta) {
        return consulta != null && consulta.getIdDentista() != null ? consulta.getIdDentista().getUtilizador() : null;
    }

    private void limparResumoFinanceiro() {
        atendimentoSelecionado = null;
        faturaAtual = null;
        procedimentosContainer.getChildren().clear();
        valorPagarLabel.setText(formatarMoeda(BigDecimal.ZERO));
        seguroCombo.getItems().clear();
        seguroCombo.getSelectionModel().clearSelection();
        seguroCombo.setDisable(true);
        pagamentoGroup.selectToggle(null);
        mostrarSkeleton(false);
    }

    private void limparResumo() {
        pacienteNome.setText("-");
        pacienteNif.setText("NIF: -");
        limparResumoFinanceiro();
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal valorFormatado = valor != null ? valor.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return "€ " + valorFormatado.toPlainString();
    }

    private String formatarPercentual(BigDecimal valor) {
        BigDecimal valorFormatado = valor != null ? valor.stripTrailingZeros() : BigDecimal.ZERO;
        return valorFormatado.toPlainString() + "%";
    }

    @FXML
    private void abrirAgenda() throws IOException {
        trocarTela("/fxml/agenda-view.fxml");
    }

    @FXML
    private void abrirPacientes() throws IOException {
        trocarTela("/fxml/pacientes-view.fxml");
    }

    @FXML
    private void fazerLogout() throws IOException {
        SessionContext.limparSessao();
        trocarTela("/fxml/login-view.fxml");
    }

    private void trocarTela(String fxmlPath) throws IOException {
        var resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            mostrarAlerta("A tela solicitada ainda nÃ£o estÃ¡ disponÃ­vel.", Alert.AlertType.INFORMATION);
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        if (MainFX.getSpringContext() != null) {
            loader.setControllerFactory(MainFX.getSpringContext()::getBean);
        }

        Parent root = loader.load();
        Scene scene = new Scene(root);
        aplicarStylesheet(scene, fxmlPath);
        Stage stage = (Stage) consultasListView.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void aplicarStylesheet(Scene scene, String fxmlPath) {
        String cssPath = switch (fxmlPath) {
            case "/fxml/login-view.fxml" -> "/css/login-style.css";
            case "/fxml/payment-view.fxml" -> "/css/payment-style.css";
            default -> null;
        };

        if (cssPath == null) {
            return;
        }

        var cssResource = getClass().getResource(cssPath);
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }
    }

    private void mostrarAlerta(String msg) {
        mostrarAlerta(msg, Alert.AlertType.ERROR);
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("InformaÃ§Ã£o");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
