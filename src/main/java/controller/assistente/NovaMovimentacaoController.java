package controller.assistente;

import app.SessionContext;
import bll.MaterialService;
import bll.MovimentacaoEstoqueService;
import dal.AssistenteRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Material;
import model.Utilizador;
import model.enums.TipoMovimentacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NovaMovimentacaoController {

    private static final String OUTRO = "Outro";

    private static final List<String> MOTIVOS_ENTRADA = List.of(
            "Reposição de stock",
            "Receção de pedido de compra",
            "Correção de inventário",
            "Devolução ao stock",
            OUTRO
    );

    private static final List<String> MOTIVOS_SAIDA = List.of(
            "Uso em consulta",
            "Material danificado",
            "Material expirado",
            "Correção de inventário",
            OUTRO
    );

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private ComboBox<Material> cbMaterial;
    @FXML private ComboBox<String>   cbTipo;
    @FXML private VBox               paneInfoMaterial;
    @FXML private Label              lblStockAtual;
    @FXML private Label              lblStockMinimo;
    @FXML private Label              lblUnidade;
    @FXML private Label              lblEstadoStock;
    @FXML private TextField          txtQuantidade;
    @FXML private ComboBox<String>   cbMotivo;
    @FXML private VBox               paneOutroMotivo;
    @FXML private TextField          txtOutroMotivo;
    @FXML private TextArea           txtObservacao;
    @FXML private Label              lblErro;

    // ─── Dependências ─────────────────────────────────────────────────────────

    @Autowired private MaterialService            materialService;
    @Autowired private MovimentacaoEstoqueService movimentacaoService;
    @Autowired private AssistenteRepository       assistenteRepository;

    // ─── Estado ───────────────────────────────────────────────────────────────

    private Stage   stage;
    private boolean saved = false;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        carregarMateriais();
        carregarTipos();

        cbMaterial.valueProperty().addListener((obs, ant, novo) -> atualizarInfoMaterial(novo));
        cbTipo.valueProperty().addListener((obs, ant, novo)     -> atualizarMotivos(novo));
        cbMotivo.valueProperty().addListener((obs, ant, novo)   -> toggleCampoOutro(novo));
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isSaved()          { return saved; }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarMateriais() {
        try {
            List<Material> ativos = materialService.listarTodos().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getAtivo()))
                    .sorted((a, b) -> {
                        if (a.getNome() == null) return 1;
                        if (b.getNome() == null) return -1;
                        return a.getNome().compareToIgnoreCase(b.getNome());
                    })
                    .toList();
            cbMaterial.setItems(FXCollections.observableArrayList(ativos));
            cbMaterial.setCellFactory(lv -> celulaMaterial());
            cbMaterial.setButtonCell(celulaMaterial());
        } catch (Exception e) {
            mostrarErroInline("Não foi possível carregar os materiais.");
        }
    }

    private void carregarTipos() {
        cbTipo.setItems(FXCollections.observableArrayList("Entrada", "Saída"));
    }

    // ─── Actualizar UI ────────────────────────────────────────────────────────

    private void atualizarInfoMaterial(Material m) {
        if (m == null) {
            paneInfoMaterial.setVisible(false);
            paneInfoMaterial.setManaged(false);
            return;
        }

        int atual    = m.getQuantidadeAtual()  != null ? m.getQuantidadeAtual()  : 0;
        int minimo   = m.getQuantidadeMinima() != null ? m.getQuantidadeMinima() : 0;
        String unid  = m.getUnidadeMedida()    != null ? m.getUnidadeMedida()    : "-";

        lblStockAtual.setText(atual + " " + unid);
        lblStockMinimo.setText(minimo + " " + unid);
        lblUnidade.setText(unid);

        if (atual == 0) {
            lblEstadoStock.setText("Sem stock");
            lblEstadoStock.getStyleClass().setAll("stock-info-critico");
        } else if (atual <= minimo) {
            lblEstadoStock.setText("Stock crítico");
            lblEstadoStock.getStyleClass().setAll("stock-info-critico");
        } else {
            lblEstadoStock.setText("Normal");
            lblEstadoStock.getStyleClass().setAll("stock-info-normal");
        }

        paneInfoMaterial.setVisible(true);
        paneInfoMaterial.setManaged(true);
    }

    private void atualizarMotivos(String tipo) {
        if ("Entrada".equals(tipo)) {
            cbMotivo.setItems(FXCollections.observableArrayList(MOTIVOS_ENTRADA));
        } else if ("Saída".equals(tipo)) {
            cbMotivo.setItems(FXCollections.observableArrayList(MOTIVOS_SAIDA));
        } else {
            cbMotivo.setItems(FXCollections.observableArrayList());
        }
        cbMotivo.getSelectionModel().clearSelection();
        cbMotivo.setValue(null);
        ocultarCampoOutro();
    }

    private void toggleCampoOutro(String motivo) {
        boolean isOutro = OUTRO.equals(motivo);
        if (paneOutroMotivo != null) {
            paneOutroMotivo.setVisible(isOutro);
            paneOutroMotivo.setManaged(isOutro);
        }
        if (!isOutro && txtOutroMotivo != null) txtOutroMotivo.clear();
    }

    private void ocultarCampoOutro() {
        if (paneOutroMotivo != null) {
            paneOutroMotivo.setVisible(false);
            paneOutroMotivo.setManaged(false);
        }
        if (txtOutroMotivo != null) txtOutroMotivo.clear();
    }

    // ─── Guardar ──────────────────────────────────────────────────────────────

    @FXML
    private void guardarMovimentacao() {
        esconderErro();

        // Validar material
        Material material = cbMaterial.getValue();
        if (material == null) { mostrarErroInline("Selecione um material."); return; }

        // Validar tipo
        String tipoStr = cbTipo.getValue();
        if (tipoStr == null || tipoStr.isBlank()) {
            mostrarErroInline("Selecione o tipo de movimentação."); return;
        }
        TipoMovimentacao tipo = "Entrada".equals(tipoStr)
                ? TipoMovimentacao.ENTRADA : TipoMovimentacao.SAIDA;

        // Validar quantidade
        int quantidade;
        try {
            quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            if (quantidade <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarErroInline("A quantidade deve ser superior a zero."); return;
        }

        // Validar motivo
        String motivoSelecionado = cbMotivo.getValue();
        if (motivoSelecionado == null || motivoSelecionado.isBlank()) {
            mostrarErroInline("Indique o motivo da movimentação."); return;
        }

        String motivo;
        if (OUTRO.equals(motivoSelecionado)) {
            motivo = txtOutroMotivo != null ? txtOutroMotivo.getText().trim() : "";
            if (motivo.isBlank()) {
                mostrarErroInline("Indique o motivo personalizado."); return;
            }
        } else {
            motivo = motivoSelecionado;
        }

        String observacao = txtObservacao != null ? txtObservacao.getText().trim() : "";
        Integer assistenteId = resolverAssistenteId();

        try {
            movimentacaoService.registarMovimentacao(
                    material.getId(),
                    assistenteId,
                    tipo,
                    quantidade,
                    motivo,
                    observacao.isBlank() ? null : observacao
            );

            mostrarSucesso(tipo == TipoMovimentacao.ENTRADA
                    ? "Entrada de stock registada com sucesso."
                    : "Saída de stock registada com sucesso.");
            saved = true;
            if (stage != null) stage.close();

        } catch (RuntimeException ex) {
            mostrarErroInline(ex.getMessage());
        }
    }

    @FXML
    private void fechar() {
        if (stage != null) stage.close();
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private Integer resolverAssistenteId() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        if (u == null) return null;
        try {
            return assistenteRepository.findByUtilizadorId(u.getId())
                    .map(a -> a.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private ListCell<Material> celulaMaterial() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Material item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String nome   = item.getNome() != null ? item.getNome() : "-";
                String unid   = item.getUnidadeMedida() != null
                        ? " (" + item.getUnidadeMedida() + ")" : "";
                int atual = item.getQuantidadeAtual() != null ? item.getQuantidadeAtual() : 0;
                setText(nome + unid + " — stock: " + atual);
            }
        };
    }

    private void mostrarErroInline(String msg) {
        if (lblErro == null) return;
        lblErro.setText(msg);
        lblErro.setVisible(true);
        lblErro.setManaged(true);
    }

    private void esconderErro() {
        if (lblErro == null) return;
        lblErro.setVisible(false);
        lblErro.setManaged(false);
    }

    private void mostrarSucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
