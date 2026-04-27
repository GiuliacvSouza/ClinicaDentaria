package controller;

import app.MainFX;
import app.SceneManager;
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
import javafx.util.Duration;
import javafx.util.StringConverter;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentController {

    private static final Locale LOCALE_PT = Locale.forLanguageTag("pt-PT");
    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private ConsultaService consultaService;
    @Autowired private AtendimentoService atendimentoService;
    @Autowired private FaturaService faturaService;
    @Autowired private PacientexSeguroService pacientexSeguroService;
    @Autowired private PagamentoService pagamentoService;

    @FXML private TextField pesquisarField;
    @FXML private ListView<Consulta> consultasListView;
    @FXML private Label nomeUtilizador;
    @FXML private Label consultasResumoLabel;
    @FXML private Label pacienteNome;
    @FXML private Label pacienteNif;
    @FXML private Label consultaMetaLabel;
    @FXML private Label estadoFaturaLabel;
    @FXML private Label dataEmissaoLabel;
    @FXML private VBox procedimentosContainer;
    @FXML private Label valorPagarLabel;
    @FXML private ComboBox<Seguro> seguroCombo;
    @FXML private ToggleButton numerarioBtn;
    @FXML private ToggleButton multibancoBtn;
    @FXML private ToggleButton mbwayBtn;
    @FXML private Button emitirReciboBtn;
    @FXML private VBox paymentSkeleton;
    @FXML private VBox paymentDetailsContent;

    private ToggleGroup pagamentoGroup;
    private FilteredList<Consulta> filteredConsultas;
    private Atendimento atendimentoSelecionado;
    private Fatura faturaAtual;
    private FadeTransition skeletonPulse;

    @FXML
    public void initialize() {
        configurarToggleGroup();
        configurarUtilizador();
        configurarSeguroCombo();
        configurarSkeleton();
        configurarPesquisa();
        configurarLista();
        carregarConsultasConcluidas();
        limparResumo();
    }

    private void configurarToggleGroup() {
        pagamentoGroup = new ToggleGroup();
        numerarioBtn.setToggleGroup(pagamentoGroup);
        multibancoBtn.setToggleGroup(pagamentoGroup);
        mbwayBtn.setToggleGroup(pagamentoGroup);
    }

    private void configurarUtilizador() {
        Utilizador utilizadorLogado = SessionContext.getUtilizadorLogado();
        if (utilizadorLogado == null) {
            mostrarAlerta("Sessao expirada. Faca login novamente.");
            return;
        }

        nomeUtilizador.setText(formatarNome(utilizadorLogado));
    }

    private void configurarSeguroCombo() {
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
    }

    private void configurarPesquisa() {
        pesquisarField.textProperty().addListener((obs, oldValue, newValue) -> aplicarFiltro(newValue));
    }

    private void configurarLista() {
        consultasListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                limparResumo();
            } else {
                carregarDetalhesConsulta(selected);
            }
        });

        consultasListView.setCellFactory(listView -> new ListCell<>() {
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

                VBox card = new VBox(10);
                card.getStyleClass().add("consulta-card");

                HBox topRow = new HBox(10);
                Label nomeLabel = new Label(formatarNome(paciente));
                nomeLabel.getStyleClass().add("consulta-card-title");

                Label statusLabel = new Label("Concluida");
                statusLabel.getStyleClass().addAll("agenda-status-pill", "agenda-status-concluido");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                topRow.getChildren().addAll(nomeLabel, spacer, statusLabel);

                Label metaLabel = new Label(formatarMetaConsulta(consulta, dentista));
                metaLabel.getStyleClass().add("consulta-card-meta");
                metaLabel.setWrapText(true);

                HBox bottomRow = new HBox(8);
                bottomRow.getStyleClass().add("consulta-card-tags");

                Label nifLabel = new Label("NIF: " + valorOuPadrao(paciente != null ? paciente.getNif() : null));
                nifLabel.getStyleClass().add("consulta-chip");

                Label tipoLabel = new Label(valorOuPadrao(consulta.getTipo()));
                tipoLabel.getStyleClass().add("consulta-chip");

                bottomRow.getChildren().addAll(nifLabel, tipoLabel);
                card.getChildren().addAll(topRow, metaLabel, bottomRow);

                setText(null);
                setGraphic(card);
            }
        });
    }

    private void carregarConsultasConcluidas() {
        List<Consulta> consultas = consultaService.listarPorStatus(EstadoConsulta.CONCLUIDA).stream()
            .filter(this::consultaVisivelParaPagamentoSeguro)
            .collect(Collectors.toList());

        filteredConsultas = new FilteredList<>(FXCollections.observableArrayList(consultas), consulta -> true);
        consultasListView.setItems(filteredConsultas);
        atualizarResumoLista();
    }

    private void aplicarFiltro(String filtroTexto) {
        if (filteredConsultas == null) {
            return;
        }

        filteredConsultas.setPredicate(consulta -> {
            if (filtroTexto == null || filtroTexto.isBlank()) {
                return true;
            }

            Utilizador paciente = getPacienteUtilizador(consulta);
            if (paciente == null) {
                return false;
            }

            String filtro = filtroTexto.trim().toLowerCase(LOCALE_PT);
            return formatarNome(paciente).toLowerCase(LOCALE_PT).contains(filtro)
                    || (paciente.getNif() != null && paciente.getNif().toLowerCase(LOCALE_PT).contains(filtro));
        });

        atualizarResumoLista();
    }

    private boolean consultaDisponivelParaPagamentoSeguro(Consulta consulta) {
        try {
            return consultaDisponivelParaPagamento(consulta);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Determina se uma consulta concluida deve ser apresentada na vista de pagamentos.
     * Agora inclui consultas que nao tenham um Atendimento associado (serao mostradas,
     * mas a emissao de fatura so e possivel apos criar um Atendimento).
     */
    private boolean consultaVisivelParaPagamentoSeguro(Consulta consulta) {
        try {
            if (consulta == null) return false;

            Atendimento atendimento = atendimentoService.buscarPorConsulta(consulta);
            if (atendimento == null) {
                // Mostrar consultas concluídas mesmo sem atendimento
                return true;
            }

            Fatura fatura = faturaService.buscarPorAtendimento(atendimento.getId());
            return fatura == null || fatura.getEstado() != EstadoFatura.PAGA;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean consultaDisponivelParaPagamento(Consulta consulta) {
        if (consulta == null || consulta.getStatus() != EstadoConsulta.CONCLUIDA) {
            return false;
        }

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
        pacienteNome.setText(formatarNome(paciente));
        pacienteNif.setText("NIF: " + valorOuPadrao(paciente != null ? paciente.getNif() : null));
        consultaMetaLabel.setText(formatarMetaConsulta(consulta, getDentistaUtilizador(consulta)));

        try {
            atendimentoSelecionado = atendimentoService.buscarPorConsulta(consulta);

            if (atendimentoSelecionado == null) {
                // Consulta sem atendimento: mostramos informações básicas e permitimos
                // que o utilizador saiba que e necessario criar um atendimento para faturar.
                faturaAtual = null;
                preencherResumoFatura();
                carregarSegurosPaciente(consulta.getIdPaciente());
                procedimentosContainer.getChildren().clear();
                valorPagarLabel.setText(formatarMoeda(BigDecimal.ZERO));
                atualizarEstadoAcao();
                mostrarSkeleton(false);
                return;
            }

            faturaAtual = faturaService.buscarPorAtendimento(atendimentoSelecionado.getId());
            preencherResumoFatura();
            carregarSegurosPaciente(consulta.getIdPaciente());
            preencherProcedimentos();
            atualizarTotais();
            atualizarEstadoAcao();
            mostrarSkeleton(false);
        } catch (RuntimeException e) {
            limparResumoFinanceiro();
            mostrarSkeleton(false);
            mostrarAlerta(e.getMessage());
        }
    }

    private void preencherResumoFatura() {
        if (faturaAtual == null) {
            estadoFaturaLabel.setText("Pronta a emitir");
            dataEmissaoLabel.setText("-");
            return;
        }

        estadoFaturaLabel.setText(faturaAtual.getEstado() == null ? "-" : formatarEstadoFatura(faturaAtual.getEstado()));
        dataEmissaoLabel.setText(faturaAtual.getDataEmissao() == null ? "-" : faturaAtual.getDataEmissao().format(DATA_FORMATTER));
    }

    private void preencherProcedimentos() {
        procedimentosContainer.getChildren().clear();

        if (atendimentoSelecionado == null || atendimentoSelecionado.getProcedimentos() == null) {
            return;
        }

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

            GridPane linha = new GridPane();
            linha.getStyleClass().add("procedure-row");
            linha.setHgap(10);
            linha.getColumnConstraints().addAll(
                    criarColunaProcedimento(38),
                    criarColunaProcedimento(22),
                    criarColunaProcedimento(14),
                    criarColunaProcedimento(26)
            );

            Label procedimentoLabel = new Label(procedimento.getNome());
            procedimentoLabel.getStyleClass().add("procedure-cell-primary");
            procedimentoLabel.setWrapText(true);

            Label tipoLabel = new Label(valorOuPadrao(procedimento.getTipo()));
            tipoLabel.getStyleClass().add("procedure-cell");
            tipoLabel.setWrapText(true);

            Label ivaLabel = new Label(formatarPercentual(taxaIva));
            ivaLabel.getStyleClass().add("procedure-cell");

            Label valorLabel = new Label(formatarMoeda(valorFinalItem));
            valorLabel.getStyleClass().add("procedure-cell-value");

            linha.add(procedimentoLabel, 0, 0);
            linha.add(tipoLabel, 1, 0);
            linha.add(ivaLabel, 2, 0);
            linha.add(valorLabel, 3, 0);
            GridPane.setHalignment(valorLabel, HPos.RIGHT);

            procedimentosContainer.getChildren().add(linha);
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
        Consulta consultaSelecionada = consultasListView.getSelectionModel().getSelectedItem();
        if (consultaSelecionada == null || atendimentoSelecionado == null) {
            mostrarAlerta("Selecione uma consulta concluida primeiro.");
            return;
        }

        MetodoPagamento metodoSelecionado = null;
        try {
            metodoSelecionado = getMetodoSelecionado();
        } catch (RuntimeException ignored) {
            // Não obrigar seleção de método: permitimos marcar sempre como paga
        }

        Utilizador utilizadorLogado = SessionContext.getUtilizadorLogado();
        if (utilizadorLogado == null) {
            mostrarAlerta("Sessao expirada. Faca login novamente.");
            return;
        }

        if (faturaAtual == null) {
            faturaAtual = faturaService.emitirFaturaPorAtendimento(atendimentoSelecionado);
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setIdUtilizador(utilizadorLogado);
        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setValorPago(obterValorAPagar());
        pagamento.setMetodo(metodoSelecionado);
        pagamento.setIdFatura(faturaAtual);

        // Registrar pagamento (validações no service garantem integridade)
        pagamentoService.registrarPagamento(pagamento);

        // Sempre marcar a fatura como PAGA e persistir
        faturaAtual.setEstado(EstadoFatura.PAGA);
        faturaAtual = faturaService.salvar(faturaAtual);

        // Atualizar o estado da consulta para Faturada (idempotente via serviço)
        try {
            consultaService.faturarConsulta(consultaSelecionada.getId());
        } catch (RuntimeException ignored) {
            // Se a transição for inválida, ainda garantimos que a fatura está marcada como paga
        }

        filteredConsultas.getSource().remove(consultaSelecionada);
        consultasListView.getSelectionModel().clearSelection();
        atualizarResumoLista();
        limparResumo();

        mostrarAlerta("Fatura-recibo emitida com sucesso. A consulta passou para o estado Faturada.",
                Alert.AlertType.INFORMATION);
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
        throw new RuntimeException("Selecione um metodo de pagamento.");
    }

    private BigDecimal obterValorAPagar() {
        BigDecimal valor = faturaAtual != null ? faturaAtual.getValorFinal() : calcularValorAtendimento();
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void atualizarTotais() {
        valorPagarLabel.setText(formatarMoeda(obterValorAPagar()));
    }

    private void atualizarEstadoAcao() {
        boolean semSelecao = atendimentoSelecionado == null;
        boolean jaFaturada = faturaAtual != null && faturaAtual.getEstado() == EstadoFatura.PAGA;

        emitirReciboBtn.setDisable(semSelecao || jaFaturada);
        emitirReciboBtn.setText(jaFaturada ? "FATURA JA EMITIDA" : "EMITIR FATURA / RECIBO");
    }

    private void atualizarResumoLista() {
        if (consultasResumoLabel == null || filteredConsultas == null) {
            return;
        }

        int total = filteredConsultas.size();
        consultasResumoLabel.setText(total == 1
                ? "1 consulta concluida pronta para pagamento"
                : total + " consultas concluidas prontas para pagamento");
    }

    private void configurarSkeleton() {
        skeletonPulse = new FadeTransition(Duration.millis(950), paymentSkeleton);
        skeletonPulse.setFromValue(0.55);
        skeletonPulse.setToValue(1.0);
        skeletonPulse.setAutoReverse(true);
        skeletonPulse.setCycleCount(Animation.INDEFINITE);
        mostrarSkeleton(false);
    }

    private void mostrarSkeleton(boolean mostrar) {
        paymentSkeleton.setVisible(mostrar);
        paymentSkeleton.setManaged(mostrar);
        paymentDetailsContent.setVisible(!mostrar);
        paymentDetailsContent.setManaged(!mostrar);

        if (mostrar) {
            skeletonPulse.play();
        } else {
            skeletonPulse.stop();
            paymentSkeleton.setOpacity(1.0);
        }
    }

    private void limparResumoFinanceiro() {
        atendimentoSelecionado = null;
        faturaAtual = null;
        procedimentosContainer.getChildren().clear();
        estadoFaturaLabel.setText("-");
        dataEmissaoLabel.setText("-");
        valorPagarLabel.setText(formatarMoeda(BigDecimal.ZERO));
        seguroCombo.getItems().clear();
        seguroCombo.getSelectionModel().clearSelection();
        seguroCombo.setDisable(true);
        pagamentoGroup.selectToggle(null);
        atualizarEstadoAcao();
    }

    private void limparResumo() {
        pacienteNome.setText("-");
        pacienteNif.setText("NIF: -");
        consultaMetaLabel.setText("Selecione uma consulta concluida para emitir a fatura.");
        limparResumoFinanceiro();
    }

    private Utilizador getPacienteUtilizador(Consulta consulta) {
        return consulta != null && consulta.getIdPaciente() != null ? consulta.getIdPaciente().getUtilizador() : null;
    }

    private Utilizador getDentistaUtilizador(Consulta consulta) {
        return consulta != null && consulta.getIdDentista() != null ? consulta.getIdDentista().getUtilizador() : null;
    }

    private String formatarNome(Utilizador utilizador) {
        if (utilizador == null) {
            return "Paciente sem dados";
        }

        String primeiroNome = utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome().trim() : "";
        String ultimoNome = utilizador.getUltimoNome() != null ? utilizador.getUltimoNome().trim() : "";
        String nomeCompleto = (primeiroNome + " " + ultimoNome).trim();
        return nomeCompleto.isBlank() ? "Paciente sem dados" : nomeCompleto;
    }

    private String formatarMetaConsulta(Consulta consulta, Utilizador dentista) {
        String dataHora = consulta != null && consulta.getDataHoraInicio() != null
                ? consulta.getDataHoraInicio().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DATA_HORA_FORMATTER)
                : "--/--/---- --:--";
        String dentistaNome = valorOuPadrao(dentista != null ? formatarNome(dentista) : null);
        String tipo = valorOuPadrao(consulta != null ? consulta.getTipo() : null);
        return dataHora + "  |  " + tipo + "  |  " + dentistaNome;
    }

    private String formatarEstadoFatura(EstadoFatura estado) {
        return switch (estado) {
            case PENDENTE -> "Pendente";
            case PAGA -> "Paga";
            case CANCELADA -> "Cancelada";
            case ANULADA -> "Anulada";
        };
    }

    private BigDecimal calcularValorAtendimento() {
        if (atendimentoSelecionado == null || atendimentoSelecionado.getProcedimentos() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (AtendimentoProcedimento item : atendimentoSelecionado.getProcedimentos()) {
            Procedimento procedimento = item.getProcedimento();
            if (procedimento == null || procedimento.getValor() == null) {
                continue;
            }

            int quantidade = item.getQuantidade() != null ? item.getQuantidade() : 1;
            BigDecimal desconto = item.getDesconto() != null ? item.getDesconto() : BigDecimal.ZERO;
            BigDecimal taxaIva = procedimento.getTaxaIva() != null ? procedimento.getTaxaIva() : BigDecimal.ZERO;

            BigDecimal subtotal = procedimento.getValor().multiply(BigDecimal.valueOf(quantidade));
            BigDecimal valorBase = subtotal.multiply(
                    BigDecimal.ONE.subtract(desconto.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
            );
            BigDecimal valorFinal = valorBase.multiply(
                    BigDecimal.ONE.add(taxaIva.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
            );

            total = total.add(valorFinal);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatarPercentual(BigDecimal valor) {
        BigDecimal valorFormatado = valor != null ? valor.stripTrailingZeros() : BigDecimal.ZERO;
        return valorFormatado.toPlainString() + "%";
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal valorFormatado = valor != null
                ? valor.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        NumberFormat formatador = NumberFormat.getNumberInstance(LOCALE_PT);
        formatador.setMinimumFractionDigits(2);
        formatador.setMaximumFractionDigits(2);
        return "EUR " + formatador.format(valorFormatado);
    }

    private String valorOuPadrao(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    @FXML
    private void abrirAgenda() throws IOException {
        SceneManager.trocarTela("/fxml/Agenda.fxml", "/css/dashboard-style.css");
    }

    @FXML
    private void abrirPacientes() throws IOException {
        SceneManager.trocarTela("/fxml/pacientes.fxml", "/css/dashboard-style.css");
    }

    @FXML
    private void fazerLogout() throws IOException {
        SessionContext.limparSessao();
        SceneManager.trocarTelaMaximizado("/fxml/login-view.fxml", "/css/login-style.css");
    }

    private void trocarTela(String fxmlPath) throws IOException {
        var resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            mostrarAlerta("A tela solicitada ainda nao esta disponivel.", Alert.AlertType.INFORMATION);
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        if (MainFX.getSpringContext() != null) {
            loader.setControllerFactory(MainFX.getSpringContext()::getBean);
        }

        Stage stage = (Stage) consultasListView.getScene().getWindow();
        boolean estavaMaximizada = stage.isMaximized();
        double larguraAtual = Math.max(stage.getWidth(), stage.getScene() != null ? stage.getScene().getWidth() : 0);
        double alturaAtual = Math.max(stage.getHeight(), stage.getScene() != null ? stage.getScene().getHeight() : 0);
        Parent root = loader.load();
        Scene scene = new Scene(root, larguraAtual, alturaAtual);
        aplicarStylesheet(scene, fxmlPath);
        stage.setScene(scene);
        if (!estavaMaximizada) {
            stage.setWidth(larguraAtual);
            stage.setHeight(alturaAtual);
        }
        stage.setMaximized(estavaMaximizada);
        stage.show();
    }

    private void aplicarStylesheet(Scene scene, String fxmlPath) {
        String cssPath = switch (fxmlPath) {
            case "/fxml/login-view.fxml" -> "/css/login-style.css";
            case "/fxml/payment-view.fxml" -> "/css/payment-style.css";
            case "/fxml/Agenda.fxml", "/fxml/pacientes.fxml" -> "/css/dashboard-style.css";
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

    private void mostrarAlerta(String mensagem) {
        mostrarAlerta(mensagem, Alert.AlertType.ERROR);
    }

    private void mostrarAlerta(String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Informacao");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
