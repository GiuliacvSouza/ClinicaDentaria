package controller.assistente;

import app.MainFX;
import app.SceneManager;
import app.SessionContext;
import bll.ConsultaService;
import bll.MaterialService;
import bll.MovimentacaoEstoqueService;
import bll.PedidoCompraService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Material;
import model.MovimentacaoEstoque;
import model.Utilizador;
import model.dto.ConsultaAgendadaDTO;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssistenteDashboardController {

    private static final String CSS = "/css/assistente-style.css";
    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM", new Locale("pt", "PT"));
    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HORA_MIN_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label lblNomeUtilizador;
    @FXML private Label lblSaudacao;
    @FXML private Label lblSubtitulo;

    // Cards
    @FXML private Label lblConsultasAgendadas;
    @FXML private Label lblStockCritico;
    @FXML private Label lblPedidosPendentes;
    @FXML private Label lblMovimentacoes24h;

    // Painéis dinâmicos
    @FXML private VBox containerAlertas;
    @FXML private VBox containerMovimentacoes;

    // Tabela — colunas sem sala/gabinete
    @FXML private VBox  containerConsultasGrid;
    @FXML private Label lblRestamConsultas;

    // ─── Services ─────────────────────────────────────────────────────────────

    private ConsultaService            consultaService;
    private MaterialService            materialService;
    private MovimentacaoEstoqueService movimentacaoService;
    private PedidoCompraService        pedidoCompraService;

    @Autowired
    public void setConsultaService(ConsultaService s)            { this.consultaService = s; }
    @Autowired
    public void setMaterialService(MaterialService s)            { this.materialService = s; }
    @Autowired
    public void setMovimentacaoService(MovimentacaoEstoqueService s) { this.movimentacaoService = s; }
    @Autowired
    public void setPedidoCompraService(PedidoCompraService s)    { this.pedidoCompraService = s; }

    // ─── Inicialização ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        preencherNomeNavbar();
        preencherSaudacao();
        carregarCards();
        configurarTabela();
        carregarConsultasHoje();
        carregarAlertasStock();
        carregarMovimentacoesRecentes();
    }

    // ── Nome na navbar ────────────────────────────────────────────────────────

    private void preencherNomeNavbar() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        if (u == null || lblNomeUtilizador == null) return;
        String nome = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim();
        if (!nome.isBlank()) lblNomeUtilizador.setText(nome);
    }

    // ── Saudação ──────────────────────────────────────────────────────────────

    private void preencherSaudacao() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        String pn = (u != null && u.getPrimeiroNome() != null && !u.getPrimeiroNome().isBlank())
                ? u.getPrimeiroNome().trim() : "Assistente";
        set(lblSaudacao, saudacao() + ", " + pn);
        set(lblSubtitulo, "Aqui está o resumo da sua atividade para hoje, "
                + LocalDate.now().format(DATA_FMT) + ".");
    }

    private String saudacao() {
        int h = LocalDateTime.now().getHour();
        if (h < 12) return "Bom dia";
        if (h < 19) return "Boa tarde";
        return "Boa noite";
    }

    // ── Cards ─────────────────────────────────────────────────────────────────

    private void carregarCards() {
        // Consultas hoje
        try {
            if (consultaService != null) {
                long n = consultaService.listarTodasAgendadas().stream()
                        .filter(c -> c.getDataHoraInicio() != null
                                && LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault())
                                .toLocalDate().equals(LocalDate.now()))
                        .count();
                set(lblConsultasAgendadas, String.valueOf(n));
            }
        } catch (Exception ignored) {}

        // Stock crítico
        try {
            if (materialService != null) {
                long n = materialService.listarTodos().stream()
                        .filter(m -> m.getQuantidadeAtual() != null && m.getQuantidadeMinima() != null
                                && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                        .count();
                set(lblStockCritico, String.format("%02d", n));
            }
        } catch (Exception ignored) {}

        // Pedidos
        try {
            if (pedidoCompraService != null)
                set(lblPedidosPendentes, String.format("%02d", pedidoCompraService.listarTodos().size()));
        } catch (Exception ignored) {}

        // Movimentações hoje
        try {
            if (movimentacaoService != null) {
                long n = movimentacaoService.listarTodos().stream()
                        .filter(m -> m.getData() != null && m.getData().equals(LocalDate.now()))
                        .count();
                set(lblMovimentacoes24h, String.valueOf(n));
            }
        } catch (Exception ignored) {}
    }

    // ── Alertas de stock ──────────────────────────────────────────────────────

    private void carregarAlertasStock() {
        if (containerAlertas == null) return;
        containerAlertas.getChildren().clear();

        try {
            if (materialService == null) {
                addVazio(containerAlertas, "Sem alertas de stock.");
                return;
            }
            List<Material> criticos = materialService.listarTodos().stream()
                    .filter(m -> m.getQuantidadeAtual() != null && m.getQuantidadeMinima() != null
                            && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                    .sorted(Comparator.comparingInt(m -> m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0))
                    .limit(5)
                    .toList();

            if (criticos.isEmpty()) {
                addVazio(containerAlertas, "Sem alertas de stock. Tudo em ordem.");
                return;
            }

            boolean primeiro = true;
            for (Material m : criticos) {
                if (!primeiro) {
                    Region div = new Region();
                    div.getStyleClass().add("row-divider");
                    containerAlertas.getChildren().add(div);
                }
                containerAlertas.getChildren().add(criarLinhaAlerta(m));
                primeiro = false;
            }
        } catch (Exception e) {
            addVazio(containerAlertas, "Não foi possível carregar os alertas.");
        }
    }

    private VBox criarLinhaAlerta(Material m) {
        boolean zerado = m.getQuantidadeAtual() != null && m.getQuantidadeAtual() == 0;

        HBox linha = new HBox(10);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("alert-row");
        linha.setPadding(new javafx.geometry.Insets(6, 8, 6, 8));

        Region barra = new Region();
        barra.getStyleClass().add(zerado ? "alert-bar-red" : "alert-bar-orange");
        barra.setMinHeight(34);

        VBox textos = new VBox(2);
        Label titulo = new Label(m.getNome() != null ? m.getNome() : "Material");
        titulo.getStyleClass().add("alert-item-title");

        int qty = m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0;
        int min = m.getQuantidadeMinima() != null ? m.getQuantidadeMinima() : 0;
        String unidade = m.getUnidadeMedida() != null ? " " + m.getUnidadeMedida() : "";
        Label sub = new Label((zerado ? "Stock esgotado" : "Stock baixo") + " — " + qty + unidade + " / mín. " + min + unidade);
        sub.getStyleClass().add("alert-item-sub");
        sub.setWrapText(true);

        textos.getChildren().addAll(titulo, sub);
        linha.getChildren().addAll(barra, textos);

        VBox wrapper = new VBox(linha);
        return wrapper;
    }

    // ── Movimentações recentes ────────────────────────────────────────────────

    private void carregarMovimentacoesRecentes() {
        if (containerMovimentacoes == null) return;
        containerMovimentacoes.getChildren().clear();

        try {
            if (movimentacaoService == null) {
                addVazio(containerMovimentacoes, "Sem movimentações recentes.");
                return;
            }
            List<MovimentacaoEstoque> recentes = movimentacaoService.listarTodos().stream()
                    .filter(m -> m.getData() != null)
                    .sorted(Comparator.comparing(MovimentacaoEstoque::getData).reversed())
                    .limit(4)
                    .toList();

            if (recentes.isEmpty()) {
                addVazio(containerMovimentacoes, "Sem movimentações recentes.");
                return;
            }

            boolean primeiro = true;
            for (MovimentacaoEstoque m : recentes) {
                if (!primeiro) {
                    Region div = new Region();
                    div.getStyleClass().add("row-divider");
                    containerMovimentacoes.getChildren().add(div);
                }
                containerMovimentacoes.getChildren().add(criarLinhaMovimentacao(m));
                primeiro = false;
            }
        } catch (Exception e) {
            addVazio(containerMovimentacoes, "Não foi possível carregar as movimentações.");
        }
    }

    private HBox criarLinhaMovimentacao(MovimentacaoEstoque m) {
        boolean entrada = m.getQuantidade() != null && m.getQuantidade() > 0
                && !"AJUSTE".equalsIgnoreCase(m.getMotivo());

        HBox linha = new HBox(10);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new javafx.geometry.Insets(12, 8, 12, 8));

        Region ponto = new Region();
        ponto.getStyleClass().add(entrada ? "mov-dot-in" : "mov-dot-out");

        VBox textos = new VBox(2);
        javafx.scene.layout.HBox.setHgrow(textos, javafx.scene.layout.Priority.ALWAYS);

        String nomeMaterial = m.getIdMaterial() != null && m.getIdMaterial().getNome() != null
                ? m.getIdMaterial().getNome() : "Material";
        String prefixo = entrada ? "Entrada: " : "Saída: ";
        Label titulo = new Label(prefixo + nomeMaterial);
        titulo.getStyleClass().add("mov-item-title");

        String responsavel = "";
        if (m.getIdUtilizador() != null && m.getIdUtilizador().getUtilizador() != null) {
            var u = m.getIdUtilizador().getUtilizador();
            responsavel = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim();
        }
        String obs = m.getObservacao() != null && !m.getObservacao().isBlank()
                ? m.getObservacao() : responsavel;
        Label sub = new Label(obs.isBlank() ? "-" : obs);
        sub.getStyleClass().add("mov-item-sub");

        textos.getChildren().addAll(titulo, sub);

        Label hora = new Label(m.getData() != null ? m.getData().toString() : "");
        hora.getStyleClass().add("mov-item-time");

        linha.getChildren().addAll(ponto, textos, hora);
        return linha;
    }

    // ── Tabela de próximas consultas ──────────────────────────────────────────

    private void configurarTabela() {
        if (containerConsultasGrid != null) {
            containerConsultasGrid.setFillWidth(true);
        }
    }

    private GridPane criarGrelhaConsulta(String styleClass) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add(styleClass);
        grid.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(grid, Priority.NEVER);

        double[] percentagens = {10, 20, 24, 20, 13, 13};
        for (double percentagem : percentagens) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setPercentWidth(percentagem);
            coluna.setMinWidth(0);
            coluna.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(coluna);
        }
        return grid;
    }

    private GridPane criarCabecalhoConsultas() {
        GridPane header = criarGrelhaConsulta("consultas-grid-header");
        adicionarCelulaTexto(header, 0, "Hora", "consulta-header-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(header, 1, "Paciente", "consulta-header-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(header, 2, "Dentista", "consulta-header-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(header, 3, "Procedimento", "consulta-header-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(header, 4, "Estado", "consulta-header-cell", Pos.CENTER);
        adicionarCelulaTexto(header, 5, "Ações", "consulta-header-cell", Pos.CENTER);
        return header;
    }

    private GridPane criarLinhaConsulta(ConsultaAgendadaDTO dto, boolean alternada) {
        GridPane linha = criarGrelhaConsulta(alternada ? "consultas-grid-row-alt" : "consultas-grid-row");

        String hora = "--:--";
        if (dto.getDataHoraInicio() != null) {
            hora = LocalDateTime.ofInstant(dto.getDataHoraInicio(), ZoneId.systemDefault()).format(HORA_FMT);
        }

        String dentista = dto.getNomeDentista();
        dentista = dentista != null && !dentista.isBlank() ? "Dr(a). " + dentista : "-";

        adicionarCelulaTexto(linha, 0, hora, "consulta-body-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(linha, 1, vazio(dto.getNomePaciente()), "consulta-body-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(linha, 2, dentista, "consulta-body-cell", Pos.CENTER_LEFT);
        adicionarCelulaTexto(linha, 3, vazio(dto.getProcedimento()), "consulta-body-cell", Pos.CENTER_LEFT);

        Label badge = new Label(textoEstado(dto.getStatus()));
        badge.getStyleClass().addAll("status-pill", classeEstado(dto.getStatus()));
        adicionarCelula(linha, 4, badge, "consulta-body-cell", Pos.CENTER);

        Button btn = new Button("Ver pormenores");
        btn.getStyleClass().add("table-action-button");
        btn.setMinWidth(112);
        btn.setPrefWidth(112);
        btn.setMaxWidth(112);
        btn.setOnAction(e -> abrirModalDetalhes(dto.getIdConsulta()));
        adicionarCelula(linha, 5, btn, "consulta-body-cell", Pos.CENTER);

        return linha;
    }

    private void adicionarCelulaTexto(GridPane grid, int coluna, String texto, String styleClass, Pos alinhamento) {
        Label label = new Label(texto);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMaxWidth(Double.MAX_VALUE);
        adicionarCelula(grid, coluna, label, styleClass, alinhamento);
    }

    private void adicionarCelula(GridPane grid, int coluna, Region conteudo, String styleClass, Pos alinhamento) {
        HBox celula = new HBox(conteudo);
        celula.getStyleClass().add(styleClass);
        celula.setAlignment(alinhamento);
        celula.setMinWidth(0);
        celula.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(celula, Priority.ALWAYS);
        grid.add(celula, coluna, 0);
    }

    private void carregarConsultasHoje() {
        if (containerConsultasGrid == null) return;

        containerConsultasGrid.getChildren().clear();
        containerConsultasGrid.getChildren().add(criarCabecalhoConsultas());

        List<ConsultaAgendadaDTO> rows = List.of();
        try {
            if (consultaService != null) {
                rows = consultaService.listarTodasAgendadas().stream()
                        .filter(c -> c.getDataHoraInicio() != null
                                && LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault())
                                .toLocalDate().equals(LocalDate.now()))
                        .sorted(Comparator.comparing(ConsultaAgendadaDTO::getDataHoraInicio,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .limit(8)
                        .toList();
            }
        } catch (Exception ignored) {}

        if (rows.isEmpty()) {
            Label vazio = new Label("Sem consultas agendadas para hoje.");
            vazio.getStyleClass().addAll("section-caption", "consultas-grid-empty");
            containerConsultasGrid.getChildren().add(vazio);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                containerConsultasGrid.getChildren().add(criarLinhaConsulta(rows.get(i), i % 2 != 0));
            }
        }

        long restam = rows.stream()
                .filter(r -> r.getStatus() != EstadoConsulta.CONCLUIDA
                        && r.getStatus() != EstadoConsulta.CANCELADA)
                .count();
        set(lblRestamConsultas, restam + " consultas");
    }

    // ── Modal de detalhes ─────────────────────────────────────────────────────

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
            modal.initOwner(containerConsultasGrid.getScene().getWindow());
            modal.setResizable(false);
            modal.setTitle("Detalhes da Consulta");

            Scene scene = new Scene(root);
            var css = getClass().getResource(CSS);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            ctrl.setStage(modal);
            modal.setScene(scene);
            ctrl.carregarConsulta(idConsulta);
            modal.showAndWait();

        } catch (Exception ex) {
            mostrarErro("Não foi possível abrir os detalhes da consulta.");
        }
    }

    // ─── Botões de ação rápida ────────────────────────────────────────────────

    @FXML private void registarMovimentacao() { navegar("/fxml/assistente/movimentacoes-stock.fxml"); }
    @FXML private void criarPedidoCompra()    { navegar("/fxml/assistente/pedidos-compra.fxml"); }

    // ─── Navegação ────────────────────────────────────────────────────────────

    @FXML private void abrirDashboard()     { /* página actual */ }
    @FXML private void abrirConsultasDia()  { navegar("/fxml/assistente/consultas-dia.fxml"); }
    @FXML private void abrirMateriais()     { navegar("/fxml/assistente/materiais.fxml"); }
    @FXML private void abrirMovimentacoes() { navegar("/fxml/assistente/movimentacoes-stock.fxml"); }
    @FXML private void abrirPedidosCompra() { navegar("/fxml/assistente/pedidos-compra.fxml"); }
    @FXML private void abrirFornecedores()  { navegar("/fxml/assistente/fornecedores.fxml"); }
    @FXML private void abrirAlertas()       { navegar("/fxml/assistente/alertas.fxml"); }
    @FXML private void abrirPerfil()        { navegar("/fxml/assistente/perfil-assistente.fxml"); }

    @FXML
    private void fazerLogout() {
        SessionContext.limparSessao();
        try {
            SceneManager.trocarTelaMaximizado("/fxml/login-view.fxml", "/css/login-style.css");
        } catch (IOException e) {
            mostrarErro("Erro ao fazer logout.");
        }
    }

    private void navegar(String fxml) {
        try {
            SceneManager.trocarTela(fxml, CSS);
        } catch (IOException e) {
            mostrarErro("Página ainda não disponível.");
        }
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private void addVazio(VBox container, String msg) {
        Label l = new Label(msg);
        l.getStyleClass().add("section-caption");
        l.setPadding(new javafx.geometry.Insets(8));
        container.getChildren().add(l);
    }

    private void set(Label lbl, String v) { if (lbl != null) lbl.setText(v); }

    private String nvl(String s) { return s != null ? s.trim() : ""; }

    private String vazio(String s) { return s != null && !s.isBlank() ? s.trim() : "-"; }

    private String textoEstado(EstadoConsulta e) {
        if (e == null) return "-";
        return switch (e) {
            case AGENDADA    -> "Agendada";
            case CONFIRMADA  -> "Confirmada";
            case EM_ESPERA   -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA   -> "Concluída";
            case CANCELADA   -> "Cancelada";
            case PENDENTE    -> "Pendente";
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

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
